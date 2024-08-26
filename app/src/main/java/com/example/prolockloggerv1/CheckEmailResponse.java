package com.example.prolockloggerv1;

public class CheckEmailResponse {
    private boolean registered;
    private String email;

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public String getEmail() {
        return email;
    }
}
