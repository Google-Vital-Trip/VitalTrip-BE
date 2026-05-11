package com.vitaltrip.auth.service;

import com.vitaltrip.auth.dto.*;
import com.vitaltrip.common.exception.AppException;
import com.vitaltrip.common.response.ErrorCode;
import com.vitaltrip.common.security.JwtUtil;
import com.vitaltrip.user.entity.User;
import com.vitaltrip.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    // ── checkEmail ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("이메일 사용 가능 - 미존재 시 true 반환")
    void checkEmail_미존재_true() {
        given(userRepository.existsByEmail("new@example.com")).willReturn(false);

        assertThat(authService.checkEmail("new@example.com")).isTrue();
    }

    @Test
    @DisplayName("이메일 중복 - 이미 존재 시 false 반환")
    void checkEmail_중복_false() {
        given(userRepository.existsByEmail("exists@example.com")).willReturn(true);

        assertThat(authService.checkEmail("exists@example.com")).isFalse();
    }

    // ── signup ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("회원가입 성공")
    void signup_성공() {
        SignupRequest req = signupRequest("user@example.com", "Pass1@word", "Pass1@word");
        given(userRepository.existsByEmail(req.getEmail())).willReturn(false);
        given(passwordEncoder.encode(req.getPassword())).willReturn("encoded");

        authService.signup(req);

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 불일치")
    void signup_비밀번호_불일치_예외() {
        SignupRequest req = signupRequest("user@example.com", "Pass1@word", "Different1@");

        assertThatThrownBy(() -> authService.signup(req))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode())
                        .isEqualTo(ErrorCode.VALIDATION_FAILED));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signup_이메일_중복_예외() {
        SignupRequest req = signupRequest("dup@example.com", "Pass1@word", "Pass1@word");
        given(userRepository.existsByEmail(req.getEmail())).willReturn(true);

        assertThatThrownBy(() -> authService.signup(req))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode())
                        .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS));
    }

    // ── login ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("로그인 성공 - 토큰 반환")
    void login_성공() {
        LoginRequest req = new LoginRequest();
        setField(req, "email", "user@example.com");
        setField(req, "password", "Pass1@word");

        User user = localUser(1L, "user@example.com");
        given(userRepository.findByEmail(req.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(req.getPassword(), user.getPassword())).willReturn(true);
        given(jwtUtil.generateAccessToken(1L, "user@example.com")).willReturn("access-token");
        given(jwtUtil.generateRefreshToken(1L, "user@example.com")).willReturn("refresh-token");

        LoginResponse response = authService.login(req);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUser().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("로그인 실패 - 유저 없음")
    void login_유저없음_예외() {
        LoginRequest req = new LoginRequest();
        setField(req, "email", "ghost@example.com");
        setField(req, "password", "Pass1@word");
        given(userRepository.findByEmail(req.getEmail())).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    @DisplayName("로그인 실패 - 구글 소셜 계정")
    void login_구글계정_예외() {
        LoginRequest req = new LoginRequest();
        setField(req, "email", "google@example.com");
        setField(req, "password", "Pass1@word");

        User googleUser = User.builder()
                .id(2L).email("google@example.com").name("Google User")
                .birthDate("2000-01-01").countryCode("KR").phoneNumber("+821012345678")
                .provider(User.Provider.GOOGLE).role(User.Role.USER).build();
        given(userRepository.findByEmail(req.getEmail())).willReturn(Optional.of(googleUser));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_비밀번호_불일치_예외() {
        LoginRequest req = new LoginRequest();
        setField(req, "email", "user@example.com");
        setField(req, "password", "WrongPass1@");

        User user = localUser(1L, "user@example.com");
        given(userRepository.findByEmail(req.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(req.getPassword(), user.getPassword())).willReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    // ── logout ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("로그아웃 성공 - 리프레시 토큰 null 처리")
    void logout_성공() {
        User user = localUser(1L, "user@example.com");
        user.setRefreshToken("existing-refresh-token");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        authService.logout(1L);

        assertThat(user.getRefreshToken()).isNull();
    }

    // ── refresh ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("토큰 갱신 성공")
    void refresh_성공() {
        RefreshRequest req = new RefreshRequest();
        setField(req, "refreshToken", "valid-refresh");

        User user = localUser(1L, "user@example.com");
        user.setRefreshToken("valid-refresh");

        Claims claims = mock(Claims.class);
        given(claims.getSubject()).willReturn("1");

        given(jwtUtil.validateRefreshToken("valid-refresh")).willReturn(true);
        given(jwtUtil.parseRefreshToken("valid-refresh")).willReturn(claims);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(jwtUtil.generateAccessToken(1L, "user@example.com")).willReturn("new-access");

        Map<String, String> result = authService.refresh(req);

        assertThat(result.get("accessToken")).isEqualTo("new-access");
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 유효하지 않은 토큰")
    void refresh_유효하지않은_토큰_예외() {
        RefreshRequest req = new RefreshRequest();
        setField(req, "refreshToken", "invalid-token");
        given(jwtUtil.validateRefreshToken("invalid-token")).willReturn(false);

        assertThatThrownBy(() -> authService.refresh(req))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode())
                        .isEqualTo(ErrorCode.UNAUTHORIZED));
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 저장된 토큰과 불일치")
    void refresh_토큰_불일치_예외() {
        RefreshRequest req = new RefreshRequest();
        setField(req, "refreshToken", "submitted-token");

        User user = localUser(1L, "user@example.com");
        user.setRefreshToken("stored-different-token");

        Claims claims = mock(Claims.class);
        given(claims.getSubject()).willReturn("1");

        given(jwtUtil.validateRefreshToken("submitted-token")).willReturn(true);
        given(jwtUtil.parseRefreshToken("submitted-token")).willReturn(claims);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.refresh(req))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode())
                        .isEqualTo(ErrorCode.UNAUTHORIZED));
    }

    // ── changePassword ───────────────────────────────────────────────────────

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_성공() {
        PasswordChangeRequest req = new PasswordChangeRequest();
        setField(req, "currentPassword", "Old1@pass");
        setField(req, "newPassword", "New1@pass");
        setField(req, "newPasswordConfirm", "New1@pass");

        User user = localUser(1L, "user@example.com");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("Old1@pass", user.getPassword())).willReturn(true);
        given(passwordEncoder.encode("New1@pass")).willReturn("new-encoded");

        authService.changePassword(1L, req);

        assertThat(user.getPassword()).isEqualTo("new-encoded");
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 소셜 계정")
    void changePassword_소셜계정_예외() {
        PasswordChangeRequest req = new PasswordChangeRequest();
        setField(req, "currentPassword", "Old1@pass");
        setField(req, "newPassword", "New1@pass");
        setField(req, "newPasswordConfirm", "New1@pass");

        User googleUser = User.builder()
                .id(1L).email("g@example.com").name("G User")
                .birthDate("2000-01-01").countryCode("KR").phoneNumber("+821012345678")
                .provider(User.Provider.GOOGLE).role(User.Role.USER).build();
        given(userRepository.findById(1L)).willReturn(Optional.of(googleUser));

        assertThatThrownBy(() -> authService.changePassword(1L, req))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode())
                        .isEqualTo(ErrorCode.VALIDATION_FAILED));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호 불일치")
    void changePassword_새비밀번호_불일치_예외() {
        PasswordChangeRequest req = new PasswordChangeRequest();
        setField(req, "currentPassword", "Old1@pass");
        setField(req, "newPassword", "New1@pass");
        setField(req, "newPasswordConfirm", "Different1@");

        User user = localUser(1L, "user@example.com");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.changePassword(1L, req))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode())
                        .isEqualTo(ErrorCode.VALIDATION_FAILED));
    }

    // ── adminLogin ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("관리자 로그인 성공")
    void adminLogin_성공() {
        AdminLoginRequest req = new AdminLoginRequest();
        setField(req, "email", "admin@example.com");
        setField(req, "password", "Admin1@pass");

        User admin = adminUser(10L, "admin@example.com");
        given(userRepository.findByEmail(req.getEmail())).willReturn(Optional.of(admin));
        given(passwordEncoder.matches(req.getPassword(), admin.getPassword())).willReturn(true);
        given(jwtUtil.generateAccessToken(10L, "admin@example.com")).willReturn("admin-access");
        given(jwtUtil.generateRefreshToken(10L, "admin@example.com")).willReturn("admin-refresh");

        Map<String, String> result = authService.adminLogin(req);

        assertThat(result.get("accessToken")).isEqualTo("admin-access");
        assertThat(result.get("refreshToken")).isEqualTo("admin-refresh");
    }

    @Test
    @DisplayName("관리자 로그인 실패 - ADMIN 권한 없음")
    void adminLogin_권한없음_예외() {
        AdminLoginRequest req = new AdminLoginRequest();
        setField(req, "email", "user@example.com");
        setField(req, "password", "Pass1@word");

        User user = localUser(1L, "user@example.com");
        given(userRepository.findByEmail(req.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(req.getPassword(), user.getPassword())).willReturn(true);

        assertThatThrownBy(() -> authService.adminLogin(req))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode())
                        .isEqualTo(ErrorCode.FORBIDDEN));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private SignupRequest signupRequest(String email, String password, String confirm) {
        SignupRequest req = new SignupRequest();
        setField(req, "email", email);
        setField(req, "name", "Test User");
        setField(req, "password", password);
        setField(req, "passwordConfirm", confirm);
        setField(req, "birthDate", "2000-01-01");
        setField(req, "countryCode", "KR");
        setField(req, "phoneNumber", "+821012345678");
        return req;
    }

    private User localUser(Long id, String email) {
        return User.builder()
                .id(id).email(email).name("Test User")
                .password("encoded-pass")
                .birthDate("2000-01-01").countryCode("KR").phoneNumber("+821012345678")
                .provider(User.Provider.LOCAL).role(User.Role.USER).build();
    }

    private User adminUser(Long id, String email) {
        return User.builder()
                .id(id).email(email).name("Admin User")
                .password("encoded-pass")
                .birthDate("1990-01-01").countryCode("KR").phoneNumber("+821099999999")
                .provider(User.Provider.LOCAL).role(User.Role.ADMIN).build();
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
