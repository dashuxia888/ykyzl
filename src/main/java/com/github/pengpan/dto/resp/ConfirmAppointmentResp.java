package com.github.pengpan.dto.resp;

import lombok.Data;

@Data
public class ConfirmAppointmentResp {
    private String admit_range;
    private String app_order_id;
    private String doc_level;
    private String doc_name;
    private String loc_room;
    private String loc_zone;
    private String patient_id;
    private String reg_fee;
    private String seq_code;
}
