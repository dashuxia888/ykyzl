package com.github.pengpan.common.cache.key;

import com.github.pengpan.common.cache.BaseKeyEnum;
import com.github.pengpan.common.cache.KeyAssembly;
import com.github.pengpan.common.cache.ModuleKeyEnum;
import com.github.pengpan.util.Assert;
import com.github.pengpan.util.RegexUtil;

public class CacheKey extends KeyAssembly {

    public enum KeyEnum implements BaseKeyEnum {
        ACCOUNT_AUTH,
        DEPARTMENT_INFO,
        DOCTOR_FOR_DEPARTMENT,
        DOCTOR_ADM_SCHEDULE,
        MANUAL_TASK,
        PATIENT_LIST,
        ;

        @Override
        public ModuleKeyEnum module() {
            return ModuleKeyEnum.CACHE;
        }
    }

    public static String accountAuth(String mobile) {
        Assert.isTrue(RegexUtil.isMobile(mobile));
        return assemble(KeyEnum.ACCOUNT_AUTH, mobile);
    }

    public static String departmentInfo() {
        return assemble(KeyEnum.DEPARTMENT_INFO);
    }

    public static String doctorForDepartment(String deptId) {
        return assemble(KeyEnum.DOCTOR_FOR_DEPARTMENT, deptId);
    }

    public static String doctorAdmSchedule(String deptId, String docId) {
        return assemble(KeyEnum.DOCTOR_ADM_SCHEDULE, deptId, docId);
    }

    public static String manualTask(long taskId) {
        Assert.isLegalId(taskId);
        return assemble(KeyEnum.MANUAL_TASK, taskId);
    }

    public static String patientList(String mobile) {
        Assert.isTrue(RegexUtil.isMobile(mobile));
        return assemble(KeyEnum.PATIENT_LIST, mobile);
    }
}
