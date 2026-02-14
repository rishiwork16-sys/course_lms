package com.finallms.backend.controller;

import com.finallms.backend.dto.AuthDto;
import com.finallms.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/admin/login")
    public ResponseEntity<AuthDto.AuthResponse> adminLogin(@RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(authService.loginAdmin(request));
    }

    @PostMapping("/student/otp")
    public ResponseEntity<String> sendOtp(@RequestBody AuthDto.OtpRequest request) {
        try {
          String ans =   authService.sendOtp(request.getPhone());
           if(ans.length()==6){
             return ResponseEntity.ok("OTP sent successfully.");
           }
           else{
            return ResponseEntity.status(429).body(ans);
           }
        } catch (RuntimeException e) {
            return ResponseEntity.status(429).body(e.getMessage());
        }
    }

    @PostMapping("/student/verify-login")
    public ResponseEntity<AuthDto.AuthResponse> verifyLoginOtp(@RequestBody AuthDto.VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyLoginOtp(request));
    }

    @PostMapping("/student/verify-register")
    public ResponseEntity<AuthDto.AuthResponse> verifyRegistrationOtp(@RequestBody AuthDto.VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyRegistrationOtp(request));
    }

    @PostMapping("/student/verify")
    public ResponseEntity<AuthDto.AuthResponse> verifySmart(@RequestBody AuthDto.VerifyOtpRequest request) {
        String normalizedPhone = request.getPhone() != null
                ? request.getPhone().replaceAll("[^0-9]", "").trim()
                : "";
        request.setPhone(normalizedPhone);
        boolean exists = authService.userExistsByPhone(normalizedPhone);
        if (exists) {
            return ResponseEntity.ok(authService.verifyLoginOtp(request));
        } else {
            return ResponseEntity.ok(authService.verifyRegistrationOtp(request));
        }
    }

    @GetMapping("/student/check")
    public ResponseEntity<?> checkStudentExists(@RequestParam String phone) {
        String normalizedPhone = phone != null ? phone.replaceAll("[^0-9]", "").trim() : "";
        boolean exists = authService.userExistsByPhone(normalizedPhone);
        return ResponseEntity.ok(java.util.Map.of("exists", exists));
    }

    @PostMapping("/student/email/otp")
    public ResponseEntity<String> sendEmailOtp(@RequestBody AuthDto.EmailOtpRequest request) {
        try {
            String ans = authService.sendEmailOtp(request.getEmail());
            if (ans.length() == 6) {
                return ResponseEntity.ok("OTP sent successfully.");
            } else {
                return ResponseEntity.status(429).body(ans);
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(429).body(e.getMessage());
        }
    }

    @PostMapping("/student/email/verify")
    public ResponseEntity<AuthDto.AuthResponse> verifyEmailSmart(@RequestBody AuthDto.VerifyEmailOtpRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        boolean exists = authService.userExistsByEmail(email);
        if (exists) {
            return ResponseEntity.ok(authService.verifyLoginEmailOtp(request));
        } else {
            return ResponseEntity.ok(authService.verifyRegistrationEmailOtp(request));
        }
    }

    @GetMapping("/student/email/check")
    public ResponseEntity<?> checkStudentExistsByEmail(@RequestParam String email) {
        String normalized = email != null ? email.trim().toLowerCase() : "";
        boolean exists = authService.userExistsByEmail(normalized);
        return ResponseEntity.ok(java.util.Map.of("exists", exists));
    }

    @PostMapping("/student/guest")
    public ResponseEntity<AuthDto.AuthResponse> guestLogin() {
        return ResponseEntity.ok(authService.createGuestUser());
    }
}
