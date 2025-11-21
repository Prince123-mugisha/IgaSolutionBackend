package com.work.IGA.Utils.PaymentUtils;

import java.util.Map;
import java.util.UUID;
import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    @NotNull(message = "Course ID is required")
    private UUID courseId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency = "RWF";

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Redirect URL is required")
    private String redirectUrl;

    // Internal field - not from request body
    private String token;

    // Optional fields
    private String callbackUrl;
    private String description;
    private Map<String, Object> metadata;
}
