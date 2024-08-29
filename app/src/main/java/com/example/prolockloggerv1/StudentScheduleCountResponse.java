package com.example.prolockloggerv1;

import com.google.gson.annotations.SerializedName;

public class StudentScheduleCountResponse {

    @SerializedName("student")
    private String student;

    @SerializedName("schedule_count")
    private int scheduleCount;

    // Getters and Setters
    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    public int getScheduleCount() {
        return scheduleCount;
    }

    public void setScheduleCount(int scheduleCount) {
        this.scheduleCount = scheduleCount;
    }
}
