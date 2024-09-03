package com.example.prolockloggerv1;

import com.google.gson.annotations.SerializedName;

public class LabSchedule {
    private int id;
    private int course_id;
    private int instructor_id;
    private int block_id;
    private String year;
    private String course_code;
    private String course_name;
    private String day_of_the_week;
    private String class_start;
    private String class_end;
    private String password;

    @SerializedName("course")
    private Course course;

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCourseId() {
        return course_id;
    }

    public void setCourseId(int course_id) {
        this.course_id = course_id;
    }

    public int getInstructorId() {
        return instructor_id;
    }

    public void setInstructorId(int instructor_id) {
        this.instructor_id = instructor_id;
    }

    public int getBlockId() {
        return block_id;
    }

    public void setBlockId(int block_id) {
        this.block_id = block_id;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getCourseCode() {
        return course_code;
    }

    public void setCourseCode(String course_code) {
        this.course_code = course_code;
    }

    public String getCourseName() {
        return course_name;
    }

    public void setCourseName(String course_name) {
        this.course_name = course_name;
    }

    public String getDayOfTheWeek() {
        return day_of_the_week;
    }

    public void setDayOfTheWeek(String day_of_the_week) {
        this.day_of_the_week = day_of_the_week;
    }

    public String getClassStart() {
        return class_start;
    }

    public void setClassStart(String class_start) {
        this.class_start = class_start;
    }

    public String getClassEnd() {
        return class_end;
    }

    public void setClassEnd(String class_end) {
        this.class_end = class_end;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }


}
