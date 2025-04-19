package com.example.assignment.domain.user.dto.response;

import com.example.assignment.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private String username;
    private String nickname;
    private String role;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .build();
    }
}
