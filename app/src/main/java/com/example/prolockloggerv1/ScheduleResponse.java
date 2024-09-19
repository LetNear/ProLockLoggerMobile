package com.example.prolockloggerv1;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ScheduleResponse {
    @SerializedName("instructor")
    private String instructor;

    @SerializedName("email")
    private String email;

    @SerializedName("schedules")
    private List<Schedule> schedules;

    // Getters
    public String getInstructor() {
        return instructor;
    }

    public String getEmail() {
        return email;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }
}

