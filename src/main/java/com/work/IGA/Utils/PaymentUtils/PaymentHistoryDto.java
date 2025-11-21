package com.work.IGA.Utils.PaymentUtils;

import com.work.IGA.Models.Courses.Payment;
import com.work.IGA.Models.Courses.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryDto {
    
    private UUID id;
    private String courseName;
    private UUID courseId;
    private String studentName;
    private UUID studentId;
    private double amount;
    private PaymentStatus paymentStatus;
    private String transactionReference;
    private String paymentMethod;
    private LocalDateTime paymentDate;

    // Constructor to convert from Payment entity
    public PaymentHistoryDto(Payment payment) {
        this.id = payment.getId();
        this.amount = payment.getAmount();
        this.paymentStatus = payment.getPaymentStatus();
        this.transactionReference = payment.getTransactionReference();
        this.paymentMethod = payment.getPaymentMethod();
        this.paymentDate = payment.getPaymentDate();
        
        // Safely extract course information
        if (payment.getCourse() != null) {
            this.courseId = payment.getCourse().getId();
            this.courseName = payment.getCourse().getCourseName();
        }
        
        // Safely extract student information
        if (payment.getStudent() != null) {
            this.studentId = payment.getStudent().getId();
            this.studentName = payment.getStudent().getFirstName() + " " + payment.getStudent().getLastName();
        }
    }
}