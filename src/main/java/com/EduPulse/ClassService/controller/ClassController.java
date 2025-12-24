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
    @GetMapping("/{classId}")
    public ResponseEntity<Optional<ClassEntity>> getClassById(@PathVariable Long classId) {
        // You can add authorization later (e.g. only enrolled students/lecturer)
        Optional<ClassEntity> response = classService.getClassById(classId); // Add this method if needed
        return ResponseEntity.ok(response);
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
        // Implement in service if needed
        // List<LectureResponse> response = classService.getLecturesByClass(classId);
        // return ResponseEntity.ok(response);
        return ResponseEntity.ok(List.of()); // Placeholder
    }

    // ========== Optional: Get attendance for a lecture ==========
    @GetMapping("/lectures/{lectureId}/attendance")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceForLecture(@PathVariable Long lectureId) {
        // Implement in service if needed
        return ResponseEntity.ok(List.of()); // Placeholder
    }
}