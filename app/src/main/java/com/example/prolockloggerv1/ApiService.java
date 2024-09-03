package com.example.prolockloggerv1;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("posts")
    Call<List<Account>> getAccounts();

    @POST("posts")
    Call<Void> addAccount(@Body Account account);

    @GET("users/{email}")
    Call<Account> getAccount(@Path("email") String email);

    @GET("userInfo/{email}")
    Call<UserDetailsResponse> getUserDetails(@Path("email") String email);

    @PUT("userInfo/update")
    Call<Void> updateUserDetails(@Body UserDetailsRequest userDetailsRequest);

    @GET("userInfo/{email}")
    Call<CheckEmailResponse> checkEmailRegistration(@Path("email") String email);

    // Endpoint for student count by email (role_number 2)
    @GET("student-count/{email}")
    Call<StudentCountResponse> getStudentCountByEmail(@Path("email") String email);

    // Endpoint for instructor schedule count by email (role_number 2)
    @GET("instructor/schedule-count/{email}")
    Call<ScheduleCountResponse> getScheduleCountByEmail(@Path("email") String email);

    // Endpoint for total student-schedule count by email (role_number 3)
    @GET("student-schedule-count")
    Call<StudentScheduleCountResponse> getStudentScheduleCount(@Query("email") String email);

    // Endpoint for total logs count by email (role_number 3)
    @GET("total-logs-count")
    Call<LogCountResponse> getTotalLogsCount(@Query("email") String email);

    @GET("/api/lab-schedules")
    Call<List<LabSchedule>> getLabSchedules();

    @GET("alternative-student-schedule/{email}")
    Call<List<Schedule>> getAlternativeSchedulesByEmail(@Path("email") String email);
}
