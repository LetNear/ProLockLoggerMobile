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

    public UserDetailsRequest(String userEmail, String firstName, String middleName, String lastName, String suffix, String dateOfBirth, String gender, String contactNumber, String completeAddress) {
        this.email = userEmail;
        this.first_name = firstName;
        this.middle_name = middleName;
        this.last_name = lastName;
        this.suffix = suffix;
        this.date_of_birth = dateOfBirth;
        this.gender = gender;
        this.contact_number = contactNumber;
        this.complete_address = completeAddress;
    }

    // Getters and setters...
}
