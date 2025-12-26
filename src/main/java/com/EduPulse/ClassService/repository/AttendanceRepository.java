package com.EduPulse.ClassService.repository;

import com.EduPulse.ClassService.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByStudentIdAndLectureId(Long studentId, Long lectureId);
    List<Attendance> findByLectureId(Long lectureId);

    // NEW: Find all attendance records for a specific student
    List<Attendance> findByStudentId(Long studentId);

    // NEW: Find attendance by student and status
    List<Attendance> findByStudentIdAndStatus(Long studentId, Attendance.AttendanceStatus status);

    // NEW: Find attendance by lecture and status
    List<Attendance> findByLectureIdAndStatus(Long lectureId, Attendance.AttendanceStatus status);
}