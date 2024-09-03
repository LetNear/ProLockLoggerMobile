package com.example.prolockloggerv1;

import com.google.gson.annotations.SerializedName;

public class DoorControlResponse {
    @SerializedName("message")
    private String message;

    // Include other fields as per your response
    // e.g., log details if needed

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
