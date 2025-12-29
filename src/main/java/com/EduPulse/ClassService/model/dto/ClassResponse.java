package com.EduPulse.ClassService.model.dto;

import com.EduPulse.ClassService.model.ClassEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ClassResponse {
    Long id;
    String name;
    String description;
    Long gradeId;
    String gradeName;
    Long lecturerId;
    String lecturerName;
    LocalDate startDate;
    LocalDate endDate;
    ClassEntity.ClassStatus status;
    int lectureCount;
}