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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    // In ClassService
    public ClassResponse getClassById(Long classId) {
        ClassEntity entity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        GradeResponse grade = userServiceClient.validateGrade(entity.getGradeId());
        UserResponse lecturer = userServiceClient.validateLecturer(entity.getLecturerId());

        return ClassResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .gradeId(grade.getId())
                .gradeName(grade.getName())
                .lecturerId(lecturer.getId())
                .lecturerName(lecturer.getFullName())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus())
                .lectureCount(entity.getLectures().size())  // ← Real count from entity
                .build();
    }

    public List<LectureResponse> getLecturesByClass(Long classId) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        return classEntity.getLectures().stream()
                .map(lecture -> LectureResponse.builder()
                        .id(lecture.getId())
                        .classId(classId)
                        .title(lecture.getTitle())
                        .description(lecture.getDescription())
                        .dateTime(lecture.getDateTime())
                        .videoLink(lecture.getVideoLink())
                        .pdfUrl(lecture.getPdfUrl())
                        .createdAt(lecture.getCreatedAt())
                        .build())
                .toList();
    }


    // In ClassService.java (add this method)
    public List<AttendanceResponse> getAttendanceForLecture(Long lectureId) {
        // Find the lecture first
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found (ID: " + lectureId + ")"));

        // Get all attendance records for this lecture
        List<Attendance> attendances = attendanceRepository.findByLectureId(lectureId);

        // Convert to DTOs with fetched student names
        return attendances.stream()
                .map(attendance -> {
                    // Fetch fresh student name via Feign (optional - or use denormalized if you have it)
                    String studentName = "Unknown Student";
                    try {
                        UserResponse student = userServiceClient.validateStudent(attendance.getStudentId());
                        studentName = student.getFullName();
                    } catch (FeignException e) {
                        // Log error if needed, fallback to unknown
                    }

                    return AttendanceResponse.builder()
                            .id(attendance.getId())
                            .studentId(attendance.getStudentId())
                            .studentName(studentName)
                            .lectureId(lecture.getId())
                            .lectureTitle(lecture.getTitle())
                            .status(attendance.getStatus().name())
                            .checkInTime(attendance.getCheckInTime())
                            .build();
                })
                .collect(Collectors.toList());
    }


    /**
     * Get all classes for a specific grade
     */
    public List<ClassResponse> getClassesByGrade(Long gradeId) {
        // Validate grade exists
        GradeResponse grade;
        try {
            grade = userServiceClient.validateGrade(gradeId);
        } catch (FeignException e) {
            throw new RuntimeException("Grade not found (ID: " + gradeId + "): " + e.getMessage());
        }

        // Find all classes for this grade
        List<ClassEntity> classes = classRepository.findByGradeId(gradeId);

        // Convert to DTOs with fetched lecturer names
        return classes.stream()
                .map(classEntity -> {
                    // Fetch lecturer name
                    String lecturerName = "Unknown Lecturer";
                    try {
                        UserResponse lecturer = userServiceClient.validateLecturer(classEntity.getLecturerId());
                        lecturerName = lecturer.getFullName();
                    } catch (FeignException e) {
                        // Log error if needed, fallback to unknown
                    }

                    return ClassResponse.builder()
                            .id(classEntity.getId())
                            .name(classEntity.getName())
                            .description(classEntity.getDescription())
                            .gradeId(grade.getId())
                            .gradeName(grade.getName())
                            .lecturerId(classEntity.getLecturerId())
                            .lecturerName(lecturerName)
                            .startDate(classEntity.getStartDate())
                            .endDate(classEntity.getEndDate())
                            .status(classEntity.getStatus())
                            .lectureCount(classEntity.getLectures() != null ? classEntity.getLectures().size() : 0)
                            .build();
                })
                .collect(Collectors.toList());
    }


    // Optional: Add this method to ClassService.java to get classes by lecturer

    /**
     * Get all classes taught by a specific lecturer
     */
    public List<ClassResponse> getClassesByLecturer(Long lecturerId) {
        // Validate lecturer exists
        UserResponse lecturer;
        try {
            lecturer = userServiceClient.validateLecturer(lecturerId);
        } catch (FeignException e) {
            throw new RuntimeException("Lecturer not found (ID: " + lecturerId + "): " + e.getMessage());
        }

        // Find all classes for this lecturer
        List<ClassEntity> classes = classRepository.findByLecturerId(lecturerId);

        // Convert to DTOs with fetched grade names
        return classes.stream()
                .map(classEntity -> {
                    // Fetch grade name
                    String gradeName = "Unknown Grade";
                    try {
                        GradeResponse grade = userServiceClient.validateGrade(classEntity.getGradeId());
                        gradeName = grade.getName();
                    } catch (FeignException e) {
                        // Log error if needed, fallback to unknown
                    }

                    return ClassResponse.builder()
                            .id(classEntity.getId())
                            .name(classEntity.getName())
                            .description(classEntity.getDescription())
                            .gradeId(classEntity.getGradeId())
                            .gradeName(gradeName)
                            .lecturerId(lecturer.getId())
                            .lecturerName(lecturer.getFullName())
                            .startDate(classEntity.getStartDate())
                            .endDate(classEntity.getEndDate())
                            .status(classEntity.getStatus())
                            .lectureCount(classEntity.getLectures() != null ? classEntity.getLectures().size() : 0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ========== Get all classes ==========
    public List<ClassResponse> getAllClasses() {
        List<ClassEntity> classes = classRepository.findAll();

        return classes.stream()
                .map(classEntity -> buildClassResponse(classEntity))
                .collect(Collectors.toList());
    }

    // ========== Get a single lecture by ID ==========
    public LectureResponse getLectureById(Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found (ID: " + lectureId + ")"));

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

    // ========== Update class details ==========
    public ClassResponse updateClass(Long classId, ClassRequest request, Long lecturerId) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found (ID: " + classId + ")"));

        // Verify the lecturer owns this class
        if (!classEntity.getLecturerId().equals(lecturerId)) {
            throw new RuntimeException("Unauthorized: You can only update your own classes");
        }

        // Validate grade if changed
        if (!classEntity.getGradeId().equals(request.getGradeId())) {
            try {
                userServiceClient.validateGrade(request.getGradeId());
            } catch (FeignException e) {
                throw new RuntimeException("Grade not found (ID: " + request.getGradeId() + "): " + e.getMessage());
            }
        }

        // Update fields
        classEntity.setName(request.getName());
        classEntity.setDescription(request.getDescription());
        classEntity.setGradeId(request.getGradeId());
        classEntity.setStartDate(request.getStartDate());
        classEntity.setEndDate(request.getEndDate());

        classEntity = classRepository.save(classEntity);

        return buildClassResponse(classEntity);
    }

    // ========== Delete/Archive a class ==========
    public void deleteClass(Long classId) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found (ID: " + classId + ")"));

        // Option 1: Soft delete (archive) - set status to ARCHIVED
        classEntity.setStatus(ClassEntity.ClassStatus.ARCHIVED);
        classRepository.save(classEntity);

        // Option 2: Hard delete (uncomment if you prefer)
        // classRepository.delete(classEntity);
    }

    // ========== Get attendance for a specific student ==========
    public List<AttendanceResponse> getAttendanceForStudent(Long studentId) {
        // Validate student exists
        UserResponse student;
        try {
            student = userServiceClient.validateStudent(studentId);
        } catch (FeignException e) {
            throw new RuntimeException("Student not found (ID: " + studentId + "): " + e.getMessage());
        }

        // Get all attendance records for this student
        List<Attendance> attendances = attendanceRepository.findByStudentId(studentId);

        // Convert to DTOs
        return attendances.stream()
                .map(attendance -> {
                    Lecture lecture = attendance.getLecture();
                    return AttendanceResponse.builder()
                            .id(attendance.getId())
                            .studentId(attendance.getStudentId())
                            .studentName(student.getFullName())
                            .lectureId(lecture.getId())
                            .lectureTitle(lecture.getTitle())
                            .status(attendance.getStatus().name())
                            .checkInTime(attendance.getCheckInTime())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ========== Helper method to build ClassResponse ==========
    private ClassResponse buildClassResponse(ClassEntity classEntity) {
        // Fetch grade name
        String gradeName = "Unknown Grade";
        try {
            GradeResponse grade = userServiceClient.validateGrade(classEntity.getGradeId());
            gradeName = grade.getName();
        } catch (FeignException e) {
            // Log error if needed
        }

        // Fetch lecturer name
        String lecturerName = "Unknown Lecturer";
        try {
            UserResponse lecturer = userServiceClient.validateLecturer(classEntity.getLecturerId());
            lecturerName = lecturer.getFullName();
        } catch (FeignException e) {
            // Log error if needed
        }

        return ClassResponse.builder()
                .id(classEntity.getId())
                .name(classEntity.getName())
                .description(classEntity.getDescription())
                .gradeId(classEntity.getGradeId())
                .gradeName(gradeName)
                .lecturerId(classEntity.getLecturerId())
                .lecturerName(lecturerName)
                .startDate(classEntity.getStartDate())
                .endDate(classEntity.getEndDate())
                .status(classEntity.getStatus())
                .lectureCount(classEntity.getLectures() != null ? classEntity.getLectures().size() : 0)
                .build();
    }

    // ========== Update Lecture ==========
    public LectureResponse updateLecture(Long lectureId, LectureRequest request, Long lecturerId) {

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found (ID: " + lectureId + ")"));

        ClassEntity classEntity = lecture.getClassEntity();

        // 🔐 Ensure lecturer owns the class
        if (!classEntity.getLecturerId().equals(lecturerId)) {
            throw new RuntimeException("Unauthorized: You can only update lectures of your own classes");
        }

        // Update fields
        lecture.setTitle(request.getTitle());
        lecture.setDescription(request.getDescription());
        lecture.setDateTime(request.getDateTime());
        lecture.setVideoLink(request.getVideoLink());
        lecture.setPdfUrl(request.getPdfUrl());

        lecture = lectureRepository.save(lecture);

        return LectureResponse.builder()
                .id(lecture.getId())
                .classId(classEntity.getId())
                .title(lecture.getTitle())
                .description(lecture.getDescription())
                .dateTime(lecture.getDateTime())
                .videoLink(lecture.getVideoLink())
                .pdfUrl(lecture.getPdfUrl())
                .createdAt(lecture.getCreatedAt())
                .build();
    }

    // ========== Delete Lecture ==========
    public void deleteLecture(Long lectureId, Long lecturerId) {

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found (ID: " + lectureId + ")"));

        ClassEntity classEntity = lecture.getClassEntity();

        // 🔐 Ensure lecturer owns the class
        if (!classEntity.getLecturerId().equals(lecturerId)) {
            throw new RuntimeException("Unauthorized: You can only delete lectures of your own classes");
        }

        // Hard delete (recommended for lectures)
        lectureRepository.delete(lecture);
    }


    // ========== Get attendance of logged-in student for a lecture ==========
    public AttendanceResponse getMyAttendanceForLecture(Long lectureId, Long studentId) {

        // 🔍 Validate lecture exists
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found (ID: " + lectureId + ")"));

        // 🔍 Validate student exists
        UserResponse student;
        try {
            student = userServiceClient.validateStudent(studentId);
        } catch (FeignException e) {
            throw new RuntimeException("Student not found (ID: " + studentId + ")");
        }

        // 🔍 Find attendance record
        Attendance attendance = attendanceRepository
                .findByStudentIdAndLectureId(studentId, lectureId)
                .orElseThrow(() -> new RuntimeException("Attendance not marked for this lecture"));

        // ✅ Build response
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .studentId(studentId)
                .studentName(student.getFullName())
                .lectureId(lecture.getId())
                .lectureTitle(lecture.getTitle())
                .status(attendance.getStatus().name())
                .checkInTime(attendance.getCheckInTime())
                .build();
    }

    // ========== Get total lectures count ==========
    public Long getTotalLecturesCount() {
        return lectureRepository.count();
    }


}