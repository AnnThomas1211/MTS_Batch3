package com.fidelity.mts.controller;

import com.fidelity.mts.application.dto.UserLoginRequest;
import com.fidelity.mts.application.dto.UserLoginResponse;
import com.fidelity.mts.application.dto.UserRegistrationRequest;
import com.fidelity.mts.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * POST /api/v1/users/register
     * Creates a new user and a linked account with balance 0.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationRequest request) {
        String message = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", message));
    }

    /**
     * POST /api/v1/users/login
     * Authenticates a user and returns their accountId and holderName.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequest request) {
        UserLoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }
}
