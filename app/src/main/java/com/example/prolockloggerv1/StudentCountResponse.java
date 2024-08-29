package com.example.prolockloggerv1;

import com.google.gson.annotations.SerializedName;

public class StudentCountResponse {

    @SerializedName("student_count")
    private int studentCount;

    @SerializedName("schedule_count")
    private int scheduleCount;

    public int getStudentCount() {
        return studentCount;
    }

    public int getScheduleCount() {
        return scheduleCount;
    }

    public void setScheduleCount(int scheduleCount) {
        this.scheduleCount = scheduleCount;
    }
}
