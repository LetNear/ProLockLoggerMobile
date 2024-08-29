package com.example.prolockloggerv1;

import com.google.gson.annotations.SerializedName;

public class NextScheduleResponse {

    @SerializedName("instructor")
    private String instructorName;

    @SerializedName("email")
    private String email;

    @SerializedName("next_schedule")
    private ScheduleDetails nextSchedule;

    public String getInstructorName() {
        return instructorName;
    }

    public String getEmail() {
        return email;
    }

    public ScheduleDetails getNextSchedule() {
        return nextSchedule;
    }

    public static class ScheduleDetails {

        @SerializedName("subject_code")
        private String subjectCode;

        @SerializedName("subject_name")
        private String subjectName;

        @SerializedName("class_start")
        private String classStart;

        @SerializedName("class_end")
        private String classEnd;

        @SerializedName("day_of_the_week")
        private String dayOfTheWeek;

        public String getSubjectCode() {
            return subjectCode;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public String getClassStart() {
            return classStart;
        }

        public String getClassEnd() {
            return classEnd;
        }

        public String getDayOfTheWeek() {
            return dayOfTheWeek;
        }
    }
}
