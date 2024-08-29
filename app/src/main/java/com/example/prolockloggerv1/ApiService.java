package com.example.prolockloggerv1;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

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

    // Endpoint for student count
    @GET("student-count/{email}")
    Call<StudentCountResponse> getStudentCountByEmail(@Path("email") String email);

    // Endpoint for instructor schedule count
    @GET("instructor/schedule-count/{email}")
    Call<StudentCountResponse> getScheduleCountByEmail(@Path("email") String email);
}







