package com.work.IGA.Controllers.Courses;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.ObjectError;
import jakarta.validation.Valid;

import com.work.IGA.Services.CourseServices.ResourceService;
import com.work.IGA.Utils.ResourseException.ResourceDto;
import com.work.IGA.Utils.ResourseException.ResourceResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
public class ResourceController {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResourceResponse<?>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .findFirst()
                .orElse("Validation failed");

        ResourceResponse<?> response = new ResourceResponse<>(
                errorMessage,
                false,
                null,
                400
        );
        return ResponseEntity.badRequest().body(response);
    }

    private final ResourceService resourceService;

    @PostMapping(value = "/create", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('ROLE_INSTRUCTOR')")
    public ResponseEntity<ResourceResponse<?>> createResource(
            @Valid @ModelAttribute ResourceDto resourceDto,
            @RequestHeader("Authorization") String token) throws IOException {
        // Remove "Bearer " prefix if present
        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        
        ResourceResponse<?> response = resourceService.createResource(resourceDto, jwtToken);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{resourceId}")
    public ResponseEntity<ResourceResponse<?>> getResourceById(
            @PathVariable UUID resourceId) {
        ResourceResponse<?> response = resourceService.getResourceById(resourceId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<ResourceResponse<?>> getAllResources() {
        ResourceResponse<?> response = resourceService.getAllResources();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<ResourceResponse<?>> getAllResourcesByModuleId(
            @PathVariable UUID moduleId) {
        ResourceResponse<?> response = resourceService.getAllResourcesByModuleId(moduleId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping(value = "/update/{resourceId}", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('ROLE_INSTRUCTOR')")
    public ResponseEntity<ResourceResponse<?>> updateResource(
            @PathVariable UUID resourceId,
            @ModelAttribute ResourceDto resourceDto,
            @RequestHeader("Authorization") String token) {
        // Remove "Bearer " prefix if present
        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        
        ResourceResponse<?> response = resourceService.updateResource(resourceId, resourceDto, jwtToken);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/delete/{resourceId}")
    @PreAuthorize("hasAuthority('ROLE_INSTRUCTOR')")
    public ResponseEntity<ResourceResponse<?>> deleteResource(
            @PathVariable UUID resourceId,
            @RequestHeader("Authorization") String token) {
        // Remove "Bearer " prefix if present
        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        
        ResourceResponse<?> response = resourceService.deleteResource(resourceId, jwtToken);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}