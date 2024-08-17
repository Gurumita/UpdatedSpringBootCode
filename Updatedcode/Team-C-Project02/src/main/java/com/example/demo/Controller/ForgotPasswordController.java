package com.example.demo.Controller;

import com.example.demo.Models.User;
import com.example.demo.Service.EmailService;
import com.example.demo.Service.OtpService;
import com.example.demo.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class ForgotPasswordController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpService otpService;

    @PostMapping("/send_otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");


        User user = userService.findByNameAndEmail(name, email);
        if (name == null || email == null){
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not found"));
        }

        // Generate and save OTP
        String otp = otpService.generateOtp();
        otpService.saveOtpForUser(user.getUserid(), otp);

        // Send OTP via email
        emailService.sendOtpEmail(user.getEmail(), otp);

        return ResponseEntity.ok(Map.of("success", true, "message", "OTP sent to your email."));
    }

    @PostMapping("/validate_otp")
    public ResponseEntity<?> validateOtpAndResetPassword(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        User user = userService.findByNameAndEmail(name, email);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not found"));
        }

        if (!otpService.validateOtpForUser(user.getUserid(), otp)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid OTP"));
        }

        userService.updatePassword(user.getUserid(), newPassword);
        otpService.invalidateOtpForUser(user.getUserid());

        return ResponseEntity.ok(Map.of("success", true, "message", "Password has been successfully reset."));
    }
}
