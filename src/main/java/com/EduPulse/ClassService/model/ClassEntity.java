package com.EduPulse.ClassService.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// ClassEntity.java
@Entity
@Table(name = "classes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    private String description;

    @Column(name = "grade_id", nullable = false)
    private Long gradeId;

    @Column(name = "lecturer_id", nullable = false)
    private Long lecturerId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private ClassStatus status = ClassStatus.ACTIVE;

    @OneToMany(mappedBy = "classEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lecture> lectures = new ArrayList<>();

    public enum ClassStatus {
        ACTIVE, ARCHIVED, CANCELLED
    }
}