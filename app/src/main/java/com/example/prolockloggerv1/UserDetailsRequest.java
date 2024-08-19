package com.example.prolockloggerv1;

public class UserDetailsRequest {
    private String email;
    private String first_name;
    private String middle_name;
    private String last_name;
    private String suffix;
    private String date_of_birth;
    private String gender;
    private String contact_number;
    private String complete_address;

    public UserDetailsRequest(String email, String first_name, String middle_name, String last_name, String suffix,
                              String date_of_birth, String gender, String contact_number, String complete_address) {
        this.email = email;
        this.first_name = first_name;
        this.middle_name = middle_name;
        this.last_name = last_name;
        this.suffix = suffix;
        this.date_of_birth = date_of_birth;
        this.gender = gender;
        this.contact_number = contact_number;
        this.complete_address = complete_address;
    }
}
