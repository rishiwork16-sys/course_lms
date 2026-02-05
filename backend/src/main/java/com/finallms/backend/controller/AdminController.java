package com.finallms.backend.controller;

import com.finallms.backend.dto.*;
import com.finallms.backend.entity.*;
import com.finallms.backend.enums.*;
import com.finallms.backend.repository.*;
import com.finallms.backend.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private StudentService studentService;

    // Helper for JSON parsing
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(
            @RequestPart("course") String courseJson,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) throws IOException {

        CourseDto.CreateCourseRequest request = objectMapper.readValue(courseJson, CourseDto.CreateCourseRequest.class);
        return ResponseEntity.ok(courseService.createCourse(request, thumbnail));
    }

    @GetMapping("/courses")
    public ResponseEntity<List<?>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/courses/{id}")
    public ResponseEntity<CourseDto.CourseResponse> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseDetails(id));
    }

    @PostMapping("/modules")
    public ResponseEntity<?> createModule(@RequestBody ModuleDto.CreateModuleRequest request) {
        return ResponseEntity.ok(courseService.createModule(request));
    }

    @PostMapping("/videos")
    public ResponseEntity<?> addVideo(
            @RequestParam("moduleId") Long moduleId,
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(courseService.addVideo(moduleId, title, file));
    }

    @PostMapping("/assignments")
    public ResponseEntity<?> addAssignment(
            @RequestParam Long moduleId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String textContent,
            @RequestParam AssignmentType type,
            @RequestParam(value = "file", required = false) MultipartFile file,
            java.security.Principal principal) throws IOException {
        String createdBy = principal != null ? principal.getName() : null;
        Assignment assignment = courseService.addAssignment(moduleId, title, description, textContent, type, file,
                createdBy);
        return ResponseEntity.ok(assignment);
    }

    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> getReports() {
        return ResponseEntity.ok(reportService.getStats());
    }

    @GetMapping("/payments")
    public ResponseEntity<java.util.List<com.finallms.backend.dto.PaymentDto.AdminPaymentResponse>> getPayments(
            @RequestParam(value = "status", required = false) String status) {
        java.util.List<Payment> payments;
        if (status != null && !status.isBlank()) {
            PaymentStatus s = PaymentStatus.valueOf(status.toUpperCase());
            payments = paymentRepository.findTop50ByStatusOrderByCreatedAtDesc(s);
        } else {
            payments = paymentRepository.findTop50ByOrderByCreatedAtDesc();
        }
        java.util.List<com.finallms.backend.dto.PaymentDto.AdminPaymentResponse> resp = payments.stream().map(p -> {
            com.finallms.backend.dto.PaymentDto.AdminPaymentResponse r = new com.finallms.backend.dto.PaymentDto.AdminPaymentResponse();
            r.setId(p.getId());
            r.setOrderId(p.getOrderId());
            r.setPaymentId(p.getPaymentId());
            r.setSignature(p.getSignature());
            r.setAmount(p.getAmount());
            r.setStatus(p.getStatus() != null ? p.getStatus().name() : null);
            r.setUserName(p.getUserName());
            r.setUserId(p.getUser() != null ? p.getUser().getId() : null);
            r.setCourseId(p.getCourse() != null ? p.getCourse().getId() : null);
            r.setErrorReason(p.getErrorReason());
            r.setCreatedAt(p.getCreatedAt());
            r.setUpdatedAt(p.getUpdatedAt());
            return r;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    // UPDATE ENDPOINTS

    @PutMapping("/courses/{id}")
    public ResponseEntity<?> updateCourse(
            @PathVariable Long id,
            @RequestPart("course") String courseJson,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) throws IOException {
        CourseDto.UpdateCourseRequest request = objectMapper.readValue(courseJson, CourseDto.UpdateCourseRequest.class);
        return ResponseEntity.ok(courseService.updateCourse(id, request, thumbnail));
    }

    @PutMapping("/modules/{id}")
    public ResponseEntity<?> updateModule(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        courseService.updateModule(id, body.get("title"));
        return ResponseEntity.ok("Module updated successfully");
    }

    @PutMapping("/videos/{id}")
    public ResponseEntity<?> updateVideo(
            @PathVariable Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        return ResponseEntity.ok(courseService.updateVideo(id, title, file));
    }

    @PutMapping("/assignments/{id}")
    public ResponseEntity<?> updateAssignment(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String textContent,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        courseService.updateAssignment(id, title, description, textContent, file);
        return ResponseEntity.ok("Assignment updated successfully");
    }

    // DELETE ENDPOINTS

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok("Course deleted successfully");
    }

    @DeleteMapping("/modules/{id}")
    public ResponseEntity<?> deleteModule(@PathVariable Long id) {
        courseService.deleteModule(id);
        return ResponseEntity.ok("Module deleted successfully");
    }

    @DeleteMapping("/videos/{id}")
    public ResponseEntity<?> deleteVideo(@PathVariable Long id) {
        courseService.deleteVideo(id);
        return ResponseEntity.ok("Video deleted successfully");
    }

    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long id) {
        courseService.deleteAssignment(id);
        return ResponseEntity.ok("Assignment deleted successfully");
    }

    // VIDEO PREVIEW

    @GetMapping("/videos/{id}/preview")
    public ResponseEntity<VideoDto.SignedUrlResponse> previewVideo(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getVideoPreviewUrl(id));
    }

    @GetMapping("/assignments/{id}/file")
    public ResponseEntity<?> getAssignmentFile(@PathVariable Long id) {
        String fileUrl = courseService.getAssignmentFileUrl(id);
        return ResponseEntity.ok(Map.of("fileUrl", fileUrl));
    }

    @GetMapping("/students/{userId}")
    public ResponseEntity<?> getStudentDetails(@PathVariable Long userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Student not found"));
        var enrollments = enrollmentRepository.findByUser(user);
        var courseIds = enrollments.stream().map(e -> e.getCourse().getId()).collect(Collectors.toList());
        var enrolledCourses = enrollments.stream().map(e -> e.getCourse().getTitle()).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of(
                "courseIds", courseIds,
                "name", user.getName(),
                "email", user.getEmail(),
                "phone", user.getPhone(),
                "address", user.getAddress(),
                "enrolledCourses", enrolledCourses));
    }

    @GetMapping("/students")
    public ResponseEntity<List<Map<String, Object>>> getAllStudents() {
        var students = studentService.getAllStudents();
        var response = students.stream().map(u -> Map.<String, Object>of(
                "id", u.getId(),
                "name", u.getName(),
                "email", u.getEmail(),
                "phone", u.getPhone(),
                "address", u.getAddress())).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // STUDENT MANAGEMENT - Will be implemented when we add StudentService methods
    // Commenting out for now until StudentService is updated
    /*
     * @GetMapping("/students")
     * public ResponseEntity<?> getAllStudents() {
     * return ResponseEntity.ok(studentService.getAllStudents());
     * }
     * 
     * @GetMapping("/courses/{id}/students")
     * public ResponseEntity<?> getCourseStudents(@PathVariable Long id) {
     * return ResponseEntity.ok(studentService.getCourseStudents(id));
     * }
     * 
     * @DeleteMapping("/enrollments/{id}")
     * public ResponseEntity<?> removeEnrollment(@PathVariable Long id) {
     * studentService.removeEnrollment(id);
     * return ResponseEntity.ok("Student removed from course successfully");
     * }
     */
    // EXAM MANAGEMENT

    @Autowired
    private com.finallms.backend.service.ExamService examService;

    @PostMapping("/exams")
    public ResponseEntity<?> createExam(@RequestBody ExamDto.CreateExamRequest request) {
        return ResponseEntity.ok(examService.createExam(request));
    }

    @GetMapping("/exams/{id}")
    public ResponseEntity<ExamDto.ExamResponse> getExam(@PathVariable Long id) {
        return ResponseEntity.ok(examService.getExam(id));
    }

    @PutMapping("/exams/{id}")
    public ResponseEntity<ExamDto.ExamResponse> updateExam(@PathVariable Long id,
            @RequestBody ExamDto.CreateExamRequest request) {
        return ResponseEntity.ok(examService.updateExam(id, request));
    }

    @DeleteMapping("/exams/{id}")
    public ResponseEntity<?> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.ok("Exam deleted successfully");
    }

    @PostMapping("/exams/{examId}/questions")
    public ResponseEntity<?> addQuestion(@PathVariable Long examId,
            @RequestBody QuestionDto.CreateQuestionRequest request) {
        return ResponseEntity.ok(examService.addQuestion(examId, request));
    }
}
