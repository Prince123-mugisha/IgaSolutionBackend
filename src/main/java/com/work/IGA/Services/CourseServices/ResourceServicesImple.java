package com.work.IGA.Services.CourseServices;

import java.io.IOException;
import java.util.UUID;
import com.work.IGA.Utils.ResourseException.ResourceDto;
import com.work.IGA.Utils.ResourseException.ResourceResponse;

public interface  ResourceServicesImple {
    ResourceResponse<?> createResource(ResourceDto resourceDto, String token) throws IOException;
    ResourceResponse<?> getResourceById(UUID resourceId);
    ResourceResponse<?> getAllResources();
    ResourceResponse<?> getAllResourcesByModuleId(UUID moduleId);
    ResourceResponse<?> updateResource(UUID resourceId, ResourceDto resourceDto, String token);
    ResourceResponse<?> deleteResource(UUID resourceId, String token);
}
