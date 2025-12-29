package com.EduPulse.ClassService.model.dto;

import lombok.Data;

//feign
@Data
public class GradeResponse {



    private Long id;

    private String name;

    private String description;


    public GradeResponse(Long id, String name, String description) {
        this.id=id;
        this.name=name;
        this.description=description;
    }
}
