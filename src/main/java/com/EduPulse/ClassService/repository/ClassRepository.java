package com.EduPulse.ClassService.repository;

import com.EduPulse.ClassService.model.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassRepository extends JpaRepository<ClassEntity, Long> {
    List<ClassEntity> findByLecturerId(Long lecturerId);

    List<ClassEntity> findByGradeId(Long gradeId);

    // Find classes by status
    List<ClassEntity> findByStatus(ClassEntity.ClassStatus status);

    // Find classes by grade and status
    List<ClassEntity> findByGradeIdAndStatus(Long gradeId, ClassEntity.ClassStatus status);
}