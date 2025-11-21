package com.work.IGA.Controllers.Courses;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.work.IGA.Services.CourseServices.ModuleServices;
import com.work.IGA.Utils.ModuleExceptions.ModuleDto;
import com.work.IGA.Utils.ModuleExceptions.ModuleResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/modules")
@PreAuthorize("hasRole('INSTRUCTOR')")
@RequiredArgsConstructor
public class ModulesControllers {

    private final ModuleServices moduleServices;

    @PostMapping("/create")
    public ResponseEntity<ModuleResponse> createModule(@RequestBody ModuleDto moduleDto, @RequestHeader ("Authorization") String token) {
        ModuleResponse response = moduleServices.createModule(moduleDto, token);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{moduleId}")
    public ResponseEntity<ModuleResponse> getModuleById(
        @PathVariable UUID moduleId
    ) {
        ModuleResponse response = moduleServices.getModuleById(moduleId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<ModuleResponse> getAllModulesByCourseId(
        @PathVariable UUID courseId
    ) {
        ModuleResponse response = moduleServices.getAllModulesByCourseId(courseId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/update/{moduleId}")
    public ResponseEntity<ModuleResponse> updateModule(
        @PathVariable UUID moduleId,
        @RequestBody ModuleDto moduleDto
    ) {
        ModuleResponse response = moduleServices.updateModule(moduleId, moduleDto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/delete/{moduleId}")
    public ResponseEntity<ModuleResponse> deleteModule(
        @PathVariable UUID moduleId
    ) {
        ModuleResponse response = moduleServices.deleteModule(moduleId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
 
    
}
