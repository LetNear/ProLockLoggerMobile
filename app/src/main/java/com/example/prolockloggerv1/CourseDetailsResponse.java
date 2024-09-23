package com.example.prolockloggerv1;

import java.util.List;

public class CourseDetailsResponse {
    private String instructor;
    private List<CourseDetails> course_details; // Make sure this matches the API response

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public List<CourseDetails> getCourseDetails() {
        return course_details; // This should match the field "course_details" in the response
    }

    public void setCourseDetails(List<CourseDetails> course_details) {
        this.course_details = course_details;
    }
}

