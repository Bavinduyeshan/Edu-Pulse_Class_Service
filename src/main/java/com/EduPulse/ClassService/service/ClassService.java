package com.EduPulse.ClassService.service;

import com.EduPulse.ClassService.model.*;
import com.EduPulse.ClassService.model.dto.*;
import com.EduPulse.ClassService.repository.AttendanceRepository;
import com.EduPulse.ClassService.repository.ClassRepository;
import com.EduPulse.ClassService.repository.LectureRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final LectureRepository lectureRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserServiceClient userServiceClient;  // Your Feign client

    // ========== Create Class ==========
    public ClassResponse createClass(ClassRequest request, Long lecturerId) {
        // Validate & fetch lecturer data
        UserResponse lecturer;
        try {
            lecturer = userServiceClient.validateLecturer(lecturerId);
        } catch (FeignException e) {
            throw new RuntimeException("Invalid or unauthorized lecturer (ID: " + lecturerId + "): " + e.getMessage());
        }

        // Validate & fetch grade data
        GradeResponse grade;
        try {
            grade = userServiceClient.validateGrade(request.getGradeId());
        } catch (FeignException e) {
            throw new RuntimeException("Grade not found (ID: " + request.getGradeId() + "): " + e.getMessage());
        }

        // Build and save ClassEntity (only IDs)
        ClassEntity classEntity = ClassEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .gradeId(grade.getId())
                .lecturerId(lecturer.getId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        classEntity = classRepository.save(classEntity);

        // Build response with dynamically fetched names
        return ClassResponse.builder()
                .id(classEntity.getId())
                .name(classEntity.getName())
                .description(classEntity.getDescription())
                .gradeId(grade.getId())
                .gradeName(grade.getName())             // ← fetched fresh from UserService
                .lecturerId(lecturer.getId())
                .lecturerName(lecturer.getFullName())   // ← fetched fresh from UserService
                .startDate(classEntity.getStartDate())
                .endDate(classEntity.getEndDate())
                .status(classEntity.getStatus())
                .build();
    }

    // ========== Schedule Lecture ==========
    public LectureResponse scheduleLecture(Long classId, LectureRequest request) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found (ID: " + classId + ")"));

        Lecture lecture = Lecture.builder()
                .classEntity(classEntity)
                .title(request.getTitle())
                .description(request.getDescription())
                .dateTime(request.getDateTime())
                .videoLink(request.getVideoLink())
                .pdfUrl(request.getPdfUrl())
                .build();

        lecture = lectureRepository.save(lecture);

        return LectureResponse.builder()
                .id(lecture.getId())
                .classId(lecture.getClassEntity().getId())
                .title(lecture.getTitle())
                .description(lecture.getDescription())
                .dateTime(lecture.getDateTime())
                .videoLink(lecture.getVideoLink())
                .pdfUrl(lecture.getPdfUrl())
                .createdAt(lecture.getCreatedAt())
                .build();
    }

    // ========== Mark Attendance ==========
    public AttendanceResponse markAttendance(Long lectureId, AttendanceRequest request) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found (ID: " + lectureId + ")"));

        // Validate & fetch student data
        UserResponse student;
        try {
            student = userServiceClient.validateStudent(request.getStudentId());
        } catch (FeignException e) {
            throw new RuntimeException("Student not found (ID: " + request.getStudentId() + "): " + e.getMessage());
        }

        // Check if already marked
        Optional<Attendance> existing = attendanceRepository.findByStudentIdAndLectureId(
                request.getStudentId(), lectureId);

        Attendance attendance = existing.orElseGet(() -> Attendance.builder()
                .studentId(request.getStudentId())
                .lecture(lecture)
                .build());

        attendance.setStatus(Attendance.AttendanceStatus.valueOf(request.getStatus().toUpperCase()));
        attendance.setCheckInTime(LocalDateTime.now());

        attendance = attendanceRepository.save(attendance);

        // Build response with fetched student name
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .studentId(attendance.getStudentId())
                .studentName(student.getFullName())  // ← fetched fresh from UserService
                .lectureId(lecture.getId())
                .lectureTitle(lecture.getTitle())
                .status(attendance.getStatus().name())
                .checkInTime(attendance.getCheckInTime())
                .build();
    }

    public Optional<ClassEntity> getClassById(Long classId) {
        return classRepository.findById(classId);
    }
}