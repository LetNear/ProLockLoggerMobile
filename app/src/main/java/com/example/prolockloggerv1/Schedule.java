package com.example.prolockloggerv1;

import com.google.gson.annotations.SerializedName;

public class Schedule {
    private int id;
    @SerializedName("course_name")
    private String courseName;  // Changed from subject_name to courseName
    @SerializedName("day_of_the_week")
    private String dayOfTheWeek;
    @SerializedName("class_start")
    private String classStart;
    @SerializedName("class_end")
    private String classEnd;
    @SerializedName("block_id")
    private int blockId;
    private Block block; // Block object
    private String year; // Year field
    private String instructor; // Instructor field

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCourseName() {
        return courseName; // Corrected getter for course name
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName; // Corrected setter for course name
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

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    // Getter and Setter for instructor
    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    // Nested Block class with year field
    public static class Block {
        private String block;
        private String year;  // Year field

        // Getter and Setter for block
        public String getBlock() {
            return block;
        }

        public void setBlock(String block) {
            this.block = block;
        }

        // Getter and Setter for year
        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }
    }
}
