package com.EduPulse.ClassService.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ClassRequest {
    @NotBlank String name;
    String description;
    @NotNull Long gradeId;
    @NotNull LocalDate startDate;
    @NotNull LocalDate endDate;
}