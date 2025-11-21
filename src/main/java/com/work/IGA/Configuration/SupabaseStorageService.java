package com.work.IGA.Configuration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;

@Service
public class SupabaseStorageService {

    private static final Logger logger = Logger.getLogger(SupabaseStorageService.class.getName());

    @Value("${supabase.url}")
    private String supabaseUrl;
    
    @Value("${supabase.api.key}")
    private String supabaseKey;

    @Value("${supabase.bucket.instructors}")
    private String instructorsBucket;

    @Value("${supabase.bucket.courses}")
    private String coursesBucket;

    private final HttpClient httpClient = HttpClient.newHttpClient();


    /**
     * Uploads a file to Supabase Storage
     * @param file The file to upload
     * @param bucketName The bucket to upload to
     * @param folder The folder within the bucket
     * @return The public URL of the uploaded file
     * @throws IOException If there's an error handling the file
     * @throws InterruptedException If the upload is interrupted
     */

     public String uploadFile(MultipartFile file, String bucketName, String folder) throws IOException, InterruptedException {
               if (file == null) {
                      logger.severe("uploadFile: MultipartFile is null");
                      throw new IOException("MultipartFile is null");
               }
               if (file.isEmpty()) {
                      logger.severe("uploadFile: MultipartFile is empty");
                      throw new IOException("file is Empty");
               }

               String fileName = generatedFileName(file, folder);
               String uploadUrl = buildUploadUrl(bucketName, fileName);

               logger.info(String.format("Attempting to upload file: %s to bucket: %s in folder: %s", file.getOriginalFilename(), bucketName, folder));

               HttpRequest request;
               try {
                      request = buildHttpRequest(uploadUrl, file);
               } catch (Exception ex) {
                      logger.severe("Error building HTTP request for file upload: " + ex.getMessage());
                      throw new IOException("Error building HTTP request: " + ex.getMessage(), ex);
               }

               HttpResponse<String> response;
               try {
                      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
               } catch (Exception ex) {
                      logger.severe("Error sending HTTP request for file upload: " + ex.getMessage());
                      throw new IOException("Error sending HTTP request: " + ex.getMessage(), ex);
               }

               if (response.statusCode() == 200 || response.statusCode() == 201) {
                      String publicUrl = buildPublicUrl(bucketName, fileName);
                      logger.info(String.format("File uploaded successfully. Public URL: %s", publicUrl));
                      return publicUrl;
               } else {
                      logger.severe(String.format("Failed to upload file. Status Code: %d, Response: %s", response.statusCode(), response.body()));
                      throw new IOException("Failed to upload file to Supabase Storage : " + response.body());
               }
     }

      /**
     * Uploads a file to the instructor bucket
     * @param file The file to upload
     * @param folder The folder within the instructor bucket
     * @return The public URL of the uploaded file
     */


     // Method to upload file to Instructor bucket
     public String uploadToInstructorFile(MultipartFile file, String folder) throws IOException, InterruptedException {
                     logger.info(String.format("Uploading instructor file to folder: %s", folder));
                     if (file == null) {
                            logger.severe("uploadToInstructorFile: MultipartFile is null");
                            throw new IOException("MultipartFile is null");
                     }
                     if (file.isEmpty()) {
                            logger.severe("uploadToInstructorFile: MultipartFile is empty");
                            throw new IOException("file is Empty");
                     }
                     try {
                            return uploadFile(file, instructorsBucket, folder);
                     } catch (Exception ex) {
                            logger.severe("Exception in uploadToInstructorFile: " + ex.getMessage());
                            throw ex;
                     }

     }

       /**
     * Uploads a file to the course bucket
     * @param file The file to upload
     * @param folder The folder within the course bucket
     * @return The public URL of the uploaded file
     */

     public String uploadToCourseFile(MultipartFile file, String folder) throws IOException, InterruptedException {
        logger.fine(String.format("Uploading course file to folder: %s", folder));
        return uploadFile(file, coursesBucket, folder);
     }

    private String generatedFileName(MultipartFile file , String folder) {
              String cleanFileName = file.getOriginalFilename()
                            .replaceAll("\\s+", "-")
                            .replaceAll("[()']", "")
                            .replaceAll("[^a-zA-Z0-9.-]", "-" );

              return folder + "/" + UUID.randomUUID() + "-" + cleanFileName;
    }
   
   private String buildUploadUrl(String bucketName, String fileName) {
                 try {
                     String encodedBucket = URLEncoder.encode(bucketName, StandardCharsets.UTF_8.toString());
                     String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
                     return supabaseUrl + "/storage/v1/object/" + encodedBucket + "/" + encodedFileName;
                 }
                 catch (UnsupportedEncodingException e) {
                     throw new RuntimeException("Error encoding file name", e);
                 }
   }

   private String buildPublicUrl(String bucketName, String fileName) {
            try {
                String encodedBucket = URLEncoder.encode(bucketName, StandardCharsets.UTF_8.toString());
                String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
                return supabaseUrl + "/storage/v1/object/public/" + encodedBucket + "/" + encodedFileName;
            }
            catch( UnsupportedEncodingException e ) {
                   throw new RuntimeException("Error encoding public URL", e);
            }
   }

   private HttpRequest buildHttpRequest(String url, MultipartFile file) throws IOException {
         return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer " + supabaseKey)
                .header("Content-Type", file.getContentType())
                .PUT(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
                .build();
   }

   /**
 * Deletes a file from Supabase Storage
 * @param fileUrl The public URL of the file to delete
 * @param folder The folder within the bucket
 * @return true if deletion was successful
 * @throws IOException If there's an error deleting the file
 * @throws InterruptedException If the deletion is interrupted
 */

  public boolean deleteFile(String fileUrl, String folder) throws IOException, InterruptedException {
       if (fileUrl == null || fileUrl.isEmpty()) {
              throw new IOException("File URL is null or empty");

       }

       // Exstract bucket name and file path from URL 
       String[] urlParts = fileUrl.split("/storage/v1/object/public/");
       if (urlParts.length != 2 ) {
              throw new IOException("Invalid file URL format");
       }

       String [] urlPaths = urlParts[1].split("/", 2);
       if (urlPaths.length != 2) {
              throw new IOException("Invalid file URL format");              
       }

       String bucketName = urlPaths[0];
       String filePath = urlPaths[1];

       // Build delete Url 
       String deleteUrl = buildDeleteUrl(bucketName, filePath);

       logger.fine(String.format("Attempting to delete file: %s from bucket: %s", filePath, bucketName));
       
       // Build delete request
       HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create(deleteUrl))
              .header("apikey", supabaseKey)
              .header("Authorization", "Bearer " + supabaseKey)
              .DELETE()
              .build();
       
       // Send delete request 
       HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

       if (response.statusCode() == 200 || response.statusCode() == 204) {
              logger.info(String.format("File deleted successfully: %s", fileUrl));
              return true;
       }
       else {
              logger.severe(String.format("Failed to delete file. Status Code: %d, Response: %s", response.statusCode(), response.body()));
              throw new IOException("Failed to delete file from Supabase Storage : " + response.body());
       }
   }

   private String buildDeleteUrl(String bucketName, String filePath) {
              try {
                     String encodedBucket  = URLEncoder.encode(bucketName, StandardCharsets.UTF_8.toString());
                     String encodedFilePath = URLEncoder.encode(filePath, StandardCharsets.UTF_8.toString());
                     return supabaseUrl + "/storage/v1/object/" + encodedBucket + "/" + encodedFilePath;

              }
              catch( UnsupportedEncodingException e ) {
                     throw new RuntimeException("Error encoding delete URL", e);
              }
   }
}
