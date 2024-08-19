package com.example.prolockloggerv1;

public class UserDetailsResponse {
    private String first_name;
    private String middle_name;
    private String last_name;
    private String suffix;
    private String date_of_birth;
    private String gender;
    private String contact_number;
    private String complete_address;

    // Getters and Setters
    public String getFirst_name() { return first_name; }
    public void setFirst_name(String first_name) { this.first_name = first_name; }

    public String getMiddle_name() { return middle_name; }
    public void setMiddle_name(String middle_name) { this.middle_name = middle_name; }

    public String getLast_name() { return last_name; }
    public void setLast_name(String last_name) { this.last_name = last_name; }

    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }

    public String getDate_of_birth() { return date_of_birth; }
    public void setDate_of_birth(String date_of_birth) { this.date_of_birth = date_of_birth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getContact_number() { return contact_number; }
    public void setContact_number(String contact_number) { this.contact_number = contact_number; }

    public String getComplete_address() { return complete_address; }
    public void setComplete_address(String complete_address) { this.complete_address = complete_address; }
}

