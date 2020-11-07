package com.github.pengpan.service;

import com.alibaba.fastjson.TypeReference;
import com.github.pengpan.common.Constant;
import com.github.pengpan.common.cache.key.CacheKey;
import com.github.pengpan.dto.AccountAuth;
import com.github.pengpan.dto.req.ConfirmAppointmentReq;
import com.github.pengpan.dto.req.LoginReq;
import com.github.pengpan.dto.resp.*;
import com.github.pengpan.entity.Task;
import com.github.pengpan.task.RegistrationTask;
import com.github.pengpan.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CoreService {

    private final static String BASE_URL;
    private final static String BASE_URL_V_1_2_0 = "https://www.aksofy.com/v1.2.0/iOS";
    private final static String BASE_URL_V_1_2_1 = "https://www.aksofy.com/v1.2.1/iOS";
    private final static String BASE_URL_V_1_2_2 = "https://www.aksofy.com/v1.2.2/iOS";
    private final static String BASE_URL_V_1_2_3 = "https://www.aksofy.com/v1.2.3/iOS";

    static {
        BASE_URL = BASE_URL_V_1_2_3;
    }

    @Resource
    private AccountService accountService;
    @Resource
    private RedisManager redisManager;
    @Resource
    private RegistrationTask registrationTask;

    /**
     * 新版登录
     */
    public Result<AccountAuth> login(String mobile, String password) {
        try {
            HttpUtil.setSession(StringUtil.uuid());

            Result<String> captchaResult = captcha();
            if (captchaResult.isError()) {
                log.info("账号 [{}] 登录失败!", mobile);
                return Result.error("[2]登录失败");
            }

            String verifyCode = CaptchaUtil.decrypt(captchaResult.getData());
            if (StringUtil.isEmpty(verifyCode)) {
                log.info("账号 [{}] 登录失败!", mobile);
                return Result.error("[3]登录失败");
            }

            return login(mobile, password, verifyCode);
        } finally {
            HttpUtil.removeSession();
        }
    }

    /**
     * 图形验证码
     */
    public Result<String> captcha() {
        String url = BASE_URL + "/user/captcha";

        Map<String, Object> req = new HashMap<>();
        req.put("appKey", Constant.APP_KEY);
        req.put("timeStamp", DateUtil.getTimestamp());

        TypeReference<String> respType = new TypeReference<String>() {
        };

        String data = HttpUtil.doPost(url, req, respType);
        return Result.success(data);
    }

    /**
     * 登录
     */
    public Result<AccountAuth> login(String mobile, String password, String verifyCode) {
        if (StringUtil.isEmpty(mobile)) {
            return Result.error("手机号不能为空");
        }
        if (StringUtil.isEmpty(password)) {
            return Result.error("密码不能为空");
        }
        if (StringUtil.isEmpty(verifyCode)) {
            return Result.error("验证码不能为空");
        }

        String url = BASE_URL + "/user/login";
        String deviceId = MD5Util.md5(String.valueOf(System.currentTimeMillis()));

        LoginReq req = new LoginReq();
        req.setAppKey(Constant.APP_KEY);
        req.setDeviceId(deviceId);
        req.setPhoneNum(mobile);
        req.setPassword(MD5Util.md5(password));
        req.setType(0);
        req.setVerify_code(verifyCode);

        TypeReference<LoginResp> respType = new TypeReference<LoginResp>() {
        };

        try {
            LoginResp data = HttpUtil.doPost(url, req, respType, true);
            if (data == null) {
                return Result.error("[1]登录失败");
            }

            log.info("账号 [{}] 登录成功!", mobile);

            String token = data.getToken();

            AccountAuth accountAuth = new AccountAuth();
            accountAuth.setMobile(mobile);
            accountAuth.setToken(token);
            accountAuth.setDeviceId(deviceId);
            accountAuth.setSessionId(HttpUtil.getSession());

            return Result.success(accountAuth);
        } catch (Exception e) {
            log.info("账号 [{}] 登录失败!", mobile);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 就诊人列表
     */
    public Result<List<PatientResp>> viewPatientList(String mobile) {
        if (StringUtil.isEmpty(mobile)) {
            return Result.error("手机号不能为空");
        }

        String key = CacheKey.patientList(mobile);
        List<PatientResp> cacheData = redisManager.getList(key, PatientResp.class);
        if (CollUtil.isNotEmpty(cacheData)) {
            return Result.success(cacheData);
        }

        String auth = accountService.getAuth(mobile);
        if (StringUtil.isEmpty(auth)) {
            return Result.error("获取授权失败");
        }

        String url = BASE_URL + "/patient/viewPatientList";
        TypeReference<List<PatientResp>> respType = new TypeReference<List<PatientResp>>() {
        };

        try {
            List<PatientResp> data = HttpUtil.doPost(url, null, respType, auth, true);
            if (data == null) {
                return Result.success(new ArrayList<>());
            }
            if (CollUtil.isNotEmpty(data)) {
                redisManager.set(key, data, 3, TimeUnit.HOURS);
            }
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取科室信息
     */
    public Result<List<DepartmentInfoResp>> getDepartmentsInfo() {
        String key = CacheKey.departmentInfo();
        List<DepartmentInfoResp> cacheData = redisManager.getList(key, DepartmentInfoResp.class);
        if (CollUtil.isNotEmpty(cacheData)) {
            return Result.success(cacheData);
        }

        String url = BASE_URL + "/source/getDepartmentsInfo";
        TypeReference<List<DepartmentInfoResp>> respType = new TypeReference<List<DepartmentInfoResp>>() {
        };

        try {
            List<DepartmentInfoResp> data = HttpUtil.doPost(url, null, respType);

            if (CollUtil.isNotEmpty(data)) {
                redisManager.set(key, data, 1, TimeUnit.DAYS);
            }

            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取号源信息
     */
    public Result<List<SevenDayCourseResp>> getSevenDayCourse(String deptId) {
        String url = BASE_URL + "/source/getSevenDayCourse";

        Map<String, Object> req = new HashMap<>();
        req.put("appKey", Constant.APP_KEY);
        req.put("dept_id", deptId);

        TypeReference<List<SevenDayCourseResp>> respType = new TypeReference<List<SevenDayCourseResp>>() {
        };

        List<SevenDayCourseResp> data = HttpUtil.doPost(url, req, respType);
        if (data == null) {
            return Result.success(new ArrayList<>());
        }
        return Result.success(data);
    }

    /**
     * 获取号源信息
     */
    public Result<OneDayCourseResp> getOneDayCourse(String deptId, String startDate) {
        String url = BASE_URL + "/source/getOneDayCourse";

        Map<String, Object> req = new HashMap<>();
        req.put("timeStamp", DateUtil.getTimestamp());
        req.put("appKey", Constant.APP_KEY);
        req.put("dept_id", deptId);
        req.put("dept_flag", "");
        req.put("start_date", startDate);

        TypeReference<OneDayCourseResp> respType = new TypeReference<OneDayCourseResp>() {
        };

        OneDayCourseResp data = HttpUtil.doPost(url, req, respType);
        return Result.success(data);
    }

    /**
     * 获取医生列表
     */
    public Result<List<DepartmentDoctorResp>> getDepartmentDoctors(String deptId) {
        String key = CacheKey.doctorForDepartment(deptId);
        List<DepartmentDoctorResp> cacheData = redisManager.getList(key, DepartmentDoctorResp.class);
        if (CollUtil.isNotEmpty(cacheData)) {
            return Result.success(cacheData);
        }

        String url = BASE_URL + "/source/getDepartmentDoctors";

        Map<String, Object> req = new HashMap<>();
        req.put("appKey", Constant.APP_KEY);
        req.put("dept_id", deptId);

        TypeReference<List<DepartmentDoctorResp>> respType = new TypeReference<List<DepartmentDoctorResp>>() {
        };

        List<DepartmentDoctorResp> data = HttpUtil.doPost(url, req, respType);

        if (CollUtil.isNotEmpty(data)) {
            redisManager.set(key, data, 1, TimeUnit.DAYS);
        }

        return Result.success(data);
    }

    /**
     * 获取医生所有的号
     */
    public Result<DoctorAdmScheduleResp> getDoctorAdmSchedule(String docId, String deptId) {
        String url = BASE_URL + "/source/getDoctorAdmSchedule";

        Map<String, Object> req = new HashMap<>();
        req.put("appKey", Constant.APP_KEY);
        req.put("doc_id", docId);
        req.put("dept_id", deptId);

        TypeReference<DoctorAdmScheduleResp> respType = new TypeReference<DoctorAdmScheduleResp>() {
        };

        DoctorAdmScheduleResp data = HttpUtil.doPost(url, req, respType);
        return Result.success(data);
    }

    public Result<List<ConfirmAppointmentResp>> confirmAppointment(Task task, String auth, String deviceId, boolean beijingInsurance) {
        Result<String> captchaResult = captcha();
        if (captchaResult.isError()) {
            return Result.error("挂号获取验证码失败");
        }
        String verifyCode = CaptchaUtil.decrypt(captchaResult.getData());
        if (StringUtil.isEmpty(verifyCode)) {
            return Result.error("挂号解析验证码失败");
        }
        return confirmAppointment(task, auth, deviceId, beijingInsurance, verifyCode);
    }

    /**
     * 预约挂号
     */
    public Result<List<ConfirmAppointmentResp>> confirmAppointment(Task task, String auth, String deviceId, boolean beijingInsurance, String verifyCode) {
        String url = BASE_URL + (beijingInsurance ? "/source/confirmAppointment" : "/source/confirmRegistration");

        ConfirmAppointmentReq req = new ConfirmAppointmentReq();
        req.setReg_fee(task.getRegFee());
        req.setTimeStamp(DateUtil.getTimestamp());
        req.setDoc_name(task.getDeptName());
        req.setStart_date(task.getTargetDate());
        req.setDoc_id(task.getDocId());
        req.setDeviceId(deviceId);
        req.setDept_id(task.getDeptId());
        req.setAppKey(Constant.APP_KEY);
        req.setAdmit_range(task.getAdmitRange());
        req.setDoc_level(task.getDocLevel());
        req.setSchedule_item_code(task.getScheduleItemCode());
        req.setVerify_code(verifyCode);

        TypeReference<List<ConfirmAppointmentResp>> respType = new TypeReference<List<ConfirmAppointmentResp>>() {
        };

        try {
            List<ConfirmAppointmentResp> data = HttpUtil.doPost(url, req, respType, auth, true);
            if (data == null) {
                return Result.success(new ArrayList<>());
            }
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 手动初始化任务
     */
    public Result<Boolean> initTask() {
        registrationTask.initTask();
        registrationTask.initLogin();
        registrationTask.initIPPool();
        return Result.success(true);
    }

    public Result<Boolean> manualBrush() {
        registrationTask.initTask();
        if (!registrationTask.isRun()) {
            return Result.error("当前没有待处理任务");
        }
        registrationTask.startTask();
        return Result.success(true);
    }
}
