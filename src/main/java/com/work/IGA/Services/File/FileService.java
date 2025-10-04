package com.work.IGA.Services.File;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Service
public class FileService {
    private final RestTemplate restTemplate;
    private final Map<String, MediaType> mediaTypeMap;

    public FileService() {
        this.restTemplate = new RestTemplate();
        this.mediaTypeMap = new HashMap<>();
        
        // Initialize common MIME types
        // Documents
        mediaTypeMap.put("pdf", MediaType.APPLICATION_PDF);
        mediaTypeMap.put("doc", MediaType.parseMediaType("application/msword"));
        mediaTypeMap.put("docx", MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        mediaTypeMap.put("xls", MediaType.parseMediaType("application/vnd.ms-excel"));
        mediaTypeMap.put("xlsx", MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        mediaTypeMap.put("ppt", MediaType.parseMediaType("application/vnd.ms-powerpoint"));
        mediaTypeMap.put("pptx", MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation"));
        mediaTypeMap.put("txt", MediaType.TEXT_PLAIN);
        
        // Images
        mediaTypeMap.put("png", MediaType.IMAGE_PNG);
        mediaTypeMap.put("jpg", MediaType.IMAGE_JPEG);
        mediaTypeMap.put("jpeg", MediaType.IMAGE_JPEG);
        mediaTypeMap.put("gif", MediaType.IMAGE_GIF);
        mediaTypeMap.put("svg", MediaType.parseMediaType("image/svg+xml"));
        mediaTypeMap.put("webp", MediaType.parseMediaType("image/webp"));
        mediaTypeMap.put("bmp", MediaType.parseMediaType("image/bmp"));
        
        // Other formats
        mediaTypeMap.put("zip", MediaType.parseMediaType("application/zip"));
        mediaTypeMap.put("rar", MediaType.parseMediaType("application/x-rar-compressed"));
        mediaTypeMap.put("7z", MediaType.parseMediaType("application/x-7z-compressed"));
        mediaTypeMap.put("mp4", MediaType.parseMediaType("video/mp4"));
        mediaTypeMap.put("mp3", MediaType.parseMediaType("audio/mpeg"));
        
        // Default for unknown types
        mediaTypeMap.put("raw", MediaType.APPLICATION_OCTET_STREAM);
    }

    public ResponseEntity<byte[]> getFileFromCloudinary(String fileUrl) {
        try {
            if (!fileUrl.startsWith("https://res.cloudinary.com/")) {
                throw new IllegalArgumentException("Invalid Cloudinary URL");
            }

            // Configure headers for initial request
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.set("Accept", "*/*");
            
            // Make request with headers
            ResponseEntity<byte[]> cloudinaryResponse = restTemplate.exchange(
                fileUrl,
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(requestHeaders),
                byte[].class
            );

            if (cloudinaryResponse.getBody() == null) {
                return ResponseEntity.notFound().build();
            }

            // Get content type from response
            MediaType contentType = cloudinaryResponse.getHeaders().getContentType();
            if (contentType == null) {
                // Fallback to extension-based detection
                String fileExtension = getFileExtension(fileUrl).toLowerCase();
                contentType = mediaTypeMap.getOrDefault(fileExtension, MediaType.APPLICATION_OCTET_STREAM);
            }

            // Set response headers
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(contentType);
            
            // Determine content disposition based on file type
            boolean isViewable = isViewableInBrowser(contentType);
            responseHeaders.setContentDisposition(
                (isViewable ? 
                    org.springframework.http.ContentDisposition.inline() : 
                    org.springframework.http.ContentDisposition.attachment()
                )
                .filename(getFileName(fileUrl))
                .build()
            );

            // Add cache control
            responseHeaders.setCacheControl("max-age=31536000"); // 1 year
            
            return new ResponseEntity<>(cloudinaryResponse.getBody(), responseHeaders, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean isViewableInBrowser(MediaType mediaType) {
        return mediaType != null && (
            mediaType.equals(MediaType.APPLICATION_PDF) ||
            mediaType.equals(MediaType.IMAGE_PNG) ||
            mediaType.equals(MediaType.IMAGE_JPEG) ||
            mediaType.equals(MediaType.IMAGE_GIF) ||
            mediaType.toString().equals("image/svg+xml") ||
            mediaType.toString().equals("image/webp")
        );
    }

    private String getFileExtension(String url) {
        int lastDotIndex = url.lastIndexOf('.');
        return lastDotIndex > 0 ? url.substring(lastDotIndex + 1) : "";
    }

    private String getFileName(String url) {
        int lastSlashIndex = url.lastIndexOf('/');
        return lastSlashIndex > 0 ? url.substring(lastSlashIndex + 1) : "file";
    }
}