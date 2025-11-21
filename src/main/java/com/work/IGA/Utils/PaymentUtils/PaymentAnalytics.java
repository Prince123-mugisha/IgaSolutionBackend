package com.work.IGA.Utils.PaymentUtils;

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
public class PaymentAnalytics {

    private int totalPayments;
    private int completedPayments;
    private int pendingPayments;
    private int failedPayments;
    private int cancelledPayments;
    private int refundedPayments;
    private double totalRevenue;
    private double averagePaymentAmount;
    
}
