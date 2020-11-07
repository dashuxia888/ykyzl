package com.github.pengpan.dto.resp;

import lombok.Data;

import java.util.List;

@Data
public class OneDayCourseResp {
    private List<Course> morning;
    private List<Course> afternoon;

    @Data
    public static class Course {
        private String address;
        private String admit_range;
        private String available;
        private String doc_id;
        private String fee;
        private String name;
        private String need_appointment;
        private String schedule_item_code;
        private boolean served;
        private String time;
        private String title;
    }
}
