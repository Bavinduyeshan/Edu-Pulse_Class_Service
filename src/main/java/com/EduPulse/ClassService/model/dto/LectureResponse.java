package com.EduPulse.ClassService.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LectureResponse {
    private Long id;
    private Long classId;
    private String title;
    private String description;
    private LocalDateTime dateTime;
    private String videoLink;
    private String pdfUrl;
    private LocalDateTime createdAt;
}