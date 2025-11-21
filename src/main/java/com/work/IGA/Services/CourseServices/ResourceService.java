package com.work.IGA.Services.CourseServices;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

import com.work.IGA.Configuration.JwtUtils;
import com.work.IGA.Configuration.SupabaseStorageService;
import com.work.IGA.Models.Courses.Modules;
import com.work.IGA.Models.Courses.ResourceSchema;
import com.work.IGA.Repositories.ModulesRepository;
import com.work.IGA.Repositories.ResourceRepository;
import com.work.IGA.Utils.ResourseException.ResourceDto;
import com.work.IGA.Utils.ResourseException.ResourceResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResourceService implements ResourceServicesImple {

    private final ResourceRepository resourceRepository;
    private final ModulesRepository moduleRepository;
    private final SupabaseStorageService supabaseStorageService;
    private final JwtUtils jwtUtils;

    @Override
    public ResourceResponse<?> createResource(ResourceDto resourceDto, String token) {
        try {
            // Validate token
            if (!jwtUtils.validateJwtToken(token)) {
                return new ResourceResponse<>("Invalid or expired token", false, null, 401);
            }

            // Get instructor email from token
            String instructorEmail = jwtUtils.getEmailFromJwtToken(token);
            if (instructorEmail == null) {
                return new ResourceResponse<>("Unauthorized: Invalid token", false, null, 401);
            }

            // Validate and find the module
            Optional<Modules> moduleOptional = moduleRepository.findById(resourceDto.getModuleId());
            if (!moduleOptional.isPresent()) {
                return new ResourceResponse<>("Module not found with ID: " + resourceDto.getModuleId(), false, null, 404);
            }

            Modules module = moduleOptional.get();

            // Verify that the instructor owns the course
            if (!module.getCourse().getInstructor().getEmail().equals(instructorEmail)) {
                return new ResourceResponse<>("Unauthorized: You can only create resources for your own courses", false, null, 403);
            }

            // Create new resource entity
            ResourceSchema newResource = new ResourceSchema();
            newResource.setTitle(resourceDto.getTitle());
            newResource.setResourceType(resourceDto.getResourceType());
            newResource.setDescription(resourceDto.getDescription());
            newResource.setModule(module);

            // Handle different resource types
            switch (resourceDto.getResourceType()) {
                case LINK:
                    if (resourceDto.getLink() == null || resourceDto.getLink().trim().isEmpty()) {
                        return new ResourceResponse<>("Link URL is required for LINK resource type", false, null, 400);
                    }
                    newResource.setLink(resourceDto.getLink());
                    newResource.setFileUrl(null);
                    break;

                case VIDEO:
                case DOCUMENT:
                case SLIDE:
                    if (resourceDto.getFileUrl() == null || resourceDto.getFileUrl().isEmpty()) {
                        return new ResourceResponse<>("File is required for " + resourceDto.getResourceType() + " resource type", false, null, 400);
                    }

                    // Upload file to Supabase storage
                    try {
                        String fileUrl = supabaseStorageService.uploadToCourseFile(
                            resourceDto.getFileUrl(),
                            "resources/" + resourceDto.getResourceType().toString().toLowerCase()
                        );
                        newResource.setFileUrl(fileUrl);
                        newResource.setLink(null);
                    } catch (IOException | InterruptedException e) {
                        return new ResourceResponse<>("Failed to upload file: " + e.getMessage(), false, null, 500);
                    }
                    break;

                default:
                    return new ResourceResponse<>("Invalid resource type", false, null, 400);
            }

            // Save the resource
            ResourceSchema savedResource = resourceRepository.save(newResource);

            return new ResourceResponse<>(
                "Resource created successfully",
                true,
                savedResource,
                201
            );

        } catch (Exception e) {
            return new ResourceResponse<>(
                "Failed to create resource: " + e.getMessage(),
                false,
                null,
                500
            );
        }
    }

    @Override
    public ResourceResponse<?> getResourceById(UUID resourceId) {
        try {
            if (resourceId == null) {
                return new ResourceResponse<>("Resource ID cannot be null", false, null, 400);
            }

            Optional<ResourceSchema> resource = resourceRepository.findById(resourceId);
            if (!resource.isPresent()) {
                return new ResourceResponse<>("Resource not found", false, null, 404);
            } 

            return new ResourceResponse<>("Resource fetched successfully", true, resource.get(), 200);

        } catch (Exception e) {
            return new ResourceResponse<>("Failed to get resource: " + e.getMessage(), false, null, 500);
        }
    }

    @Override
    public ResourceResponse<?> getAllResources() {
        try {
            List<ResourceSchema> resources = resourceRepository.findAll();

            if (resources.isEmpty()) {
                return new ResourceResponse<>(
                    "No resources found",
                    false,
                    Collections.emptyList(),
                    204
                );
            }
            
            return new ResourceResponse<>("Resources fetched successfully", true, resources, 200);

        } catch (Exception e) {
            return new ResourceResponse<>("Failed to get resources: " + e.getMessage(), false, null, 500);
        }
    }

    @Override
    public ResourceResponse<?> getAllResourcesByModuleId(UUID moduleId) {
        try {
            if (moduleId == null) {
                return new ResourceResponse<>("Module ID cannot be null", false, null, 400);
            }

            // Validate that the module exists
            Optional<Modules> moduleOptional = moduleRepository.findById(moduleId);
            if (!moduleOptional.isPresent()) {
                return new ResourceResponse<>("Module not found with ID: " + moduleId, false, null, 404);
            }

            List<ResourceSchema> resources = resourceRepository.findByModuleId(moduleId);

            if (resources.isEmpty()) {
                return new ResourceResponse<>(
                    "No resources found for this module",
                    false,
                    Collections.emptyList(),
                    204
                );
            }

            return new ResourceResponse<>("Resources fetched successfully", true, resources, 200);

        } catch (Exception e) {
            return new ResourceResponse<>("Failed to get resources by module ID: " + e.getMessage(), false, null, 500);
        }
    }

    @Override
    public ResourceResponse<?> updateResource(UUID resourceId, ResourceDto resourceDto, String token) {
        try {
            // Validate token
            if (!jwtUtils.validateJwtToken(token)) {
                return new ResourceResponse<>("Invalid or expired token", false, null, 401);
            }

            // Get instructor email from token
            String instructorEmail = jwtUtils.getEmailFromJwtToken(token);
            if (instructorEmail == null) {
                return new ResourceResponse<>("Unauthorized: Invalid token", false, null, 401);
            }

            // Find the existing resource 
            Optional<ResourceSchema> existingResource = resourceRepository.findById(resourceId);
            if (!existingResource.isPresent()) {
                return new ResourceResponse<>("Resource not found", false, null, 404);
            }

            ResourceSchema resourceToUpdate = existingResource.get();

            // Verify that the instructor owns the course
            if (!resourceToUpdate.getModule().getCourse().getInstructor().getEmail().equals(instructorEmail)) {
                return new ResourceResponse<>("Unauthorized: You can only update resources for your own courses", false, null, 403);
            }

            // Update module if provided
            if (resourceDto.getModuleId() != null && !resourceDto.getModuleId().equals(resourceToUpdate.getModule().getId())) {
                Optional<Modules> moduleOptional = moduleRepository.findById(resourceDto.getModuleId());
                if (!moduleOptional.isPresent()) {
                    return new ResourceResponse<>("Module not found with ID: " + resourceDto.getModuleId(), false, null, 404);
                }

                // Verify instructor owns the new module's course
                if (!moduleOptional.get().getCourse().getInstructor().getEmail().equals(instructorEmail)) {
                    return new ResourceResponse<>("Unauthorized: You can only move resources to your own courses", false, null, 403);
                }

                resourceToUpdate.setModule(moduleOptional.get());
            }

            // Update basic fields if provided 
            if (resourceDto.getTitle() != null && !resourceDto.getTitle().trim().isEmpty()) {
                resourceToUpdate.setTitle(resourceDto.getTitle());
            }

            if (resourceDto.getDescription() != null) {
                resourceToUpdate.setDescription(resourceDto.getDescription());
            }

            // Handle resource type specific update 
            if (resourceDto.getResourceType() != null) {
                resourceToUpdate.setResourceType(resourceDto.getResourceType());

                switch (resourceDto.getResourceType()) {
                    case LINK:     
                        if (resourceDto.getLink() != null && !resourceDto.getLink().trim().isEmpty()) {
                            resourceToUpdate.setLink(resourceDto.getLink());
                            
                            // Delete old file if switching from file-based resource
                            if (resourceToUpdate.getFileUrl() != null) {
                                try {
                                    supabaseStorageService.deleteFile(
                                        resourceToUpdate.getFileUrl(),
                                        "resources/" + resourceToUpdate.getResourceType().toString().toLowerCase()
                                    );
                                } catch (IOException | InterruptedException e) {
                                    // Log error but continue
                                    System.err.println("Failed to delete old file: " + e.getMessage());
                                }
                                resourceToUpdate.setFileUrl(null);
                            }
                        }
                        break;

                    case VIDEO:
                    case DOCUMENT:
                    case SLIDE:
                        if (resourceDto.getFileUrl() != null && !resourceDto.getFileUrl().isEmpty()) {
                            // Delete old file from Supabase 
                            if (resourceToUpdate.getFileUrl() != null) {
                                try {
                                    supabaseStorageService.deleteFile(
                                        resourceToUpdate.getFileUrl(),
                                        "resources/" + resourceToUpdate.getResourceType().toString().toLowerCase()
                                    );
                                } catch (IOException | InterruptedException e) {
                                    return new ResourceResponse<>("Failed to delete old file: " + e.getMessage(), false, null, 500);
                                }
                            }

                            // Upload new file 
                            try {
                                String fileUrl = supabaseStorageService.uploadToCourseFile(
                                    resourceDto.getFileUrl(),
                                    "resources/" + resourceDto.getResourceType().toString().toLowerCase()
                                );
                                resourceToUpdate.setFileUrl(fileUrl);
                                resourceToUpdate.setLink(null);
                            } catch (IOException | InterruptedException e) {
                                return new ResourceResponse<>("Failed to upload new file: " + e.getMessage(), false, null, 500);
                            }
                        }
                        break;
                }
            }

            // Save the updated resource
            ResourceSchema updatedResource = resourceRepository.save(resourceToUpdate);

            return new ResourceResponse<>("Resource updated successfully", true, updatedResource, 200);

        } catch (Exception e) {
            return new ResourceResponse<>("Failed to update resource: " + e.getMessage(), false, null, 500);
        }
    }

    @Override
    public ResourceResponse<?> deleteResource(UUID resourceId, String token) {
        try {
            // Validate token
            if (!jwtUtils.validateJwtToken(token)) {
                return new ResourceResponse<>("Invalid or expired token", false, null, 401);
            }

            // Get instructor email from token
            String instructorEmail = jwtUtils.getEmailFromJwtToken(token);
            if (instructorEmail == null) {
                return new ResourceResponse<>("Unauthorized: Invalid token", false, null, 401);
            }

            // Find the resource
            Optional<ResourceSchema> resource = resourceRepository.findById(resourceId);
            if (!resource.isPresent()) {
                return new ResourceResponse<>("Resource not found", false, null, 404);
            }

            ResourceSchema resourceToDelete = resource.get();

            // Verify that the instructor owns the course
            if (!resourceToDelete.getModule().getCourse().getInstructor().getEmail().equals(instructorEmail)) {
                return new ResourceResponse<>("Unauthorized: You can only delete resources from your own courses", false, null, 403);
            }

            // If resource has a file, delete it from storage
            if (resourceToDelete.getFileUrl() != null && !resourceToDelete.getFileUrl().isEmpty()) {
                try {
                    supabaseStorageService.deleteFile(
                        resourceToDelete.getFileUrl(),
                        "resources/" + resourceToDelete.getResourceType().toString().toLowerCase()
                    );
                } catch (IOException | InterruptedException e) {
                    return new ResourceResponse<>("Failed to delete file from storage: " + e.getMessage(), false, null, 500);
                }
            }

            // Delete the resource from the database
            resourceRepository.delete(resourceToDelete);

            return new ResourceResponse<>("Resource deleted successfully", true, null, 200);

        } catch (Exception e) {
            return new ResourceResponse<>("Failed to delete resource: " + e.getMessage(), false, null, 500);
        }
    }
}