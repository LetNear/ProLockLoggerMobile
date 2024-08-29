package com.example.prolockloggerv1;

import com.google.gson.annotations.SerializedName;

public class LogCountResponse {

    @SerializedName("email")
    private String email;

    @SerializedName("log_count")
    private int logCount;

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getLogCount() {
        return logCount;
    }

    public void setLogCount(int logCount) {
        this.logCount = logCount;
    }
}
