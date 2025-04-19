package com.example.assignment.domain.user.dto.request;

import lombok.Getter;

@Getter
public class SignupRequest {
    private String username;
    private String password;
    private String nickname;
}