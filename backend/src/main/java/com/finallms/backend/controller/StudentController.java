package com.finallms.backend.controller;

import com.finallms.backend.dto.*;
import com.finallms.backend.service.*;
import com.finallms.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private JwtUtil jwtUtil;

    // Helper to get username from Principal (which is set by JwtFilter)
    // The Principal.getName() returns the subject (email or phone)

    @Autowired
    private com.finallms.backend.service.PaymentService paymentService;

    @PostMapping("/enroll/{courseId}")
    public ResponseEntity<?> enrollFree(Principal principal, @PathVariable Long courseId) {
        if (principal == null)
            return ResponseEntity.status(401).body("Please login to enroll");
        studentService.enrollFree(principal.getName(), courseId);
        return ResponseEntity.ok("Enrolled successfully (Free)");
    }

    @PostMapping("/payment/create-order")
    public ResponseEntity<?> createOrder(Principal principal,
            @RequestBody com.finallms.backend.dto.PaymentDto.OrderRequest request) {
        if (principal == null)
            return ResponseEntity.status(401).body("Please login to create order");
        return ResponseEntity.ok(paymentService.createOrder(principal.getName(), request.getCourseId()));
    }

    @PostMapping("/payment/verify")
    public ResponseEntity<?> verifyPayment(Principal principal,
            @RequestBody com.finallms.backend.dto.PaymentDto.VerifyRequest request) {
        if (principal == null)
            return ResponseEntity.status(401).body("Please login to verify payment");
        studentService.enrollPaid(principal.getName(), request);
        return ResponseEntity.ok("Payment verified and Enrolled successfully");
    }

    @GetMapping("/courses")
    public ResponseEntity<List<com.finallms.backend.dto.CourseDto.CourseResponse>> getEnrolledCourses(
            Principal principal) {
        if (principal == null)
            return ResponseEntity.status(401).body(java.util.Collections.emptyList());
        return ResponseEntity.ok(studentService.getEnrolledCourses(principal.getName()));
    }

    @GetMapping("/all-courses")
    public ResponseEntity<List<com.finallms.backend.dto.CourseDto.CourseResponse>> getAllCourses() {
        // Reuse service method or add new one.
        // studentService doesn't have getAllCourses. using courseRepo directly or
        // adding method to studentService.
        // Better to add to StudentService.
        return ResponseEntity.ok(studentService.getAllCourses());
    }

    @GetMapping("/videos/{id}/play")
    public ResponseEntity<VideoDto.SignedUrlResponse> getVideoSignedUrl(Principal principal, @PathVariable Long id,
            @RequestParam(required = false) String quality) {
        if (principal == null)
            return ResponseEntity.status(401).build();
        if (quality == null || quality.isBlank()) {
            return ResponseEntity.ok(studentService.getVideoSignedUrl(principal.getName(), id));
        }
        return ResponseEntity.ok(studentService.getVideoSignedUrl(principal.getName(), id, quality));
    }

    @PostMapping("/videos/{id}/complete")
    public ResponseEntity<?> markVideoComplete(Principal principal, @PathVariable Long id) {
        if (principal == null)
            return ResponseEntity.status(401).body(java.util.Map.of("status", "unauthorized"));
        studentService.completeVideo(principal.getName(), id);
        return ResponseEntity.ok(java.util.Map.of("status", "ok"));
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<CourseDto.CourseResponse> getCourseContent(Principal principal, @PathVariable Long courseId) {
        if (principal == null)
            return ResponseEntity.status(401).build();
        return ResponseEntity.ok(studentService.getCourseContent(principal.getName(), courseId));
    }

    @GetMapping("/assignments/{id}/file")
    public ResponseEntity<?> getAssignmentFile(Principal principal, @PathVariable Long id) {
        if (principal == null)
            return ResponseEntity.status(401).body(java.util.Map.of("status", "unauthorized"));
        String fileUrl = studentService.getAssignmentFileUrl(principal.getName(), id);
        return ResponseEntity.ok(java.util.Map.of("fileUrl", fileUrl));
    }

    @PostMapping("/assignments/{id}/submit-text")
    public ResponseEntity<?> submitTextAssignment(Principal principal, @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        if (principal == null)
            return ResponseEntity.status(401).body("Unauthorized");
        String text = body != null ? body.get("text") : null;
        studentService.submitTextAssignment(principal.getName(), id, text);
        return ResponseEntity.ok("Submitted");
    }

    // EXAM OPERATIONS
    @Autowired
    private com.finallms.backend.service.ExamService examService;

    @PostMapping("/exams/{id}/start")
    public ResponseEntity<?> startExam(Principal principal, @PathVariable Long id) {
        if (principal == null)
            return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(examService.startExam(id, principal.getName()));
    }

    @PostMapping("/exams/submit")
    public ResponseEntity<?> submitExam(Principal principal,
            @RequestBody com.finallms.backend.dto.ExamSubmissionDto.SubmitExamRequest request) {
        if (principal == null)
            return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(examService.submitExam(request));
    }

    @PostMapping("/exams/upload")
    public ResponseEntity<?> uploadExamFile(Principal principal,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        if (principal == null)
            return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(java.util.Map.of("fileKey", studentService.uploadExamFile(file)));
    }
}
