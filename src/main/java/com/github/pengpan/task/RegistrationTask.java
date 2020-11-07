package com.github.pengpan.task;

import com.github.pengpan.entity.Account;
import com.github.pengpan.entity.Task;
import com.github.pengpan.service.AccountService;
import com.github.pengpan.service.RegistrationService;
import com.github.pengpan.service.TaskService;
import com.github.pengpan.util.CollUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RegistrationTask {

    private String targetDate = null;
    private List<Task> tasks = null;
    private List<Account> accounts = null;
    private boolean isRun = false;

    @Resource
    private RegistrationService registrationService;
    @Resource
    private TaskService taskService;
    @Resource
    private AccountService accountService;

    /**
     * 初始化任务
     */
    @Scheduled(cron = "0 30 07 ? * MON-FRI")
    public void initTask() {
        /*
        if (StringUtil.isNotEmpty(targetDate)) {
            // 手动挂号
            tasks = taskService.selectByTargetDate(targetDate);
            targetDate = null;
        } else {
            tasks = new ArrayList<>();
            // 特需门诊
            String targetDate = DateUtil.getWorkDays(3);
            tasks.addAll(taskService.selectByTargetDate(targetDate, true));
            // 普通门诊
            targetDate = DateUtil.getWorkDays(5);
            tasks.addAll(taskService.selectByTargetDate(targetDate, false));
        }
        */
        tasks = taskService.findAllTask();

        List<Long> accountIds = tasks.stream().map(Task::getAccountId).distinct().collect(Collectors.toList());
        accounts = accountService.selectByIds(accountIds);

        isRun = CollUtil.isNotEmpty(tasks) && CollUtil.isNotEmpty(accounts);
    }

    /**
     * 初始化登录
     */
    @Scheduled(cron = "0 35 07 ? * MON-FRI")
    public void initLogin() {
        if (isRun) {
            List<Account> accountsTemp = new ArrayList<>(accounts);
            registrationService.initLogin(accountsTemp);
        }
    }

    /**
     * 初始化IP池
     */
    @Scheduled(cron = "0 50 08 ? * MON-FRI")
    public void initIPPool() {
        if (isRun) {
            registrationService.initIPPool();
        }
    }

    /**
     * 再次初始化登录
     */
    @Scheduled(cron = "0 52 08 ? * MON-FRI")
    public void initLoginAgain() {
        if (isRun) {
            List<Account> accountsTemp = new ArrayList<>(accounts);
            registrationService.initLogin(accountsTemp);
        }
    }

    /**
     * 开始抢号
     */
    @Scheduled(cron = "59 59 08 ? * MON-FRI")
    public void startTask() {
        if (isRun) {
            List<Task> tasksTemp = new ArrayList<>(tasks);
            List<Account> accountsTemp = new ArrayList<>(accounts);
            registrationService.startTask(tasksTemp, accountsTemp);
        }
    }

    public void setTargetDate(String targetDate) {
        this.targetDate = targetDate;
    }

    public boolean isRun() {
        return isRun;
    }
}
