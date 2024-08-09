package com.example.prolockloggerv1;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ScheduleApi {
    @GET("lab-schedules")
    Call<List<Schedule>> getSchedules();
}
