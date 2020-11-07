package com.github.pengpan.dto.req;

import lombok.Data;

@Data
public class ConfirmAppointmentReq {
    private String doc_id;
    private String appKey;
    private String admit_range;
    private String doc_name;
    private String start_date;
    private String schedule_item_code;
    private String reg_fee;
    private String timeStamp;
    private String doc_level;
    private String service_date;
    private String dept_id;
    private String deviceId;
    private String verify_code;
}
