package com.github.pengpan.config;

import com.alibaba.fastjson.JSON;
import com.github.pengpan.dto.resp.*;
import com.github.pengpan.service.AccountService;
import com.github.pengpan.service.CoreService;
import com.github.pengpan.task.RegistrationTask;
import com.github.pengpan.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class StartupRunner implements CommandLineRunner {

    @Resource
    private CoreService coreService;
    @Resource
    private RegistrationTask registrationTask;
    @Resource
    private AccountService accountService;

    @Override
    public void run(String... args) throws Exception {
//        initAuth();
//        getDepartmentsInfo();
//        getSevenDayCourse();
//        getOneDayCourse();
//        getDepartmentDoctors();
//        getDoctorAdmSchedule();
//        viewPatientList();

//        startTask("2020-08-21");
    }

    public void startTask(String targetDate) throws Exception {
        registrationTask.setTargetDate(targetDate);

        registrationTask.initTask();
        TimeUnit.SECONDS.sleep(5);

        registrationTask.initLogin();
        TimeUnit.SECONDS.sleep(5);

        registrationTask.startTask();
    }

    public void viewPatientList() {
        Result<List<PatientResp>> result = coreService.viewPatientList("17190069934");
        log.info(JSON.toJSONString(result));
    }

    public void initAuth() {
        accountService.initAuth("17190069934", "a123456");
    }

    public void getDoctorAdmSchedule() {
        Result<DoctorAdmScheduleResp> result = coreService.getDoctorAdmSchedule("1477", "32");
        log.info(JSON.toJSONString(result));
    }

    public void getDepartmentDoctors() {
        Result<List<DepartmentDoctorResp>> result = coreService.getDepartmentDoctors("32");
        log.info(JSON.toJSONString(result));
    }

    public void getOneDayCourse() {
        Result<OneDayCourseResp> result = coreService.getOneDayCourse("32", "2020-08-01");
        log.info(JSON.toJSONString(result));
    }

    private void getSevenDayCourse() {
        Result<List<SevenDayCourseResp>> result = coreService.getSevenDayCourse("32");
        log.info(JSON.toJSONString(result));
    }

    private void getDepartmentsInfo() {
        Result<List<DepartmentInfoResp>> result = coreService.getDepartmentsInfo();
        log.info(JSON.toJSONString(result));
    }
}
