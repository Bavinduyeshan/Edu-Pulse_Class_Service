package com.EduPulse.ClassService.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AttendanceResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long lectureId;
    private String lectureTitle;
    private String status;           // PRESENT, ABSENT, LATE
    private LocalDateTime checkInTime;
}