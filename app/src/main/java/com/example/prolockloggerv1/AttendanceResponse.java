package com.example.prolockloggerv1;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AttendanceResponse {
    @SerializedName("instructor")
    private String instructorName;

    @SerializedName("attendance_logs")
    private List<AttendanceLog> attendanceLogs;

    public String getInstructorName() {
        return instructorName;
    }

    public List<AttendanceLog> getAttendanceLogs() {
        return attendanceLogs;
    }

    public static class AttendanceLog {
        @SerializedName("id")
        private int id;

        @SerializedName("course_name")
        private String courseName;

        @SerializedName("student_name")
        private String studentName;

        @SerializedName("year")
        private String year;

        @SerializedName("block")
        private String block;

        @SerializedName("time_in")
        private String timeIn;

        @SerializedName("time_out")
        private String timeOut;

        @SerializedName("status")
        private String status;

        public String getCourseName() {
            return courseName;
        }

        public String getStudentName() {
            return studentName;
        }

        public String getTimeIn() {
            return timeIn;
        }

        public String getTimeOut() {
            return timeOut;
        }

        public String getStatus() {
            return status;
        }
    }
}

