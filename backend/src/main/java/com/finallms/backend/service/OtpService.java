package com.finallms.backend.service;

import com.finallms.backend.exception.BadRequestException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OtpService {

    @Value("${fast2sms.api.key}")
    private String apiKey;

    private static final String DLT_TEMPLATE_ID = "182903";
    private static final String FAST2SMS_URL = "https://www.fast2sms.com/dev/bulkV2";
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final int MAX_SENDS_PER_HOUR = 5;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int BLOCK_MINUTES_ON_ABUSE = 15;

    // Cache: Key = Phone, Value = OTP
    // Expires 15 minutes after write (as requested)
    private Cache<String, String> otpCache = Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    // Per-phone cooldown to prevent rapid resend
    private Cache<String, Boolean> resendCooldown = Caffeine.newBuilder()
            .expireAfterWrite(RESEND_COOLDOWN_SECONDS, TimeUnit.SECONDS)
            .maximumSize(5000)
            .build();

    // Per-hour send counter
    private Cache<String, AtomicInteger> sendCounterHour = Caffeine.newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    // Failed validation attempts
    private Cache<String, AtomicInteger> failedAttempts = Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    // Blocked phones due to abuse
    private Cache<String, Boolean> blockedPhones = Caffeine.newBuilder()
            .expireAfterWrite(BLOCK_MINUTES_ON_ABUSE, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.contains("placeholder")) {
            System.out.println("Fast2SMS API Key not configured. Using Mock Mode.");
        }
    }

    public String generateAndSendOtp(String phone) {
        String normalizedPhone = phone.replaceAll("[^0-9]", "").trim();
        if (blockedPhones.getIfPresent(normalizedPhone) != null) {
            throw new BadRequestException("Too many OTP attempts. Try again later.");
        }
        if (resendCooldown.getIfPresent(normalizedPhone) != null) {
            throw new BadRequestException("Please wait before requesting OTP again.");
        }
        AtomicInteger counter = sendCounterHour.get(normalizedPhone, k -> new AtomicInteger(0));
        if (counter.get() >= MAX_SENDS_PER_HOUR) {
            blockedPhones.put(normalizedPhone, Boolean.TRUE);
            throw new BadRequestException("OTP request limit reached. Try after some time.");
        }
        counter.incrementAndGet();
        resendCooldown.put(normalizedPhone, Boolean.TRUE);
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Store in Cache (Overwrites existing if any)
        otpCache.put(normalizedPhone, otp);

        System.out.println("OTP generated for " + normalizedPhone + " at " + java.time.LocalDateTime.now());

        boolean ans = sendSms(normalizedPhone, otp);
        return ans ? otp : "something wene Wrong";
        // return otp;
    }

    public boolean validateOtp(String phone, String otp) {
        String normalizedPhone = phone.replaceAll("[^0-9]", "").trim();
        String normalizedOtp = otp.replaceAll("[^0-9]", "").trim();
        String cachedOtp = otpCache.getIfPresent(normalizedPhone);

        if (blockedPhones.getIfPresent(normalizedPhone) != null) {
            return false;
        }

        if (cachedOtp != null && cachedOtp.trim().equals(normalizedOtp)) {
            // Invalidate after successful use (One-time use)
            otpCache.invalidate(normalizedPhone);
            failedAttempts.invalidate(normalizedPhone);
            return true;
        }
        AtomicInteger fails = failedAttempts.get(normalizedPhone, k -> new AtomicInteger(0));
        if (fails.incrementAndGet() >= MAX_FAILED_ATTEMPTS) {
            blockedPhones.put(normalizedPhone, Boolean.TRUE);
            otpCache.invalidate(normalizedPhone);
        }
        return false;
    }

    private boolean sendSms(String toPhone, String otp) {
        if (apiKey == null || apiKey.contains("placeholder")) {
            System.out.println("SIMULATED SMS to " + toPhone);
            return false;
        }

        try {
            String message = "Your LMS Login OTP is " + otp;
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);

            String url = FAST2SMS_URL
                    + "?authorization=" + apiKey
                    + "&route=dlt"
                    + "&sender_id=SKLDUP"
                    + "&message=" + DLT_TEMPLATE_ID
                    + "&variables_values=" + otp
                    + "&numbers=" + toPhone
                    + "&flash=0";

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            System.out.println("Fast2SMS Response: " + response.body());
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send OTP via Fast2SMS: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
