package com.example.prolockloggerv1;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ScheduleApi {
    @GET("lab-schedules")
    Call<List<Schedule>> getSchedules();

    @GET("lab-schedules/email/{email}")
    Call<ScheduleResponse> getSchedulesByEmail(@Path("email") String email);

    @GET("student/schedule-details")
    Call<ScheduleResponse> getStudentScheduleByEmail(@Query("email") String email);
}
