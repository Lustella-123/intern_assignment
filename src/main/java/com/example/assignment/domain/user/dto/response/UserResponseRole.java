package com.example.assignment.domain.user.dto.response;

import com.example.assignment.domain.user.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponseRole {
    private UserRole role;
}
