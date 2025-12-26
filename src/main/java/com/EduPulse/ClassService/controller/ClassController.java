package com.EduPulse.ClassService.controller;

import com.EduPulse.ClassService.model.ClassEntity;
import com.EduPulse.ClassService.model.dto.*;
import com.EduPulse.ClassService.service.ClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})

public class ClassController {

    private final ClassService classService;

    // ========== Create a new Class ==========
    // Only lecturers can create classes
    @PostMapping
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<ClassResponse> createClass(
            @Valid @RequestBody ClassRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long lecturerId) {  // From JWT filter

        if (lecturerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null); // Or throw exception if you prefer
        }

        ClassResponse response = classService.createClass(request, lecturerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========== Get a single class by ID ==========
    @GetMapping("/classes/{classId}")
    public ResponseEntity<ClassResponse> getClassById(@PathVariable Long classId) {
        return ResponseEntity.ok(classService.getClassById(classId));
    }

    // ========== Schedule a new Lecture in a class ==========
    // Only the class lecturer should be able to do this
    @PostMapping("/{classId}/lectures")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<LectureResponse> scheduleLecture(
            @PathVariable Long classId,
            @Valid @RequestBody LectureRequest request,
            @RequestHeader("X-User-Id") Long lecturerId) {

        // Optional: Add check if this lecturer owns the class
        LectureResponse response = classService.scheduleLecture(classId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========== Mark attendance for a student in a lecture ==========
    // Can be called by lecturer or student (depending on your flow)
    @PostMapping("/lectures/{lectureId}/attendance")
    @PreAuthorize("hasRole('LECTURER') or hasRole('STUDENT')")
    public ResponseEntity<AttendanceResponse> markAttendance(
            @PathVariable Long lectureId,
            @Valid @RequestBody AttendanceRequest request,
            @RequestHeader("X-User-Id") Long currentUserId) {

        // Optional: If student is marking their own attendance, validate studentId == currentUserId
        AttendanceResponse response = classService.markAttendance(lectureId, request);
        return ResponseEntity.ok(response);
    }

    // ========== Optional: Get all lectures for a class ==========
    @GetMapping("/{classId}/lectures")
    public ResponseEntity<List<LectureResponse>> getLecturesByClass(@PathVariable Long classId) {
        List<LectureResponse> response = classService.getLecturesByClass(classId);
        return ResponseEntity.ok(response);
    }

    // ========== Optional: Get attendance for a lecture ==========
    // In ClassController.java - replace the placeholder
    @GetMapping("/lectures/{lectureId}/attendance")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceForLecture(@PathVariable Long lectureId) {
        List<AttendanceResponse> response = classService.getAttendanceForLecture(lectureId);
        return ResponseEntity.ok(response);
    }


    // ========== NEW: Get all classes for a specific grade ==========
    @GetMapping("/grade/{gradeId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'LECTURER', 'ADMIN')")
    public ResponseEntity<List<ClassResponse>> getClassesByGrade(@PathVariable Long gradeId) {
        List<ClassResponse> response = classService.getClassesByGrade(gradeId);
        return ResponseEntity.ok(response);
    }

    // ========== NEW: Get all classes (with optional filters) ==========
    @GetMapping
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    public ResponseEntity<List<ClassResponse>> getAllClasses() {
        List<ClassResponse> response = classService.getAllClasses();
        return ResponseEntity.ok(response);
    }

    // ========== NEW: Get a single lecture by ID ==========
    @GetMapping("/lectures/{lectureId}")
    public ResponseEntity<LectureResponse> getLectureById(@PathVariable Long lectureId) {
        LectureResponse response = classService.getLectureById(lectureId);
        return ResponseEntity.ok(response);
    }

    // ========== NEW: Get attendance for a specific student ==========
    @GetMapping("/students/{studentId}/attendance")
    @PreAuthorize("hasAnyRole('STUDENT', 'LECTURER', 'ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceForStudent(
            @PathVariable Long studentId,
            @RequestHeader("X-User-Id") Long currentUserId) {

        List<AttendanceResponse> response = classService.getAttendanceForStudent(studentId);
        return ResponseEntity.ok(response);
    }

    // ========== NEW: Update class details ==========
    @PutMapping("/{classId}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<ClassResponse> updateClass(
            @PathVariable Long classId,
            @Valid @RequestBody ClassRequest request,
            @RequestHeader("X-User-Id") Long lecturerId) {

        ClassResponse response = classService.updateClass(classId, request, lecturerId);
        return ResponseEntity.ok(response);
    }

    // ========== NEW: Delete/Archive a class ==========
    @DeleteMapping("/{classId}")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClass(
            @PathVariable Long classId,
            @RequestHeader("X-User-Id") Long userId) {

        classService.deleteClass(classId);
        return ResponseEntity.noContent().build();
    }


    // ========== NEW: Get all classes for a lecturer ==========
    @GetMapping("/lecturer/{lecturerId}")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<List<ClassResponse>> getClassesByLecturer(
            @PathVariable Long lecturerId,
            @RequestHeader("X-User-Id") Long currentUserId) {

        // Optional security check:
        // Lecturer can only fetch their own classes
        if (!currentUserId.equals(lecturerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ClassResponse> response = classService.getClassesByLecturer(lecturerId);
        return ResponseEntity.ok(response);
    }

    // ========== NEW: Update a lecture ==========
    @PutMapping("/lectures/{lectureId}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<LectureResponse> updateLecture(
            @PathVariable Long lectureId,
            @Valid @RequestBody LectureRequest request,
            @RequestHeader("X-User-Id") Long lecturerId) {

        LectureResponse response = classService.updateLecture(lectureId, request, lecturerId);
        return ResponseEntity.ok(response);
    }

    // ========== NEW: Delete a lecture ==========
    @DeleteMapping("/lectures/{lectureId}")
    @PreAuthorize("hasRole('LECTURER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLecture(
            @PathVariable Long lectureId,
            @RequestHeader("X-User-Id") Long lecturerId) {

        classService.deleteLecture(lectureId, lecturerId);
        return ResponseEntity.noContent().build();
    }


}