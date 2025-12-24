package com.EduPulse.ClassService.repository;

import com.EduPulse.ClassService.model.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassRepository extends JpaRepository<ClassEntity, Long> {
    List<ClassEntity> findByLecturerId(Long lecturerId);
}