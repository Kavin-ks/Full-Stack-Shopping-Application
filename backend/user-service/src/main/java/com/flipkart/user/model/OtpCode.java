package com.flipkart.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** One pending OTP per e-mail / mobile number. */
@Document(collection = "otp_codes")
public class OtpCode {

    @Id
    private String identifier;
    private String code;
    private Instant expiresAt;
    private int attempts;

    public OtpCode() {}

    public OtpCode(String identifier, String code, Instant expiresAt) {
        this.identifier = identifier;
        this.code = code;
        this.expiresAt = expiresAt;
    }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
}
