package com.example.demo.Repository;

import com.example.demo.Models.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    Otp findByUserId(int userId);
}