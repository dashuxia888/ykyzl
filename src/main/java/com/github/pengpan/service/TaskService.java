package com.github.pengpan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pengpan.common.Constant;
import com.github.pengpan.common.cache.key.CacheKey;
import com.github.pengpan.dto.AccountAuth;
import com.github.pengpan.dto.DocSchedule;
import com.github.pengpan.dto.resp.ConfirmAppointmentResp;
import com.github.pengpan.dto.resp.DoctorAdmScheduleResp;
import com.github.pengpan.dto.resp.OneDayCourseResp;
import com.github.pengpan.dto.resp.PatientResp;
import com.github.pengpan.entity.Account;
import com.github.pengpan.entity.Task;
import com.github.pengpan.mapper.TaskMapper;
import com.github.pengpan.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskService {

    @Resource
    private TaskMapper taskMapper;
    @Resource
    private AccountService accountService;
    @Resource
    private CoreService coreService;
    @Resource
    private RedisManager redisManager;

    public void startTask(Task task, String mobile) {
        long taskId = task.getId();

        if (StringUtil.isEmpty(mobile)) {
            fail(taskId, "未找到任务绑定的账号");
            return;
        }

        String auth = accountService.getAuth(mobile);
        if (StringUtil.isEmpty(auth)) {
            fail(taskId, "登录授权失败");
            return;
        }

        String key = CacheKey.accountAuth(mobile);
        AccountAuth accountAuth = redisManager.get(key, AccountAuth.class);
        String deviceId = Optional.ofNullable(accountAuth).map(AccountAuth::getDeviceId).orElse(null);
        String sessionId = Optional.ofNullable(accountAuth).map(AccountAuth::getSessionId).orElse(null);
        boolean beijingInsurance = isBeijingInsurance(mobile);

        AtomicInteger brushCount = new AtomicInteger(Constant.BRUSH_LOOP_COUNT);
        while (true) {
            try {
                HttpUtil.setSession(sessionId);
                DocSchedule docSchedule = getDocSchedule(task);
                if (docSchedule != null && docSchedule.isEmpty()) {
                    fail(taskId, "号已挂完");
                    break;
                }
                if (brush(docSchedule, auth, deviceId, beijingInsurance)) {
                    updateToSuccess(taskId, mobile, beijingInsurance);
                    break;
                }
                if (brushCount.decrementAndGet() == 0) {
                    fail(taskId, "[102]挂号失败");
                    break;
                }
            } catch (Exception e) {
                if ("号已挂完".equals(e.getMessage())) {
                    fail(taskId, "号已挂完");
                    break;
                }
                if (brushCount.decrementAndGet() == 0) {
                    fail(taskId, e.getMessage());
                    break;
                }
            } finally {
                HttpUtil.removeSession();
            }
        }
    }

    public void manualStartTask(Task task, Account account) {
        long taskId = task.getId();
        String mobile = account.getMobile();

        accountService.initAuth(mobile, account.getPassword());
        String auth = accountService.getAuth(mobile);
        if (StringUtil.isEmpty(auth)) {
            fail(taskId, "登录授权失败");
            return;
        }

        String key = CacheKey.accountAuth(mobile);
        AccountAuth accountAuth = redisManager.get(key, AccountAuth.class);
        String deviceId = Optional.ofNullable(accountAuth).map(AccountAuth::getDeviceId).orElse(null);
        String sessionId = Optional.ofNullable(accountAuth).map(AccountAuth::getSessionId).orElse(null);
        boolean beijingInsurance = isBeijingInsurance(mobile);

        String tKey = CacheKey.manualTask(taskId);
        redisManager.set(tKey, "true", 1, TimeUnit.HOURS);

        boolean success = false;
        String msg = null;
        while (redisManager.get(tKey) != null) {
            try {
                HttpUtil.setSession(sessionId);
                DocSchedule docSchedule = getDocSchedule(task);
                if (docSchedule != null && docSchedule.isEmpty()) {
                    msg = "号已满";
                    break;
                }
                if (brush(docSchedule, auth, deviceId, beijingInsurance)) {
                    success = true;
                    break;
                }
                msg = "[102]挂号失败";
            } catch (Exception e) {
                msg = e.getMessage();
            } finally {
                HttpUtil.removeSession();
            }
        }

        if (!success) {
            fail(taskId, msg);
        }

        redisManager.del(tKey);
    }

    private DocSchedule getDocSchedule(Task task) {
        String deptName = task.getDeptName();
        if (deptName.contains("特需")) {
            return getDocScheduleByDate(task);
        } else {
            return getDocScheduleByDoctor(task);
        }
    }

    /**
     * 按专家挂号（特需门诊除外）
     */
    private DocSchedule getDocScheduleByDoctor(Task task) {
        Result<DoctorAdmScheduleResp> doctorAdmSchedule = coreService.getDoctorAdmSchedule(task.getDocId(), task.getDeptId());

        List<DoctorAdmScheduleResp.DataOne> dataOnes = Optional.ofNullable(doctorAdmSchedule)
                .map(Result::getData)
                .map(DoctorAdmScheduleResp::getDataOne)
                .orElse(new ArrayList<>());

        DoctorAdmScheduleResp.DataOne dataOne = dataOnes.stream().filter(x -> {
            String serviceDate = x.getService_date();
            Date targetDate = DateUtil.parse(task.getTargetDate(), DateUtil.YYYY_MM_DD);
            return Objects.equals(serviceDate, DateUtil.format(targetDate, DateUtil.MM_DD));
        }).findFirst().orElse(null);
        if (dataOne == null) {
            return null;
        }

        List<DoctorAdmScheduleResp.DataOne.ScheduleInfo> scheduleInfos = dataOne.getSchedule_info();
        if (CollUtil.isEmpty(scheduleInfos)) {
            return null;
        }

        DoctorAdmScheduleResp.DataOne.ScheduleInfo scheduleInfo = scheduleInfos.stream().filter(x -> {
            String code = x.getSchedule_item_code();
            return StringUtil.isNotEmpty(code) && !"xxx".equals(code);
        }).findFirst().orElse(null);
        if (scheduleInfo == null) {
            return null;
        }

        DocSchedule docSchedule = new DocSchedule();
        docSchedule.setDocLevel(doctorAdmSchedule.getData().getDoc_level());
        docSchedule.setAdmitRange(scheduleInfo.getAdmit_range());
        docSchedule.setFee(scheduleInfo.getFee());
        docSchedule.setScheduleItemCode(scheduleInfo.getSchedule_item_code());
        docSchedule.setEmpty("已满".equals(scheduleInfo.getCourse()));
        docSchedule.setTaskId(task.getId());

        return docSchedule;
    }

    /**
     * 按日期挂号
     */
    private DocSchedule getDocScheduleByDate(Task task) {
        Result<OneDayCourseResp> oneDayCourse = coreService.getOneDayCourse(task.getDeptId(), task.getTargetDate());

        List<OneDayCourseResp.Course> morning = Optional.ofNullable(oneDayCourse)
                .map(Result::getData)
                .map(OneDayCourseResp::getMorning)
                .orElse(new ArrayList<>());
        List<OneDayCourseResp.Course> afternoon = Optional.ofNullable(oneDayCourse)
                .map(Result::getData)
                .map(OneDayCourseResp::getAfternoon)
                .orElse(new ArrayList<>());

        List<OneDayCourseResp.Course> courseList = new ArrayList<>();
        courseList.addAll(morning);
        courseList.addAll(afternoon);

        OneDayCourseResp.Course course = courseList.parallelStream()
                .filter(x -> Objects.equals(task.getDocId(), x.getDoc_id()))
                .findFirst().orElse(null);
        if (course == null) {
            return null;
        }

        DocSchedule docSchedule = new DocSchedule();
        docSchedule.setDocLevel(course.getTitle());
        docSchedule.setAdmitRange(course.getAdmit_range());
        docSchedule.setFee(course.getFee());
        docSchedule.setScheduleItemCode(course.getSchedule_item_code());
        docSchedule.setEmpty("已满".equals(course.getAvailable()));
        docSchedule.setTaskId(task.getId());

        return docSchedule;
    }

    private boolean brush(DocSchedule docSchedule, String auth, String deviceId, boolean beijingInsurance) throws Exception {
        Task task = updateSchedule(docSchedule);
        Result<List<ConfirmAppointmentResp>> result = confirmAppointment(task, auth, deviceId, beijingInsurance);
        if (result.isError()) {
            throw new Exception(result.getMsg());
        }
        return true;
    }

    private Result<List<ConfirmAppointmentResp>> confirmAppointment(Task task, String auth, String deviceId, boolean beijingInsurance) {
        finalCheck(task.getId(), task.getDeptId(), task.getDocId(), auth);
        return coreService.confirmAppointment(task, auth, deviceId, beijingInsurance);
    }

    private void finalCheck(long taskId, String deptId, String docId, String auth) {
        if (StringUtil.isEmpty(deptId)) {
            fail(taskId, "参数错误[deptId]");
        }
        if (StringUtil.isEmpty(docId)) {
            fail(taskId, "参数错误[docId]");
        }
        if (StringUtil.isEmpty(auth)) {
            fail(taskId, "参数错误[auth]");
        }
    }

    public void success(long id) {
        updateStatus(id, 1, "成功");
        log.info("[{}]挂号成功", id);
    }

    public void success(long id, String msg) {
        updateStatus(id, 1, msg);
        log.info("[{}]挂号成功", id);
    }

    public void updateToSuccess(long taskId, String mobile, boolean beijingInsurance) {
        if (beijingInsurance) {
            success(taskId, "成功挂号");
        } else {
            success(taskId, "锁号成功，请您务必完成支付");
        }
    }

    public boolean isBeijingInsurance(String mobile) {
        Result<List<PatientResp>> result = coreService.viewPatientList(mobile);
        if (result.isError()) {
            return false;
        }
        List<PatientResp> data = result.getData();
        if (CollUtil.isEmpty(data)) {
            return false;
        }
        PatientResp resp = data.stream().filter(x -> "0".equals(x.getDefault_visitor_flag())).findFirst().orElse(null);
        if (resp == null) {
            return false;
        }
        String detail = resp.getPatient_type_detail();
        return detail != null && detail.contains("北京");
    }

    public void fail(long id, String remark) {
        updateStatus(id, 2, remark);
        log.info("[{}]挂号失败：{}", id, remark);
    }

    public void updateStatus(long id, int status, String remark) {
        if (StringUtil.isNotEmpty(remark) && remark.length() > 200) {
            remark = remark.substring(0, 200);
        }
        Task task = taskMapper.selectById(id);
        if (task != null) {
            Task record = Task.builder()
                    .status(status)
                    .remark(remark)
                    .id(id)
                    .build();
            taskMapper.updateById(record);
        }
    }

    public Task updateSchedule(DocSchedule docSchedule) {
        if (docSchedule == null) {
            throw new RuntimeException("获取医生排班信息失败");
        }
        long taskId = docSchedule.getTaskId();
        Task record = Task.builder()
                .scheduleItemCode(docSchedule.getScheduleItemCode())
                .docLevel(docSchedule.getDocLevel())
                .regFee(docSchedule.getFee())
                .admitRange(docSchedule.getAdmitRange())
                .id(taskId)
                .build();
        if (taskMapper.updateById(record) < 1) {
            throw new RuntimeException("更新医生排班信息失败");
        }
        return taskMapper.selectById(taskId);
    }

    public Result<Boolean> add(Task task) {
        if (task == null) {
            return Result.error("非法请求");
        }
        Account account = accountService.findById(task.getAccountId());
        if (account == null) {
            return Result.error("账户不存在");
        }
        if (StringUtil.isEmpty(task.getDeptId())) {
            return Result.error("科室不能为空");
        }
        if (StringUtil.isEmpty(task.getDocId())) {
            return Result.error("医生不能为空");
        }
        if (StringUtil.isEmpty(task.getTargetDate())) {
            return Result.error("日期不能为空");
        } else {
            /*
            LocalTime now = LocalTime.now();
            if (now.isAfter(LocalTime.of(7, 0)) && now.isBefore(LocalTime.of(9, 0))) {
                boolean isTX = Optional.ofNullable(task.getDeptName())
                        .map(x -> x.contains("特需"))
                        .orElse(false);
                String targetDate = DateUtil.getWorkDays(isTX ? 3 : 5);
                if (!Objects.equals(task.getTargetDate(), targetDate)) {
                    return Result.error("所选日期今天不放号");
                }
            }
            */
        }

        task.setUserName(account.getMobile());

        taskMapper.insert(task);
        return Result.success(true);
    }

    public Result<Boolean> delete(long id) {
        Assert.isLegalId(id);
        taskMapper.deleteById(id);
        return Result.success(true);
    }

    public Result<Boolean> update(Task task) {
        taskMapper.updateById(task);
        return Result.success(true);
    }

    public Result<List<Task>> getList(long userId, String userName, int page, int limit) {
        IPage<Task> p = taskMapper.selectPage(new Page<>(page, limit), userId, userName);
        return Result.success(p.getRecords(), p.getTotal());
    }

    public Task findById(long id) {
        Assert.isLegalId(id);
        return taskMapper.selectById(id);
    }

    public Result<Task> get(long id) {
        Task account = findById(id);
        if (account == null) {
            return Result.error("账户不存在");
        }
        return Result.success(account);
    }

    public List<Task> selectByTargetDate(@NonNull String targetDate) {
        if (StringUtil.isEmpty(targetDate)) {
            return new ArrayList<>();
        }
        return taskMapper.selectByTargetDate(targetDate);
    }

    public List<Task> selectByTargetDate(@NonNull String targetDate, boolean special) {
        return selectByTargetDate(targetDate).stream()
                .filter(x -> special == x.getDeptName().startsWith("特需"))
                .collect(Collectors.toList());
    }

    public List<Task> findAllTask() {
        return taskMapper.findAllTask();
    }
}
