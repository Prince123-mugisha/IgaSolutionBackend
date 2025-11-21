package com.work.IGA.Services.CourseServices;

import java.time.LocalDateTime;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties.Http;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.work.IGA.Configuration.JwtUtils;
import com.work.IGA.Models.Courses.CoursesSchema;
import com.work.IGA.Models.Courses.Enrollment;
import com.work.IGA.Models.Courses.Payment;
import com.work.IGA.Models.Courses.PaymentStatus;
import com.work.IGA.Models.Courses.ProgressEnum;
import com.work.IGA.Models.Users.UserSchema;
import com.work.IGA.Repositories.CourseRepo;
import com.work.IGA.Repositories.EnrollmentRepository;
import com.work.IGA.Repositories.PaymentRepository;
import com.work.IGA.Repositories.UserRepository;
import com.work.IGA.Utils.PaymentUtils.PaymentException;
import com.work.IGA.Utils.PaymentUtils.PaymentRequest;
import com.work.IGA.Utils.PaymentUtils.PaymentResponse;
import com.work.IGA.Utils.PaymentUtils.PaymentVerificationRequest;

import lombok.RequiredArgsConstructor;
 
@Service 
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final CourseRepo courseRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final JwtUtils jwtUtils;
    private final EnrollmentRepository enrollmentRepository;


    @Value("${flutterwave.api.base.url}")
    private String flutterwaveBaseUrl;

    @Value("${flutterwave.api.secret.key}")
    private String flutterwaveSecretKey;

    @Value("${flutterwave.api.public.key}")
    private String flutterwavePublicKey;

    @Value("${flutterwave.api.callback.url}")
    private String flutterwaveCallbackUrl;

    @Override
    public PaymentResponse initiatePayment(PaymentRequest paymentRequest) throws PaymentException {
        try  {
             
            // Extract and validate token 
            String token = paymentRequest.getToken();
            if (token == null || token.trim().isEmpty()) {
                throw PaymentException.paymentGatewayError("Authentication token is required");
            }

            // Remove Bearer prefix if present 
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Validate token 
            if (!jwtUtils.validateJwtToken(token)) {
                  throw PaymentException.paymentGatewayError("Invalid or expired authentication token");
            }

            // Extract user email from token and find user 
            String userEmail = jwtUtils.getEmailFromJwtToken(token);
            Optional<UserSchema> user = userRepository.findByEmail(userEmail);
            if (user.isEmpty()) {
                throw PaymentException.paymentGatewayError("User not found with email: " + userEmail);
            }

            // Validate course  exists
            Optional<CoursesSchema> course = courseRepository.findById(paymentRequest.getCourseId());
            if (course.isEmpty()) {
                throw PaymentException.paymentGatewayError("Course not found with ID: " + paymentRequest.getCourseId());
            }

            // Check if user has already paid for this course 
            if (paymentRepository.existsByStudentIdAndCourseIdAndPaymentStatus(
                user.get().getId(),
                course.get().getId(),
                PaymentStatus.COMPLETED
            )) {
                throw new PaymentException("ALREADY_PAID",
                "User has already paid for this course",
                 "You have already paid and enrolled in this course.");
            }

            // Validate course price 
            if (course.get().getPrice() <= 0) {
                throw PaymentException.invalidAmount(course.get().getPrice());
            }

            // Generate unique payment reference
            String paymentReference = "IGA_" + System.currentTimeMillis() + "_" + user.get().getId().toString().substring(0, 8);

            // Create and  save payment record 
            Payment payment = new Payment();
            payment.setStudent(user.get());
            payment.setCourse(course.get());
            payment.setAmount(course.get().getPrice());
            payment.setPaymentStatus(PaymentStatus.PENDING);
            payment.setTransactionReference(paymentReference);
            payment.setPaymentDate(LocalDateTime.now());

            Payment savedPayment = paymentRepository.save(payment);

            // Prepare Flutterwave payment payload 
            Map<String, Object> payload = new HashMap<>();
            payload.put("tx_ref", paymentReference);
            payload.put("amount", course.get().getPrice());
            payload.put("currency", paymentRequest.getCurrency() != null ? paymentRequest.getCurrency() : "RWF");
            
            // Set redirect URL to our verification endpoint with payment reference
            String verificationUrl = "http://localhost:5000/api/public/payments/callback?payment_reference=" + paymentReference;
            payload.put("redirect_url", verificationUrl);
            payload.put("payment_options", "card,mobilemoney,ussd,bank_transfer");


            // Add  meta data 
            Map<String, Object> meta = new HashMap<>();
            meta.put("payment_id", savedPayment.getId().toString());
            meta.put("course_id", course.get().getId().toString());
            meta.put("student_id", user.get().getId().toString());
            payload.put("meta", meta);

            // Customer information 
            Map<String, Object> customer = new HashMap<>();
            customer.put("email", user.get().getEmail());
            customer.put("name", user.get().getFirstName() + " " + user.get().getLastName());
            payload.put("customer", customer);

            // Customization 
            Map<String, Object> customization = new HashMap<>();
            customization.put("title", "IGA Course Payment");
            customization.put("description", "Payment for course:" + course.get().getCourseName());
            payload.put("customizations", customization);


            // Call flutterwave Api 
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization","Bearer " +  flutterwaveSecretKey);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            String url = flutterwaveBaseUrl + "/payments";
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, requestEntity, Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                if ("success".equals(responseBody.get("status"))) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    String paymentLink = (String) data.get("link");

                    // Return success response
                    return PaymentResponse.builder()
                        .success(true)
                        .message("Payment initiated successfully")
                        .paymentLink(paymentLink)
                        .paymentReference(paymentReference)
                        .paymentId(savedPayment.getId())
                        .amount(course.get().getPrice())
                        .currency(paymentRequest.getCurrency() != null ? paymentRequest.getCurrency() : "RWF")
                        .paymentStatus(PaymentStatus.PENDING)
                        .courseId(course.get().getId())
                        .courseName(course.get().getCourseName())
                        .userId(user.get().getId())
                        .customerName(user.get().getFirstName() + " " + user.get().getLastName())
                        .customerEmail(user.get().getEmail())
                        .timestamp(LocalDateTime.now())
                        .build();
                } else {
                    String message = (String) responseBody.get("message");
                    throw PaymentException.paymentGatewayError("Flutterwave API error :  " + message);
                }
            }
            else {
                throw PaymentException.paymentGatewayError("Failed to get response from payment gateway");
            }


        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            throw new PaymentException("PAYMENT_INITIATION_ERROR", 
            "Failed to initiate payment: " + e.getMessage(),
            "Payment initiation failed. Please try again later.");
        }
    }

    @Override
    public PaymentResponse verifyPayment(PaymentVerificationRequest verificationRequest) throws PaymentException {
         try {
            // Validate verification request  
            if (verificationRequest.getTransactionId() == null || verificationRequest.getTransactionId().trim().isEmpty()) {
                throw new PaymentException("VALIDATION_ERROR", "Transaction ID is required for verification", "Transaction ID is required for verification");
            }

            if (verificationRequest.getPaymentReference() == null || verificationRequest.getPaymentReference().trim().isEmpty()) {
                throw new PaymentException("VALIDATION_ERROR", "Payment reference is required for verification", "Payment reference is required for verification");
            }

            // Find payment by reference in our database 
            Payment payment = paymentRepository.findByTransactionReference(verificationRequest.getPaymentReference())
                              .orElseThrow(() -> PaymentException.paymentVerificationFailed(verificationRequest.getTransactionId()));

                              // Check  if payment is already completed 
                              if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
                                return PaymentResponse.builder()
                                    .success(true)
                                    .message("Payment already verified and completed")
                                    .paymentId(payment.getId())
                                    .paymentReference(payment.getTransactionReference())
                                    .amount(payment.getAmount())
                                    .paymentStatus(payment.getPaymentStatus())
                                    .courseId(payment.getCourse().getId())
                                    .courseName(payment.getCourse().getCourseName())
                                    .userId(payment.getStudent().getId())
                                    .customerName(payment.getStudent().getFirstName() + " " + payment.getStudent().getLastName())
                                    .customerEmail(payment.getStudent().getEmail())
                                    .timestamp(LocalDateTime.now())
                                    .build();
                              }

                              // Verify payment with flutterwave 
                              Map<String, Object> verificationResponse = verifyWithFlutterwave(verificationRequest.getTransactionId());

                              // Check verification response 
                              if (verificationResponse != null && "success".equals(verificationResponse.get("status"))){
                                Map<String, Object> data = (Map<String, Object>) verificationResponse.get("data");
                                String status = (String) data.get("status");
                                Double amount = ((Number) data.get("amount")).doubleValue();
                                String currency = (String) data.get("currency");

                                // Validate payment details 
                                if (!"successful".equals(status)) {
                                    // Update payment status to failed 
                                    payment.setPaymentStatus(PaymentStatus.FAILED);
                                    paymentRepository.save(payment);

                                    throw PaymentException.paymentVerificationFailed(verificationRequest.getTransactionId());
                                }

                              // Validate amount matches 
                              if (Math.abs(amount - payment.getAmount()) > 0.01) {
                                // Update payment status to failed 
                                payment.setPaymentStatus(PaymentStatus.FAILED);
                                paymentRepository.save(payment);

                                throw PaymentException.paymentVerificationFailed(verificationRequest.getTransactionId());
                              }

                              // Payment is successful -update payment status 
                              payment.setPaymentStatus(PaymentStatus.COMPLETED);
                              payment.setPaymentDate(LocalDateTime.now());
                              Payment savedPayment = paymentRepository.save(payment);

                                // Create enrollment for student
                                UUID enrollmentId = createEnrollmentForStudent(payment.getStudent(), payment.getCourse());

                                // Return success response
                                return PaymentResponse.builder()
                                       .success(true)
                                       .message("Payment verified successfully and enrollment completed")
                                       .paymentId(savedPayment.getId())
                                       .transactionId(verificationRequest.getTransactionId())
                                       .paymentReference(savedPayment.getTransactionReference())
                                       .amount(savedPayment.getAmount())
                                       .currency(currency)
                                       .paymentStatus(savedPayment.getPaymentStatus())
                                       .paymentDate(savedPayment.getPaymentDate())
                                       .courseId(savedPayment.getCourse().getId())
                                       .courseName(savedPayment.getCourse().getCourseName())
                                       .userId(savedPayment.getStudent().getId())
                                       .customerName(savedPayment.getStudent().getFirstName() + " " + savedPayment.getStudent().getLastName())
                                       .customerEmail(savedPayment.getStudent().getEmail())
                                       .enrollmentId(enrollmentId)
                                       .enrollmentStatus("ENROLLED")
                                       .enrollmentDate(LocalDateTime.now())
                                       .timestamp(LocalDateTime.now())
                                       .build();

                              
                            } else {
                                // verification failed
                                payment.setPaymentStatus(PaymentStatus.FAILED);
                                paymentRepository.save(payment);

                                String errorMessage = verificationResponse != null ? 
                                (String) verificationResponse.get("message") : "Unknown error during payment verification";

                                throw PaymentException.paymentVerificationFailed(verificationRequest.getTransactionId());
                            }
         } catch (PaymentException e) {
            throw e;
         } catch (Exception e) {
            throw new PaymentException("PAYMENT_VERIFICATION_ERROR",
            "Failed to verify payment: " + e.getMessage() ,
            "Payment verification failed. Please contact support.");
         }
    }

    private Map<String, Object> verifyWithFlutterwave(String transactionId) throws PaymentException {
        try {

            // Build  verification  URL  
            String verificationUrl = flutterwaveBaseUrl + "/transactions/" + transactionId + "/verify";
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + flutterwaveSecretKey);

            // Create request  entity  
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            // Call flutterwave verification API
            ResponseEntity<Map> response = restTemplate.exchange(
                verificationUrl, HttpMethod.GET, requestEntity, Map.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            else {
                throw PaymentException.paymentGatewayError(
                    "Failed to get response from Flutterwave verification API"
                );

            }
        } catch (Exception e) {
           throw PaymentException.paymentGatewayError("Error calling Flutterwave verification API: " + e.getMessage());
        }
    }

    private UUID createEnrollmentForStudent(UserSchema student, CoursesSchema course) throws PaymentException {
        try {
            // Check  if student is already enrolled in the course 
            if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
                Optional<Enrollment> existingEnrollment = enrollmentRepository
                          .findByStudentIdAndCourseId(student.getId(),course.getId() );

                          if (existingEnrollment.isPresent()) {
                            return existingEnrollment.get().getId();
                          }
            }

            // Create new enrollment
            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setCourse(course);
            enrollment.setEnrollmentDate(LocalDateTime.now());
            enrollment.setProgress(ProgressEnum.NOT_STARTED.name());

            Enrollment savedErrollment = enrollmentRepository.save(enrollment);
            return savedErrollment.getId();



        } catch (Exception e) {
            throw new PaymentException(
               "ENROLLMENT_ERROR",
               "Failed to create enrollment for student: " + e.getMessage(),
               "Payment was successful but enrollment failed. Please contact support for assistance."
            );
        }
    }

    @Override
    public PaymentResponse handlePaymentWebhook(String transactionId, String reference, String status)
            throws PaymentException {
         try  {

            // Validate webhook parameters 
            if (transactionId == null || transactionId.trim().isEmpty()) {
                throw new PaymentException(
                    "WEBHOOK_VALIDATION_ERROR",
                    "Transaction ID is required for webhook processing",
                    "Invalid webhook data received "
                );

            }

            if (reference == null || reference.trim().isEmpty()) {
                throw new PaymentException(
                    "WEBHOOK_VALIDATION_ERROR",
                    "Payment reference is required for webhook processing",
                    "Invalid webhook data received "
                );
            }

            if (status == null || status.trim().isEmpty()) {
                throw new PaymentException(
                    "WEBHOOK_VALIDATION_ERROR",
                    "Payment status is required for webhook processing",
                    "Invalid webhook data received "
                );
            }

            // Find payment by reference in our database 
            Payment payment = paymentRepository.findByTransactionReference(reference)
                                .orElseThrow( () -> new PaymentException(
                                    "PAYMENT_NOT_FOUND",
                                    "payment not found with reference : " + reference,
                                    "Payment record not found"
                                ));
            // Log webhook received for debugging
           System.out.println("Webhook received - Reference: " + reference + 
                          ", Transaction ID: " + transactionId + 
                          ", Status: " + status);

           // If  payment is already processed , return existing status
           if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            return PaymentResponse.builder()
                .success(true)
                .message("Payment already processed and completed")
                .paymentId(payment.getId())
                .paymentReference(payment.getTransactionReference())
                .transactionId(transactionId)
                .amount(payment.getAmount())
                .paymentStatus(PaymentStatus.COMPLETED)
                .paymentDate(payment.getPaymentDate())
                .courseId(payment.getCourse().getId())
                .courseName(payment.getCourse().getCourseName())
                .userId(payment.getStudent().getId())
                .customerName(payment.getStudent().getFirstName() + " " + payment.getStudent().getLastName())
                .customerEmail(payment.getStudent().getEmail())
                .timestamp(LocalDateTime.now())
                .build();

           }

           // Verify  the  webhook by  calling Flutterwave API to double-check 
           Map<String, Object> verificationResponse = verifyWithFlutterwave(transactionId);

           if (verificationResponse != null && "success".equals(verificationResponse.get("status"))) {
            Map<String, Object> data = (Map<String, Object>) verificationResponse.get("data");
            String verifiedStatus = (String) data.get("status");
            Double verifiedAmount = ((Number) data.get("amount")).doubleValue();
            String currency = (String) data.get("currency");


            // Process base on  verified status 
            switch (verifiedStatus.toLowerCase()) {
                case "successful" : 
                    return handleSuccessfulPayment(payment, transactionId, verifiedAmount, currency);
                case "failed" : 
                    return handleFailedPayment(payment, transactionId, "Payment failed at gateway");
                
                case "cancelled" :
                   return handleCancelledPayment(payment, transactionId);
                
                default: 
                  // Keeep as pending for statuses we don't recognize yet
                  return handlePendingPayment(payment, transactionId, verifiedStatus);
            }
           }
           else {
              // Verification failed - mark as failed
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setPaymentDate(LocalDateTime.now());
            paymentRepository.save(payment);

            return PaymentResponse.builder()
                .success(false)
                .message("Webhook verification failed")
                .errorCode("WEBHOOK_VERIFICATION_FAILED")
                .paymentId(payment.getId())
                .paymentReference(reference)
                .transactionId(transactionId)
                .paymentStatus(PaymentStatus.FAILED)
                .timestamp(LocalDateTime.now())
                .build();
           }

         }
         catch (Exception e) {
            throw new PaymentException(
                "WEBHOOK_HANDLING_ERROR",
                "Failed to handle payment webhook: " + e.getMessage(),
                "An error occurred while processing the payment notification."
            );
         }
    }

    private PaymentResponse handleSuccessfulPayment(Payment payment, String transactionId, Double verifiedAmount,
            String currency) throws PaymentException {
         try  {
            // Validate amount matches 
            if (Math.abs(verifiedAmount - payment.getAmount()) > 0.01) {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);

                throw new PaymentException(
                    "AMOUNT_MISMATCH",
                    "Amount mismatch in webhook. Exoected :" + payment.getAmount() +
                    ", Got :" + verifiedAmount,
                    "Payment amount verification failed"
                );
            }

            // Update payment status to completed 
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setPaymentDate(LocalDateTime.now());
            Payment savedPayment = paymentRepository.save(payment);

            // Create enrollment for student 
            UUID enrollmentId = createEnrollmentForStudent(payment.getStudent(), payment.getCourse());

            return PaymentResponse.builder()
                .success(true)
                .message("Payment completed successfully via  webhook")
                .paymentId(savedPayment.getId())
                .transactionId(transactionId)
                .paymentReference(savedPayment.getTransactionReference())
                .amount(savedPayment.getAmount())
                .currency(currency)
                .paymentStatus(PaymentStatus.COMPLETED)
                .paymentDate(savedPayment.getPaymentDate())
                .courseId(savedPayment.getCourse().getId())
                .courseName(savedPayment.getCourse().getCourseName())
                .userId(savedPayment.getStudent().getId())
                .customerName(savedPayment.getStudent().getFirstName() + " " + savedPayment.getStudent().getLastName())
                .customerEmail(savedPayment.getStudent().getEmail())
                .enrollmentId(enrollmentId)
                .enrollmentStatus("ENROLLED")
                .enrollmentDate(LocalDateTime.now())
                .timestamp(LocalDateTime.now())
                .build();

         }
         catch (Exception e) {
            throw new PaymentException(
                "WEBHOOK_PROCESSING_ERROR",
                "Error processing successful payment webhook: " + e.getMessage(),
                "An error occurred while processing the payment notification."
            );
         }
    }

    private PaymentResponse handleFailedPayment(Payment payment, String transactionId, String reason) {
        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setPaymentDate(LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);

        return PaymentResponse.builder()
            .success(false)
            .message("Payment failed: " + reason)
            .paymentId(savedPayment.getId())
            .transactionId(transactionId)
            .paymentReference(savedPayment.getTransactionReference())
            .amount(savedPayment.getAmount())
            .paymentStatus(PaymentStatus.FAILED)
            .paymentDate(savedPayment.getPaymentDate())
            .courseId(savedPayment.getCourse().getId())
            .courseName(savedPayment.getCourse().getCourseName())
            .userId(savedPayment.getStudent().getId())
            .customerName(savedPayment.getStudent().getFirstName() + " " + savedPayment.getStudent().getLastName())
            .customerEmail(savedPayment.getStudent().getEmail())
            .timestamp(LocalDateTime.now())
            .build();
    }

    private PaymentResponse handleCancelledPayment(Payment payment, String transactionId) {
        payment.setPaymentStatus(PaymentStatus.CANCELLED);
        payment.setPaymentDate(LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);

        return PaymentResponse.builder()
            .success(false)
            .message("Payment was cancelled by user")
            .errorCode("PAYMENT_CANCELLED")
            .paymentId(savedPayment.getId())
            .transactionId(transactionId)
            .paymentReference(savedPayment.getTransactionReference())
            .amount(savedPayment.getAmount())
            .paymentStatus(PaymentStatus.CANCELLED)
            .paymentDate(savedPayment.getPaymentDate())
            .courseId(savedPayment.getCourse().getId())
            .courseName(savedPayment.getCourse().getCourseName())
            .userId(savedPayment.getStudent().getId())
            .customerName(savedPayment.getStudent().getFirstName() + " " + savedPayment.getStudent().getLastName())
            .customerEmail(savedPayment.getStudent().getEmail())
            .timestamp(LocalDateTime.now())
            .build();
    }

    private PaymentResponse handlePendingPayment(Payment payment, String transactionId , String status ) {
        // Keep as pending for statuses we don't recognize yet 
        return PaymentResponse.builder()
            .success(true)
            .message("Payment status updated :" + status)
            .paymentId(payment.getId())
            .transactionId(transactionId)
            .paymentReference(payment.getTransactionReference())
            .amount(payment.getAmount())
            .paymentStatus(PaymentStatus.PENDING)
            .courseId(payment.getCourse().getId())
            .courseName(payment.getCourse().getCourseName())
            .userId(payment.getStudent().getId())
            .customerName(payment.getStudent().getFirstName() + "" + payment.getStudent().getLastName())
            .customerEmail(payment.getStudent().getEmail())
            .timestamp(LocalDateTime.now())
            .metadata(Map.of("gateway_status", status))
            .build();
    }

    @Override
    public Optional<Payment> getPaymentById(UUID paymentId) {
         try  {
            
            //validate  payment  Id  
            if (paymentId == null) {
                return Optional.empty();
            }

            // Log the  request  for debugging 
           System.out.println("Fetching payment with ID: " + paymentId);
           
           // Find payment by ID in  the  database 
           Optional<Payment> payment = paymentRepository.findById(paymentId);

           if (payment.isPresent()) {
             System.out.println("Payment found successfully for ID:" + paymentId);

           } else {
               System.out.println("No payment found for ID:" + paymentId);
           }
          
           return payment;

         }
         catch (Exception e) {
            System.err.println("Error retrieving payment by ID: " + e.getMessage());
            return Optional.empty();
         }
    }

    @Override
    public Optional<Payment> getPaymentByReference(String reference) {
         try {

            // Validate reference paramenter 
            if (reference == null || reference.trim().isEmpty()) {
                System.err.println("Payment reference is required");
                return Optional.empty();
            }

            // Log the request for debugging 
            System.out.println("Fetching payment with reference" + reference);

            // Find payment by transaction reference in the database
            Optional<Payment> payment = paymentRepository.findByTransactionReference(reference);
            
            if (payment.isPresent()) {
                System.out.println("Payment found successfully for reference :" + reference);
                System.out.println("Payment ID :" + payment.get().getId() + 
                                     ", Status: " + payment.get().getPaymentStatus() + 
                                     ", Amount :" + payment.get().getAmount());
            } else {
                System.out.println("No payment found with reference " + reference);
            }

            return payment;
         }
         catch (Exception e) {
            System.err.println("Error retrieving payment by reference: " + e.getMessage());
            return Optional.empty();
         }
    }

    public Page<Payment> getUserPayments(UUID userId, Pageable pageable) {
        try {
            // Validate parameters 
            if (userId == null) {
                 throw new IllegalArgumentException("User Id cannot be null");

            }

            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            System.out.println("Fetching payments for user ID :" + userId + 
                                ",Page :" + pageable.getPageNumber() + 
                                ", Size : " + pageable.getPageSize());
            // Get user payments with  pagination 
            Page<Payment> payments = paymentRepository.findByStudentIdOrderByPaymentDateDesc(userId,pageable);

            System.out.println("Found" + payments.getTotalElements() + " payments for user :" + userId);

            return payments;

        } catch (Exception e) {
            System.err.println("Error fetching user payments: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve payments for user: " + e.getMessage());
        }
    }

    @Override
    public Page<Payment> getUserPaymentsByToken(String token, Pageable pageable) {
        try {
            // Validate parameters
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("Token cannot be null or empty");
            }

            if (pageable == null) {
                throw new IllegalArgumentException("Pageable cannot be null");
            }

            // Extract username (email) from JWT token
            String email = jwtUtils.getEmailFromJwtToken(token);
            
            // Find user by email
            UserSchema user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            System.out.println("Fetching payments for user email: " + email + 
                                ", Page: " + pageable.getPageNumber() + 
                                ", Size: " + pageable.getPageSize());

            // Get user payments with pagination
            Page<Payment> payments = paymentRepository.findByStudentIdOrderByPaymentDateDesc(user.getId(), pageable);
            
            System.out.println("Found " + payments.getTotalElements() + " payments for user: " + email);

            return payments;

        } catch (Exception e) {
            System.err.println("Error fetching user payments by token: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve user payments by token: " + e.getMessage());
        }
    }

    @Override
    public Page<com.work.IGA.Utils.PaymentUtils.PaymentHistoryDto> getUserPaymentHistoryByToken(String token, Pageable pageable) {
        try {
            // Get payments page
            Page<Payment> payments = getUserPaymentsByToken(token, pageable);
            
            // Convert to DTOs
            return payments.map(com.work.IGA.Utils.PaymentUtils.PaymentHistoryDto::new);
            
        } catch (Exception e) {
            System.err.println("Error fetching user payment history by token: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve user payment history by token: " + e.getMessage());
        }
    }

    @Override
    public List<Payment> getCoursePayments(UUID courseId) {
        try {
            // Validate course Id 
            if (courseId == null) {
                throw new IllegalArgumentException("Course ID cannot be null");

            }
            System.out.println("Fetching payments for course ID : " + courseId);

            // Get all payments for the course
            List<Payment> payments = paymentRepository.findByCourseIdOrderByPaymentDateDesc(courseId);

            System.out.println("Found" + payments.size() + " payments for course :" + courseId);

            return payments;

        }
        catch( Exception e) {
            System.err.println("Error retrieving course payments:" + e.getMessage());
            throw new RuntimeException("Failed to retrieve course payments:" + e.getMessage());
        }
    }

    @Override
    public List<Payment> getPaymentsByStatus(String status) {
         try {
            // Validate status 
            if (status == null || status.trim().isEmpty()) {
                throw new IllegalArgumentException("Payment status cannot be null or empty");
            }

            System.out.println("Fetching payments with status :" + status);

            // Convert string to PaymentStatus enum 
            PaymentStatus paymentStatus;
            try {
                paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid payment status: " + status +
                ". Valid statuses are PENDING, COMPLETED, FAILED, REFUNDED, CANCELLED");

            }

            // Get  payments by status 
            List<Payment> payments = paymentRepository.findByPaymentStatus(paymentStatus);

            System.out.println("Found" + payments.size() + " payments with status :" + status);

            return payments;

         }
         catch(Exception e) {
            System.err.println("Error retrieving payments by status: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve payments by status: " + e.getMessage());
         }
    }

    @Override
    public Payment updatePaymentStatus(UUID paymentId, PaymentStatus status) {
         try {
            // Validate parameters
        if (paymentId == null) {
            throw new IllegalArgumentException("Payment ID cannot be null");
        }
        
        if (status == null) {
            throw new IllegalArgumentException("Payment status cannot be null");
        }

        System.out.println("Updating payment " + paymentId + " to status: " + status);

        // Find the payment
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

        // Store old status for logging
        PaymentStatus oldStatus = payment.getPaymentStatus();
        
        // Validate status transition (business logic)
        if (!isValidStatusTransition(oldStatus, status)) {
            throw new IllegalArgumentException(
                String.format("Invalid status transition from %s to %s", oldStatus, status)
            );
        }

        // Update the status and timestamp
        payment.setPaymentStatus(status);
        payment.setPaymentDate(LocalDateTime.now());

        // Handle status-specific logic
        if (status == PaymentStatus.COMPLETED && oldStatus != PaymentStatus.COMPLETED) {
            // Create enrollment if payment is completed for the first time
            try {
                createEnrollmentForStudent(payment.getStudent(), payment.getCourse());
                System.out.println("Enrollment created for payment completion: " + paymentId);
            } catch (PaymentException e) {
                System.err.println("Failed to create enrollment for payment: " + paymentId + 
                                 ". Error: " + e.getMessage());
                // Continue with payment status update even if enrollment fails
            }
        }

        // Save the updated payment
        Payment updatedPayment = paymentRepository.save(payment);

        System.out.println("Payment status updated successfully from " + oldStatus + " to " + status + 
                          " for payment ID: " + paymentId);

        return updatedPayment;


         }
         catch (Exception e ) {
            throw new RuntimeException("Failed to update payment status:" + e.getMessage());
         }
    }

private boolean isValidStatusTransition(PaymentStatus oldStatus, PaymentStatus newStatus) {
    // Allow same status
    if (oldStatus == newStatus) {
        return true;
    }
    
    // Define valid transitions
    switch (oldStatus) {
        case PENDING:
            // From PENDING, can go to COMPLETED, FAILED, or CANCELLED
            return true ;
        
        case COMPLETED:
            // From COMPLETED, can only go to REFUNDED
            return newStatus == PaymentStatus.REFUNDED;
        
        case FAILED:
            // From FAILED or CANCELLED, can only go back to PENDING (for retry)
            return newStatus == PaymentStatus.PENDING || newStatus == PaymentStatus.CANCELLED;
        case CANCELLED:
            // From CANCELLED, can only go back to PENDING (for retry)
            return newStatus == PaymentStatus.PENDING;

        case REFUNDED:
            // REFUNDED is a final state, no transitions allowed
            return false;
        
        default:
            return false;
    }
}

    @Override
    public boolean hasUserPaidForCourse(UUID userId, UUID courseId) {
         try {
        // Validate parameters
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        if (courseId == null) {
            throw new IllegalArgumentException("Course ID cannot be null");
        }

        System.out.println("Checking payment status for user ID: " + userId + " and course ID: " + courseId);

        // Check if there's a completed payment for this user and course
        boolean hasPaid = paymentRepository.existsByStudentIdAndCourseIdAndPaymentStatus(
            userId, 
            courseId, 
            PaymentStatus.COMPLETED
        );

        System.out.println("Payment check result - User: " + userId + 
                          ", Course: " + courseId + 
                          ", Has paid: " + hasPaid);

        return hasPaid;

    } catch (Exception e) {
        System.err.println("Error checking payment status for user " + userId + 
                          " and course " + courseId + ": " + e.getMessage());
        e.printStackTrace();
        // Return false in case of error to be safe
        return false;
    }
    }

    @Override
    public boolean hasUserPaidForCourseByToken(String token, UUID courseId) {
        try {
            // Validate parameters
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("Token cannot be null or empty");
            }
            
            if (courseId == null) {
                throw new IllegalArgumentException("Course ID cannot be null");
            }

            // Extract username (email) from JWT token
            String email = jwtUtils.getEmailFromJwtToken(token);
            
            // Find user by email
            UserSchema user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            System.out.println("Checking payment status for user email: " + email + " and course ID: " + courseId);

            // Check if there's a completed payment for this user and course
            boolean hasPaid = paymentRepository.existsByStudentIdAndCourseIdAndPaymentStatus(
                user.getId(), 
                courseId, 
                PaymentStatus.COMPLETED
            );

            System.out.println("Payment check result - User: " + email + 
                              ", Course: " + courseId + 
                              ", Has paid: " + hasPaid);

            return hasPaid;

        } catch (Exception e) {
            System.err.println("Error checking payment status by token for course " + courseId + ": " + e.getMessage());
            e.printStackTrace();
            // Return false in case of error to be safe
            return false;
        }
    }

    @Override
    public PaymentResponse processRefund(UUID paymentId, String reason) throws PaymentException {
         try  {

              // Validate parameters 
              if (paymentId == null) {
                 throw new PaymentException("VALIDATION_ERROR",
                 "Payment ID is  required for refund processing",
                 "Payment ID cannot be null");
              }

              if (reason == null || reason.trim().isEmpty()) {
                    throw new PaymentException("VALIDATION_ERROR", 
                    "Refund reason is  required",
                            "Please provide a  reason for  the  refund");
              }

              System.out.println("Processing refund for payment ID : " + paymentId + ", Reason" + reason);

           // Find the payment 
           Payment payment = paymentRepository.findById(paymentId)
               .orElseThrow(() -> new PaymentException("PAYMENT_NOT_FOUND", 
               "Payment not found with ID:" + paymentId, 
               "Payment record not found")) ;
            
           // Validate payement status -only completed payment can be refunded 
           if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
                throw new PaymentException(
                    "INVALID_PAYMENT_STATUS",
                    "Only completed payments can be refunded. Current status: " + payment.getPaymentStatus(),
                    "Refunds can only be processed for completed payments."
                );
           }

           // Check if payment is already refunded 
           if (payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
                throw new PaymentException(
                    "ALREADY_REFUNDED",
                    "Payment with ID :" + paymentId + " has already been refunded",
                    "This payment has already been refunded."
                );
           }

           // Call Flutterwave refund API 
           Map<String, Object> refundRespoMap = processFlutterwaveRefund(payment, reason);

           if (refundRespoMap != null && "success".equals(refundRespoMap.get("status"))){
            Map<String, Object> data = (Map<String, Object>) refundRespoMap.get("data");
            String refundStatus = (String) data.get("status");

            if ("successful".equals(refundStatus) || "pending".equals(refundStatus)) {
                // Update payment status to refunded 
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                payment.setPaymentDate(LocalDateTime.now());
                Payment savedPayment = paymentRepository.save(payment);

            // Remove enrollment if exists
            removeStudentEnrollment(payment.getStudent(), payment.getCourse());

            return PaymentResponse.builder()
                .success(true)
                    .message("Refund processed successfully")
                    .paymentId(savedPayment.getId())
                    .paymentReference(savedPayment.getTransactionReference())
                    .amount(savedPayment.getAmount())
                    .paymentStatus(PaymentStatus.REFUNDED)
                    .paymentDate(savedPayment.getPaymentDate())
                    .courseId(savedPayment.getCourse().getId())
                    .courseName(savedPayment.getCourse().getCourseName())
                    .userId(savedPayment.getStudent().getId())
                    .customerName(savedPayment.getStudent().getFirstName() + " " + savedPayment.getStudent().getLastName())
                    .customerEmail(savedPayment.getStudent().getEmail())
                    .timestamp(LocalDateTime.now())
                        .metadata(Map.of("refund_reason", reason, "refund_status", refundStatus))
                        .gatewayResponse(refundRespoMap)
                        .build();
                } else {
                    throw new PaymentException(
                        "REFUND_FAILED",
                        "Refund failed at payment gateway. Status: " + refundStatus,
                        "Refund could not be processed at this time. Please contact support."
                    );
                }
            } else {
                String errorMessage = refundRespoMap != null ?
                (String) refundRespoMap.get("message") : "Unknown error during refund processing";
                
                throw new PaymentException(
                    "REFUND_FAILED",
                    "Refund failed at payment gateway: " + errorMessage,
                    "Refund could not be processed at this time. Please contact support."
                );
            }

        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            throw new PaymentException(
                "REFUND_PROCESSING_ERROR",
                "Failed to process refund: " + e.getMessage(),
                "Refund could not be processed. Please contact support."
            );
        }
    }



    // Helper method to create a success response refund Fliutterwave 
    private Map<String, Object> processFlutterwaveRefund(Payment payment, String reason) throws PaymentException {
        try {
            // Build refund URL 
            String refundUrl = flutterwaveBaseUrl + "/transactions/" + payment.getId() + "/refund";
            
            // Prepare refund payload 
            Map<String, Object> payload = new HashMap<>();
            payload.put("amount", payment.getAmount());
            payload.put("reason", reason);

            // Set up headers 
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + flutterwaveSecretKey);

            // Create request entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            System.out.println("Calling Flutterwave refund API for payment: " + payment.getId());

            // call Flutterwave refund API 
            ResponseEntity<Map> response = restTemplate.exchange(
                refundUrl, HttpMethod.POST, requestEntity, Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() &&  response.getBody() != null) {
                return response.getBody();
            }else {
                throw new PaymentException(
                    "REFUND_API_ERROR",
                    "Failed to get response from Flutterwave refund API",
                    "Refund processing failed at  payment gateway"
                );            }
        } catch (Exception e) {
            throw new PaymentException(
                "REFUND_API_CALL_ERROR",
                "Error calling Flutterwave refund API: " + e.getMessage(),
                "Refund processing failed at payment gateway"
            );
        }
    }

    // Helper method to remove student enrollment 
    private void removeStudentEnrollment(UserSchema student, CoursesSchema course) {
        try {
            Optional<Enrollment> enrollmentOpt = enrollmentRepository
                .findByStudentIdAndCourseId(student.getId(), course.getId());

            if (enrollmentOpt.isPresent()) {
                enrollmentRepository.delete(enrollmentOpt.get());
                System.out.println("Enrollment removed for student:" + student.getId() + 
                                   ", Course:" + course.getId());
            } else {
                System.out.println("No  enrollment found to remove for student :  " + student.getId() + 
                                   ", Course: " + course.getId());
            }

        } catch (Exception e) {
            System.err.println("Error removing enrollment for student " + student.getId() +
                               " from course " + course.getId() + ": " + e.getMessage());
        }
    }
    @Override
    public Page<Payment> getUserPayments(UUID userId,
            org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable pageable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserPayments'");
    }

    @Override
    public PaymentResponse requestRefund(UUID paymentId, String reason, String token) throws PaymentException {
        try  {
            // Validate token 
            if (token  == null || token.trim().isEmpty()) {
                throw new PaymentException(
                    "AUTHENTICATION_ERROR",
                    "Authentication token is required",
                    "Pleasee provide a valid authentication token"
                );


            }

            // Remove  Bearer prefix if present 
            if (token.startsWith("Bearer")) {
                token = token.substring(7);
            }

            // validate jwt token 
            if (!jwtUtils.validateJwtToken(token)) {
                throw new PaymentException(
                    "AUTHENTICATION_ERROR",
                    "Invalid authentication token",
                    "Please provide a valid authentication token"
                );
            }

            // Extract  user email from token and  find user 
            String userEmail = jwtUtils.getEmailFromJwtToken(token);
            Optional<UserSchema> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isEmpty()) {
                throw new PaymentException(
                    "USER_NOT_FOUND",
                    "User not found for email extracted from token",
                    "Authenticated user not found. Please contact support."
                );
            }
        
            // Find  the payment 
            Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("PAYMENT_NOT_FOUND", 
                "Payment not found with ID :" + paymentId, 
                "Payment record not found"));

            // Verify that the payment belongs to the request user 
            if (!payment.getStudent().getId().equals(userOpt.get().getId())) {
                throw new PaymentException(
                    "UNAUTHORIZATION_ACCESS",
                    "User not authorization to  refund this payment",
                    "You are not authorized to request a  refund for this payment"
                );
            }

            // Process the  refund 
            return processRefund(paymentId, reason);
        }
        catch (Exception e) {
            throw new PaymentException(
                "REFUND_REQUEST_ERROR",
                "Failed to request refund: " + e.getMessage(),
                "Refund request could not be processed. Please contact support."
            );
        }
    }

	
}