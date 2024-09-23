package com.example.prolockloggerv1;

public class CourseDetails {
    private String course_name;
    private String course_code;
    private String course_description;
    private String schedule_password;
    private String year;
    private String block;

    public String getCourseName() {
        return course_name;
    }

    public void setCourseName(String course_name) {
        this.course_name = course_name;
    }

    public String getCourseCode() {
        return course_code;
    }

    public void setCourseCode(String course_code) {
        this.course_code = course_code;
    }

    public String getCourseDescription() {
        return course_description;
    }

    public void setCourseDescription(String course_description) {
        this.course_description = course_description;
    }

    public String getSchedulePassword() {
        return schedule_password;
    }

    public void setSchedulePassword(String schedule_password) {
        this.schedule_password = schedule_password;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }
}
