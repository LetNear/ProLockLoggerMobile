package com.example.prolockloggerv1;

public class CourseUpdateRequest {
    private String courseCode;
    private String courseDescription;
    private String schedulePassword;

    public CourseUpdateRequest(String courseCode, String courseDescription, String schedulePassword) {
        this.courseCode = courseCode;
        this.courseDescription = courseDescription;
        this.schedulePassword = schedulePassword;
    }

    // Getter and Setter for courseCode
    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    // Getter and Setter for courseDescription
    public String getCourseDescription() {
        return courseDescription;
    }

    public void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription;
    }

    // Getter and Setter for schedulePassword
    public String getSchedulePassword() {
        return schedulePassword;
    }

    public void setSchedulePassword(String schedulePassword) {
        this.schedulePassword = schedulePassword;
    }
}
