package com.example.prolockloggerv1;

import com.google.gson.annotations.SerializedName;

public class ScheduleCountResponse {
    @SerializedName("instructor")
    private String instructor;

    @SerializedName("email")
    private String email;

    @SerializedName("schedule_count")
    private Integer scheduleCount;

    // Getters
    public String getInstructor() {
        return instructor;
    }

    public String getEmail() {
        return email;
    }

    public Integer getScheduleCount() {
        return scheduleCount;
    }
}
