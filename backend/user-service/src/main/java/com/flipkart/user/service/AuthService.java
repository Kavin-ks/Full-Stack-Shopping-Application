package com.flipkart.user.service;

import com.flipkart.user.dto.Dtos.*;
import com.flipkart.user.exception.ApiException;
import com.flipkart.user.model.OtpCode;
import com.flipkart.user.model.User;
import com.flipkart.user.repository.OtpRepository;
import com.flipkart.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Flipkart-style passwordless login: the user enters an e-mail or mobile number, receives a 6-digit OTP,
 * and verifying it both logs in existing users and signs up new ones.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern MOBILE = Pattern.compile("^[6-9]\\d{9}$");
    private static final int OTP_TTL_SECONDS = 300;
    private static final int MAX_ATTEMPTS = 5;

    private final UserRepository users;
    private final OtpRepository otps;
    private final JwtService jwt;
    private final boolean exposeOtp;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository users, OtpRepository otps, JwtService jwt,
                       @Value("${app.otp.expose-in-response}") boolean exposeOtp) {
        this.users = users;
        this.otps = otps;
        this.jwt = jwt;
        this.exposeOtp = exposeOtp;
    }

    public OtpResponse requestOtp(String rawIdentifier) {
        String identifier = normalize(rawIdentifier);
        String code = String.format("%06d", random.nextInt(1_000_000));
        otps.save(new OtpCode(identifier, code, Instant.now().plusSeconds(OTP_TTL_SECONDS)));
        log.info("OTP for {} is {}", identifier, code);
        boolean newUser = findUser(identifier).isEmpty();
        return new OtpResponse(identifier, newUser, exposeOtp ? code : null, OTP_TTL_SECONDS);
    }

    public AuthResponse verify(VerifyRequest req) {
        String identifier = normalize(req.identifier());
        OtpCode otp = otps.findById(identifier)
                .orElseThrow(() -> ApiException.badRequest("Please request an OTP first"));
        if (otp.getExpiresAt().isBefore(Instant.now())) {
            otps.delete(otp);
            throw ApiException.badRequest("OTP has expired. Please request a new one");
        }
        if (!otp.getCode().equals(req.otp().trim())) {
            otp.setAttempts(otp.getAttempts() + 1);
            if (otp.getAttempts() >= MAX_ATTEMPTS) {
                otps.delete(otp);
                throw ApiException.badRequest("Too many wrong attempts. Please request a new OTP");
            }
            otps.save(otp);
            throw ApiException.badRequest("Incorrect OTP. Please try again");
        }

        User user = findUser(identifier).orElseGet(() -> {
            if (!StringUtils.hasText(req.name())) {
                throw ApiException.badRequest("Please enter your name to create an account");
            }
            User u = new User();
            u.setName(req.name().trim());
            if (identifier.contains("@")) u.setEmail(identifier);
            else u.setMobile(identifier);
            return users.save(u);
        });
        otps.delete(otp);
        return new AuthResponse(jwt.issue(user), toView(user));
    }

    public static UserView toView(User u) {
        return new UserView(u.getId(), u.getName(), u.getEmail(), u.getMobile(), u.getGender(), u.getRole());
    }

    private Optional<User> findUser(String identifier) {
        return identifier.contains("@") ? users.findByEmail(identifier) : users.findByMobile(identifier);
    }

    private String normalize(String raw) {
        String value = raw == null ? "" : raw.trim().toLowerCase();
        if (EMAIL.matcher(value).matches()) return value;
        String digits = value.replaceAll("[\\s-]", "");
        if (digits.startsWith("+91")) digits = digits.substring(3);
        if (MOBILE.matcher(digits).matches()) return digits;
        throw ApiException.badRequest("Please enter valid Email ID/Mobile number");
    }
}
