package com.example.prolockloggerv1;

import java.util.List;

public class ScheduleCountResponse {
    private List<Schedule> data;  // List to hold Schedule objects
    private int total;            // Total number of schedules (if available)
    private int totalPages;
    private int scheduleCount; // Total number of pages (if available)

    // Getter for schedules
    public List<Schedule> getData() {
        return data;
    }

    // Setter for schedules
    public void setData(List<Schedule> data) {
        this.data = data;
    }

    // Getter for total items
    public int getTotal() {
        return total;
    }

    // Setter for total items
    public void setTotal(int total) {
        this.total = total;
    }

    // Getter for total pages
    public int getTotalPages() {
        return totalPages;
    }

    // Setter for total pages
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    public int getScheduleCount() {
        return scheduleCount;
    }

    public void setScheduleCount(int scheduleCount) {
        this.scheduleCount = scheduleCount;
    }
}
