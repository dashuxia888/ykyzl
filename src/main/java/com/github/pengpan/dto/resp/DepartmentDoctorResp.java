package com.github.pengpan.dto.resp;

import lombok.Data;

@Data
public class DepartmentDoctorResp {
    private int courses;
    private String doc_id;
    private String doc_level;
    private String doc_name;
    private String need_appointment;
    private String portrait_url;
    private String served;
}
