package com.flipkart.user.dto;

import jakarta.validation.constraints.*;

public final class Dtos {
    private Dtos() {}

    public record OtpRequest(@NotBlank String identifier) {}

    public record OtpResponse(String identifier, boolean newUser, String otp, int expiresInSeconds) {}

    public record VerifyRequest(@NotBlank String identifier, @NotBlank String otp, String name) {}

    public record UserView(String id, String name, String email, String mobile, String gender, String role) {}

    public record AuthResponse(String token, UserView user) {}

    public record ProfileUpdate(@NotBlank @Size(max = 60) String name,
                                @Email String email,
                                @Pattern(regexp = "^$|^[6-9]\\d{9}$", message = "must be a valid 10-digit mobile number") String mobile,
                                String gender) {}

    public record AddressRequest(
            @NotBlank @Size(max = 60) String name,
            @NotBlank @Pattern(regexp = "^[6-9]\\d{9}$", message = "must be a valid 10-digit mobile number") String mobile,
            @NotBlank @Pattern(regexp = "^[1-9]\\d{5}$", message = "must be a valid 6-digit pincode") String pincode,
            @NotBlank String locality,
            @NotBlank @Size(max = 300) String address,
            @NotBlank String city,
            @NotBlank String state,
            String landmark,
            @Pattern(regexp = "^$|^[6-9]\\d{9}$", message = "must be a valid 10-digit mobile number") String alternatePhone,
            @Pattern(regexp = "HOME|WORK") String type,
            boolean makeDefault) {}
}
