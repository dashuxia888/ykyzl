package com.github.pengpan.controller;

import com.github.pengpan.dto.resp.DepartmentDoctorResp;
import com.github.pengpan.dto.resp.DepartmentInfoResp;
import com.github.pengpan.dto.resp.DoctorAdmScheduleResp;
import com.github.pengpan.entity.Task;
import com.github.pengpan.service.CoreService;
import com.github.pengpan.service.TaskService;
import com.github.pengpan.util.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController extends BaseController {

    @Resource
    private TaskService taskService;
    @Resource
    private CoreService coreService;

    @RequestMapping("/add")
    public Result<Boolean> add(@RequestBody Task task) {
        return checkLogin(userId -> {
            task.setUserId(userId);
            return taskService.add(task);
        });
    }

    @RequestMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable long id) {
        return checkLogin(() -> taskService.delete(id));
    }

    @RequestMapping("/update")
    public Result<Boolean> update(@RequestBody Task task) {
        return checkLogin(userId -> {
            task.setUserId(userId);
            return taskService.update(task);
        });
    }

    @RequestMapping("/getList")
    public Result<List<Task>> getList(String userName, int page, int limit) {
        return checkLogin(userId -> taskService.getList(userId, userName, page, limit));
    }

    @RequestMapping("/get/{id}")
    public Result<Task> get(@PathVariable long id) {
        return checkLogin(() -> taskService.get(id));
    }

    @RequestMapping("/getDepartmentsInfo")
    public Result<List<DepartmentInfoResp>> getDepartmentsInfo() {
        return checkLogin(() -> coreService.getDepartmentsInfo());
    }

    @RequestMapping("/getDepartmentDoctors/{deptId}")
    public Result<List<DepartmentDoctorResp>> getDepartmentDoctors(@PathVariable String deptId) {
        return checkLogin(() -> coreService.getDepartmentDoctors(deptId));
    }

    @RequestMapping("/getDoctorAdmSchedule/{deptId}/{docId}")
    public Result<DoctorAdmScheduleResp> getDoctorAdmSchedule(@PathVariable String deptId, @PathVariable String docId) {
        return checkLogin(() -> coreService.getDoctorAdmSchedule(docId, deptId));
    }

    @RequestMapping("/init")
    public Result<Boolean> initTask() {
        return checkLogin(() -> coreService.initTask());
    }

    @RequestMapping("/manualBrush")
    public Result<Boolean> manualBrush() {
        return checkLogin(() -> coreService.manualBrush());
    }

}
