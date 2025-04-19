package com.example.assignment;

import com.example.assignment.config.JwtUtil;
import com.example.assignment.domain.user.dto.request.LoginRequest;
import com.example.assignment.domain.user.dto.request.SignupRequest;
import com.example.assignment.domain.user.dto.response.UserResponse;
import com.example.assignment.domain.user.entity.User;
import com.example.assignment.domain.user.enums.UserRole;
import com.example.assignment.domain.user.repository.UserRepository;
import com.example.assignment.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserApiTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private JwtUtil jwtUtil;

	@InjectMocks
	private UserService userService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("회원가입 성공")
	void signup_success() {
		SignupRequest request = new SignupRequest("testuser", "password", "tester");
		when(userRepository.existsByUsername("testuser")).thenReturn(false);

		UserResponse response = userService.signup(request);

		assertThat(response.getUsername()).isEqualTo("testuser");
		assertThat(response.getNickname()).isEqualTo("tester");
		verify(userRepository).save(any(User.class));
	}

	@Test
	@DisplayName("회원가입 실패 - 중복 유저")
	void signup_fail_duplicate() {
		SignupRequest request = new SignupRequest("testuser", "password", "tester");
		when(userRepository.existsByUsername("testuser")).thenReturn(true);

		assertThatThrownBy(() -> userService.signup(request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("이미 가입된 사용자입니다.");
	}

	@Test
	@DisplayName("로그인 성공")
	void login_success() {
		String hashPw = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults()
				.hashToString(12, "password".toCharArray());

		User user = User.builder()
				.id(1L)
				.username("testuser")
				.password(hashPw)
				.role(UserRole.USER)
				.build();

		when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
		when(jwtUtil.createToken(1L, "testuser", UserRole.USER)).thenReturn("token");

		String token = userService.login(new LoginRequest("testuser", "password"));
		assertThat(token).isEqualTo("token");
	}

	@Test
	@DisplayName("로그인 실패 - 아이디 없음")
	void login_fail_no_user() {
		when(userRepository.findByUsername("nouser")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.login(new LoginRequest("nouser", "pass")))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("아이디 또는 비밀번호가 올바르지 않습니다.");
	}

	@Test
	@DisplayName("로그인 실패 - 비밀번호 불일치")
	void login_fail_wrong_password() {
		String hashPw = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults()
				.hashToString(12, "correctpassword".toCharArray());

		User user = User.builder()
				.id(1L)
				.username("testuser")
				.password(hashPw)
				.role(UserRole.USER)
				.build();

		when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> userService.login(new LoginRequest("testuser", "wrongpass")))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("아이디 또는 비밀번호가 올바르지 않습니다.");
	}

	@Test
	@DisplayName("관리자 권한 부여 성공")
	void promote_to_admin_success() {
		User user = User.builder()
				.id(2L)
				.username("target")
				.role(UserRole.USER)
				.build();

		when(userRepository.findById(2L)).thenReturn(Optional.of(user));

		UserResponse response = userService.promoteToAdmin(2L, "ADMIN");
		assertThat(response.getRoles().getRole()).isEqualTo(UserRole.ADMIN);
	}

	@Test
	@DisplayName("관리자 권한 부여 실패 - 일반 사용자 접근")
	void promote_to_admin_fail_not_admin() {
		assertThatThrownBy(() -> userService.promoteToAdmin(1L, "USER"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("관리자 권한이 필요한 요청입니다. 접근 권한이 없습니다.");
	}

	@Test
	@DisplayName("관리자 권한 부여 실패 - 유저 없음")
	void promote_to_admin_fail_user_not_found() {
		when(userRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.promoteToAdmin(999L, "ADMIN"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("사용자를 찾을 수 없습니다.");
	}
}