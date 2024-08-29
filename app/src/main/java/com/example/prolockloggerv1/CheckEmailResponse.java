package com.example.prolockloggerv1;

public class CheckEmailResponse {
    private boolean registered;
    private String email;
    private String roleNumber;  // Add this field

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public String getEmail() {
        return email;
    }

    // Add this getter method for roleNumber
    public String getRoleNumber() {
        return roleNumber;
    }

    // Add this setter method for roleNumber
    public void setRoleNumber(String roleNumber) {
        this.roleNumber = roleNumber;
    }
}
