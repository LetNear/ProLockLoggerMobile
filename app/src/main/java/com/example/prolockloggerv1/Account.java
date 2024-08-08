package com.example.prolockloggerv1;

import androidx.annotation.NonNull;

public class Account {
    private String name;
    private String email;
    private String google_id;

    // Default constructor required for deserialization
    public Account() {
    }

    public Account(String name, String email, String googleId) {
        this.name = name;
        this.email = email;
        this.google_id = googleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGoogleId(String googleId) {
        this.google_id = googleId;
    }

    public String getGoogleId() {
        return this.google_id;
    }

    @NonNull
    public String toString() {
        return "Name: " + this.name + "\nEmail: " + this.email;
    }
}
