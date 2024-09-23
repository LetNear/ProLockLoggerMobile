package com.example.prolockloggerv1;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AttendanceApi {
    @GET("attendance/instructor")
    Call<AttendanceResponse> getAttendanceLogsByInstructor(@Query("email") String email);
}

