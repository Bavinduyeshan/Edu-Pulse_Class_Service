package com.EduPulse.ClassService.service;


import com.EduPulse.ClassService.model.dto.GradeResponse;
import com.EduPulse.ClassService.model.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${USER_SERVICE_URL:user-service-url}")

public interface UserServiceClient {



    @GetMapping("/validate/grade/{gradeId}")
    GradeResponse validateGrade(@PathVariable Long gradeId);

    @GetMapping("/lecturers/validate/{lecturerId}")
    UserResponse validateLecturer(@PathVariable Long lecturerId);
    @GetMapping("/validate/{gradeId}")
    UserResponse validateStudent(@PathVariable Long studentId);


    // NEW - for UserDetailsService
    @GetMapping("/username/{username}")
    UserResponse getUserByUsername(@PathVariable("username") String username);
}
