package com.EduPulse.ClassService.model.dto;

import lombok.Data;

@Data
public class AttendanceRequest {
    private Long studentId;
    private String status; // "PRESENT", "ABSENT", "LATE"
    private String notes;  // optional
}