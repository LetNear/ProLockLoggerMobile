package com.example.prolockloggerv1;

import com.google.gson.annotations.SerializedName;

public class Schedule {
    @SerializedName("course_code")
    private String courseCode;

    @SerializedName("course_name")
    private String courseName;

    @SerializedName("day_of_the_week")
    private String dayOfTheWeek;

    @SerializedName("class_start")
    private String classStart;

    @SerializedName("class_end")
    private String classEnd;

    @SerializedName("specific_date")
    private String specificDate;

    @SerializedName("is_makeup_class")
    private int isMakeupClass;

    @SerializedName("block")
    private String block;

    @SerializedName("year")
    private String year;

    // Getters and Setters
    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDayOfTheWeek() {
        return dayOfTheWeek;
    }

    public void setDayOfTheWeek(String dayOfTheWeek) {
        this.dayOfTheWeek = dayOfTheWeek;
    }

    public String getClassStart() {
        return classStart;
    }

    public void setClassStart(String classStart) {
        this.classStart = classStart;
    }

    public String getClassEnd() {
        return classEnd;
    }

    public void setClassEnd(String classEnd) {
        this.classEnd = classEnd;
    }

    public String getSpecificDate() {
        return specificDate;
    }

    public void setSpecificDate(String specificDate) {
        this.specificDate = specificDate;
    }

    public int getIsMakeupClass() {
        return isMakeupClass;
    }

    public void setIsMakeupClass(int isMakeupClass) {
        this.isMakeupClass = isMakeupClass;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    // Combine block and year into one string
    public String getBlockYear() {
        return year + "-" + block;
    }
}
