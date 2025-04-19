package com.example.assignment.domain.user.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.example.assignment.config.JwtUtil;
import com.example.assignment.domain.user.dto.request.LoginRequest;
import com.example.assignment.domain.user.dto.request.SignupRequest;
import com.example.assignment.domain.user.dto.response.UserResponse;
import com.example.assignment.domain.user.entity.User;
import com.example.assignment.domain.user.enums.UserRole;
import com.example.assignment.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public UserResponse signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 가입된 사용자입니다.");
        }

        String hashPw = BCrypt.withDefaults().hashToString(12, request.getPassword().toCharArray());

        User user = User.builder()
                .username(request.getUsername())
                .password(hashPw)
                .nickname(request.getNickname())
                .role(UserRole.USER)
                .build();

        userRepository.save(user);
        return UserResponse.from(user);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!BCrypt.verifyer().verify(request.getPassword().toCharArray(), user.getPassword()).verified) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        return jwtUtil.createToken(user.getId(), user.getUsername(), user.getRole());
    }

    public UserResponse promoteToAdmin(Long userId, String role) {
        if (!"ADMIN".equals(role)) {
            throw new IllegalArgumentException("관리자 권한이 필요한 요청입니다. 접근 권한이 없습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setRole(UserRole.ADMIN);
        return UserResponse.from(user);
    }
}
