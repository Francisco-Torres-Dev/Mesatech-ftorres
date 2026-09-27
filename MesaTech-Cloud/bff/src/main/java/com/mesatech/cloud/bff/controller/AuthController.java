package com.mesatech.cloud.bff.controller;

import com.mesatech.cloud.bff.dto.AuthLoginRequest;
import com.mesatech.cloud.bff.dto.AuthUserDto;
import com.mesatech.cloud.bff.service.LocalAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class AuthController {
    private final LocalAuthService localAuthService;

    public AuthController(LocalAuthService localAuthService) {
        this.localAuthService = localAuthService;
    }

    @PostMapping("/auth/local/login")
    public ResponseEntity<AuthUserDto> login(@Valid @RequestBody AuthLoginRequest request) {
        return ResponseEntity.ok(localAuthService.login(request.getRol()));
    }
}
