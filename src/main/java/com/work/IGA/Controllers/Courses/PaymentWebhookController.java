package com.work.IGA.Controllers.Courses;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.work.IGA.Services.CourseServices.PaymentService;
import com.work.IGA.Utils.PaymentUtils.PaymentResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/public/payments")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentService paymentService;

    // Manual verification endpoint for testing
    @PostMapping("/verify-payment")
    public ResponseEntity<PaymentResponse> manualVerifyPayment(
        @RequestParam String transactionId,
        @RequestParam String reference,
        @RequestParam(defaultValue = "successful") String status
    ) {
        try {
            PaymentResponse response = paymentService.handlePaymentWebhook(
                transactionId, reference, status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PaymentResponse.errorResponse(
                    "Failed to verify payment: " + e.getMessage(),
                    "PAYMENT_VERIFICATION_ERROR"));
        }
    }

    // Handle Flutterwave webhhook notfications

    @PostMapping("/webhook")
    public ResponseEntity<String> handlePaymentWebhook(@RequestBody String webhookData , 
     @RequestHeader(value = "verif-hash", required = false) String verifHash) {
          try {

            String transactionId = "extracted_from_webhook"; // Parse from webhookData
            String reference = "extracted_from_webhook"; // Parse from webhookData  
            String status = "extracted_from_webhook"; // Parse from webhookData
            
            PaymentResponse response = paymentService.handlePaymentWebhook(
                transactionId, reference, status);
            
            return ResponseEntity.ok("Webhook processed successfully");

          }
          catch (Exception e) {
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal error processing webhook");
          }
    }

    // Payment callback URL - handles redirect after payment
    @GetMapping("/callback")
    public ResponseEntity<String> handlePaymentCallback(
        @RequestParam(required = false) String status, 
        @RequestParam(required = false) String txt_ref,
        @RequestParam(required = false) String transaction_id,
        @RequestParam(required = false) String payment_reference
    ) {
        try {
            // Log the callback parameters for debugging
            System.out.println("Payment Callback - Status: " + status + 
                             ", Transaction ID: " + transaction_id + 
                             ", Reference: " + txt_ref + 
                             ", Payment Reference: " + payment_reference);
            
            if ("successful".equals(status)) {
                // Auto-verify the payment
                if (transaction_id != null && (txt_ref != null || payment_reference != null)) {
                    try {
                        String reference = txt_ref != null ? txt_ref : payment_reference;
                        PaymentResponse verificationResponse = paymentService.handlePaymentWebhook(
                            transaction_id, reference, status);
                        
                        if (verificationResponse.isSuccess()) {
                            return ResponseEntity.ok(
                                "<html><body style='text-align:center; padding:50px;'>" +
                                "<h1 style='color:green;'>✅ Payment Successful!</h1>" +
                                "<p>Your payment has been verified successfully.</p>" +
                                "<p>You can now access your course.</p>" +
                                "<p>Transaction ID: " + transaction_id + "</p>" +
                                "<p>Reference: " + reference + "</p>" +
                                "<script>setTimeout(() => window.close(), 5000);</script>" +
                                "</body></html>"
                            );
                        } else {
                            return ResponseEntity.ok(
                                "<html><body style='text-align:center; padding:50px;'>" +
                                "<h1 style='color:orange;'>⚠️ Payment Verification Pending</h1>" +
                                "<p>Payment was successful but verification is pending.</p>" +
                                "<p>Please contact support if this issue persists.</p>" +
                                "</body></html>"
                            );
                        }
                    } catch (Exception e) {
                        System.err.println("Error during auto-verification: " + e.getMessage());
                        return ResponseEntity.ok(
                            "<html><body style='text-align:center; padding:50px;'>" +
                            "<h1 style='color:green;'>✅ Payment Successful!</h1>" +
                            "<p>Payment completed successfully but auto-verification failed.</p>" +
                            "<p>Please verify manually or contact support.</p>" +
                            "<p>Transaction ID: " + transaction_id + "</p>" +
                            "</body></html>"
                        );
                    }
                } else {
                    return ResponseEntity.ok(
                        "<html><body style='text-align:center; padding:50px;'>" +
                        "<h1 style='color:green;'>✅ Payment Successful!</h1>" +
                        "<p>Payment completed successfully!</p>" +
                        "<p>Please verify your payment manually.</p>" +
                        "</body></html>"
                    );
                }
            } else if ("cancelled".equals(status)) {
                return ResponseEntity.ok(
                    "<html><body style='text-align:center; padding:50px;'>" +
                    "<h1 style='color:orange;'>❌ Payment Cancelled</h1>" +
                    "<p>Payment was cancelled. You can try again.</p>" +
                    "<script>setTimeout(() => window.close(), 3000);</script>" +
                    "</body></html>"
                );
            } else {
                return ResponseEntity.ok(
                    "<html><body style='text-align:center; padding:50px;'>" +
                    "<h1 style='color:red;'>❌ Payment Failed</h1>" +
                    "<p>Payment failed or unknown status.</p>" +
                    "<p>Please contact support if you continue to experience issues.</p>" +
                    "<p>Status: " + status + "</p>" +
                    "</body></html>"
                );
            }
        } catch (Exception e) {
            System.err.println("Error processing payment callback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    "<html><body style='text-align:center; padding:50px;'>" +
                    "<h1 style='color:red;'>❌ Error</h1>" +
                    "<p>Error processing payment callback.</p>" +
                    "<p>Please contact support.</p>" +
                    "</body></html>"
                );
        }
    }
}
