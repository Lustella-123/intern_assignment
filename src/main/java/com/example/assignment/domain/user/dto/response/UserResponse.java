package com.example.assignment.domain.user.dto.response;

import com.example.assignment.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private String username;
    private String nickname;
    private UserResponseRole roles;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .username(user.getUsername())
                .nickname(user.getNickname())
                .roles(new UserResponseRole(user.getRole()))
                .build();
    }
}
