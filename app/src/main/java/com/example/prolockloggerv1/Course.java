package com.example.prolockloggerv1;

public class Course {
    private int id;
    private String course_name;
    private String course_code;
    private String course_description;

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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
}
