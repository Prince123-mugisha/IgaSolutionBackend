package com.work.IGA.Services.CourseServices;

import java.util.UUID;
import com.work.IGA.Utils.ModuleExceptions.ModuleDto;
import com.work.IGA.Utils.ModuleExceptions.ModuleResponse;

public interface ModuleServiceImpl {
    ModuleResponse createModule(ModuleDto moduleDto, String token);
    ModuleResponse getModuleById(UUID moduleId);
    ModuleResponse getAllModulesByCourseId(UUID courseId);
    ModuleResponse updateModule(UUID moduleId, ModuleDto moduleDto);
    ModuleResponse deleteModule(UUID moduleId);

    
} 
