package com.example.prolockloggerv1;

public class Schedule {
    private int id;
    private String subject_name;
    private String day_of_the_week;
    private String class_start;
    private String class_end;
    private int block_id;
    private Block block; // Add this if you have a Block object
    private String year; // Add this if you have a year field

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSubjectName() {
        return subject_name;
    }

    public void setSubjectName(String subject_name) {
        this.subject_name = subject_name;
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

    public int getBlockId() {
        return block_id;
    }

    public void setBlockId(int block_id) {
        this.block_id = block_id;
    }

    public Block getBlock() { // Add this if you have a Block object
        return block;
    }

    public void setBlock(Block block) { // Add this if you have a Block object
        this.block = block;
    }

    public String getYear() { // Add this if you have a year field
        return year;
    }

    public void setYear(String year) { // Add this if you have a year field
        this.year = year;
    }

    // Nested Block class if needed
    public static class Block {
        private String block;

        // Getter and Setter
        public String getBlock() {
            return block;
        }

        public void setBlock(String block) {
            this.block = block;
        }
    }
}
