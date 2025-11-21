package com.work.IGA.Utils.PaymentUtils;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVerificationRequest {

    @NotBlank(message = "Transaction ID cannot is required ")
    private String transactionId;

    @NotBlank(message = "Payment reference is required ")
    private String paymentReference;

    // Optional status from webhook
    private String status;

    // Optiona amount for validation
    private Double amount;
    
}
