package com.work.IGA.Utils.PaymentUtils;

import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class PaymentDto {

    @NotNull(message = "Student ID cannot be null")
    private UUID userId;

    @NotNull(message = "Course ID cannot be null")
    private UUID courseId;

    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotBlank(message = "Phone number cannot be blank")
    private String phoneNumber;

    @NotNull(message = "Customer  name  cannot be null")
    private String customerName;

    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero")
    private double amount;

    @Pattern(regexp = "^(USD|EUR|GBP|NGN|FRW|KES)$", message = "Currency must be one of the following: USD, EUR, GBP, NGN, KES, FRW")
    private String currency = "USD";

    // Optional callback URL override
    private String callbackUrl;

    // Optional metadata 
    private String description;

    // For  payment verification 
    private String transactionId;
    private String paymentReference;

    

    
    
    
}
