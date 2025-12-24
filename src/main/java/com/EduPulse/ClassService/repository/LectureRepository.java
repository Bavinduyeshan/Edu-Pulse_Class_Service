package com.EduPulse.ClassService.repository;

import com.EduPulse.ClassService.model.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    List<Lecture> findByClassEntityId(Long classId);
}