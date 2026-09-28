package com.flipkart.user.controller;

import com.flipkart.user.dto.Dtos.*;
import com.flipkart.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/otp")
    public OtpResponse requestOtp(@Valid @RequestBody OtpRequest req) {
        return auth.requestOtp(req.identifier());
    }

    @PostMapping("/verify")
    public AuthResponse verify(@Valid @RequestBody VerifyRequest req) {
        return auth.verify(req);
    }
}
