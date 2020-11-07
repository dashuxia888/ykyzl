package com.github.pengpan.dto.resp;

import lombok.Data;

import java.util.List;

@Data
public class DepartmentInfoResp {
    private String dept_group;
    private List<Dept> depts;

    @Data
    public static class Dept {
        private String dept_id;
        private String dept_name;
        private String description;
        private String doc_id;
    }
}
