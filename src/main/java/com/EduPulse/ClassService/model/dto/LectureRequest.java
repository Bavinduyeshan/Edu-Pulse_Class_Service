package com.EduPulse.ClassService.model.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LectureRequest {
    @NotBlank String title;
    String description;
    @NotNull
    @Future LocalDateTime dateTime;
    String videoLink;
    String pdfUrl;
}