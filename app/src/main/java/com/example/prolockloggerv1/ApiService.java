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




}
