package com.work.IGA.Configuration;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final CloudinalyConf cloudinalyConf;

    public String uploadFile(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        String resourceType = "auto";  // Let Cloudinary auto-detect
        Map<String, Object> options = new HashMap<>();
        options.put("resource_type", resourceType);
        
        // For PDFs and other documents, ensure they're treated properly
        if (contentType != null) {
            if (contentType.equals("application/pdf") || 
                contentType.startsWith("application/msword") ||
                contentType.startsWith("application/vnd.openxmlformats-officedocument") ||
                contentType.startsWith("application/vnd.ms-")) {
                options.put("format", contentType.split("/")[1]);
            }
        }
        
        // Add flags to preserve the original file format
        options.put("flags", "attachment");
        
        Map uploadResult = cloudinalyConf.cloudinary().uploader().upload(
            file.getBytes(),
            options
        );
        return uploadResult.get("secure_url").toString();
    }
    
}
