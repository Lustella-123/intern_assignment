package com.example.assignment;

import com.example.assignment.config.JwtUtil;
import com.example.assignment.domain.user.dto.request.LoginRequest;
import com.example.assignment.domain.user.dto.request.SignupRequest;
import com.example.assignment.domain.user.entity.User;
import com.example.assignment.domain.user.enums.UserRole;
import com.example.assignment.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserApiTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JwtUtil jwtUtil;

	@BeforeEach
	void setup() {
		userRepository.deleteAll();
	}

	@Test
	@DisplayName("회원가입 성공")
	void signup_success() throws Exception {
		SignupRequest request = new SignupRequest("testuser", "password", "tester");

		mockMvc.perform(post("/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("testuser"));
	}

	@Test
	@DisplayName("회원가입 실패 - 이미 존재하는 사용자")
	void signup_fail_duplicate_user() throws Exception {
		userRepository.save(User.builder()
				.username("testuser")
				.password("hashed")
				.nickname("tester")
				.role(UserRole.USER)
				.build());

		SignupRequest request = new SignupRequest("testuser", "password", "tester");

		mockMvc.perform(post("/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("USER_ALREADY_EXISTS"));
	}

	@Test
	@DisplayName("로그인 성공")
	void login_success() throws Exception {
		String hashed = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults()
				.hashToString(12, "password".toCharArray());
		userRepository.save(User.builder()
				.username("testuser")
				.password(hashed)
				.nickname("tester")
				.role(UserRole.USER)
				.build());

		LoginRequest request = new LoginRequest("testuser", "password");

		mockMvc.perform(post("/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").exists());
	}

	@Test
	@DisplayName("로그인 실패 - 잘못된 자격")
	void login_fail_wrong_credentials() throws Exception {
		LoginRequest request = new LoginRequest("nouser", "wrongpass");

		mockMvc.perform(post("/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
	}

	@Test
	@DisplayName("관리자 권한 부여 성공")
	void promote_success() throws Exception {
		User admin = userRepository.save(User.builder()
				.username("admin")
				.password("pw")
				.nickname("관리자")
				.role(UserRole.ADMIN)
				.build());

		User user = userRepository.save(User.builder()
				.username("target")
				.password("pw")
				.nickname("대상")
				.role(UserRole.USER)
				.build());

		String token = jwtUtil.createToken(admin.getId(), admin.getUsername(), admin.getRole());

		mockMvc.perform(patch("/admin/users/" + user.getId() + "/roles")
						.header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("ADMIN"));
	}

	@Test
	@DisplayName("관리자 권한 부여 실패 - 일반 사용자 접근")
	void promote_fail_user_access() throws Exception {
		User user = userRepository.save(User.builder()
				.username("user")
				.password("pw")
				.nickname("일반")
				.role(UserRole.USER)
				.build());

		String token = jwtUtil.createToken(user.getId(), user.getUsername(), user.getRole());

		mockMvc.perform(patch("/admin/users/999/roles")
						.header("Authorization", token))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
	}

	@Test
	@DisplayName("관리자 권한 부여 실패 - 사용자 없음")
	void promote_fail_user_not_found() throws Exception {
		User admin = userRepository.save(User.builder()
				.username("admin")
				.password("pw")
				.nickname("관리자")
				.role(UserRole.ADMIN)
				.build());

		String token = jwtUtil.createToken(admin.getId(), admin.getUsername(), admin.getRole());

		mockMvc.perform(patch("/admin/users/9999/roles")
						.header("Authorization", token))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"));
	}
}
