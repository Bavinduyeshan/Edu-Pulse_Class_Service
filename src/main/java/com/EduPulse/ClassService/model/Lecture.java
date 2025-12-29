package com.EduPulse.ClassService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lectures")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;

    @NotBlank
    private String title;

    private String description;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    private String videoLink;
    private String pdfUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}