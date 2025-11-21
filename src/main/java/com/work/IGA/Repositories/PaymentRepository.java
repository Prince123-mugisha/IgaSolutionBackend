package com.work.IGA.Repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import com.work.IGA.Models.Courses.Payment;
import com.work.IGA.Models.Courses.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, UUID >{

     List<Payment> findByStudentIdOrderByPaymentDateDesc(UUID studentId);
     Page<Payment> findByStudentIdOrderByPaymentDateDesc(UUID studentId, org.springframework.data.domain.Pageable pageable);
     
     List<Payment> findByCourseIdOrderByPaymentDateDesc(UUID courseId);

     List<Payment> findByPaymentStatus(PaymentStatus status);


     boolean existsByStudentIdAndCourseIdAndPaymentStatus(UUID studentId, UUID courseId, PaymentStatus status);

     Optional<Payment> findByTransactionReference(String transactionReference);
     
} 
