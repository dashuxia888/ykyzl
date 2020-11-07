package com.github.pengpan.dto.resp;

import lombok.Data;

import java.util.List;

@Data
public class DoctorAdmScheduleResp {
    private List<DataOne> dataOne;
    private String doc_id;
    private String doc_level;
    private String doc_name;
    private String need_appointment;
    private String portrait_url;
    private String spec_dr;

    @Data
    public static class DataOne {
        private List<ScheduleInfo> schedule_info;
        private String service_date;
        private String weekday;

        @Data
        public static class ScheduleInfo {
            private String address;
            private String admit_range;
            private String course;
            private String fee;
            private String schedule_item_code;
            private String time;
        }
    }

}
