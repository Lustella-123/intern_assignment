package com.example.assignment.domain.user.controller;

import com.example.assignment.domain.user.dto.request.LoginRequest;
import com.example.assignment.domain.user.dto.request.SignupRequest;
import com.example.assignment.domain.user.dto.response.TokenResponse;
import com.example.assignment.domain.user.dto.response.UserResponse;
import com.example.assignment.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@RequestBody SignupRequest request) {
        return ResponseEntity.ok(userService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(new TokenResponse(userService.login(request)));
    }

    @PatchMapping("/admin/users/{userId}/roles")
    public ResponseEntity<UserResponse> promoteToAdmin(@PathVariable Long userId, Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        return ResponseEntity.ok(userService.promoteToAdmin(userId, role));
    }
}
