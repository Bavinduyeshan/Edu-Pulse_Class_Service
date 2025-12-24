package com.EduPulse.ClassService.repository;

import com.EduPulse.ClassService.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByStudentIdAndLectureId(Long studentId, Long lectureId);
    List<Attendance> findByLectureId(Long lectureId);
}