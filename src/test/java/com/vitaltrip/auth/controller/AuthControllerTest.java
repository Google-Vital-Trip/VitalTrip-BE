package com.vitaltrip.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitaltrip.auth.dto.LoginRequest;
import com.vitaltrip.auth.dto.LoginResponse;
import com.vitaltrip.auth.service.AuthService;
import com.vitaltrip.common.exception.AppException;
import com.vitaltrip.common.exception.GlobalExceptionHandler;
import com.vitaltrip.common.response.ErrorCode;
import com.vitaltrip.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── checkEmail ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/auth/check-email - 사용 가능한 이메일")
    void checkEmail_사용가능() throws Exception {
        given(authService.checkEmail("new@example.com")).willReturn(true);

        mockMvc.perform(get("/api/auth/check-email")
                        .param("email", "new@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.data.available").value(true));
    }

    @Test
    @DisplayName("GET /api/auth/check-email - 중복 이메일")
    void checkEmail_중복() throws Exception {
        given(authService.checkEmail("dup@example.com")).willReturn(false);

        mockMvc.perform(get("/api/auth/check-email")
                        .param("email", "dup@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(false));
    }

    // ── signup ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/signup - 정상 회원가입 201")
    void signup_성공_201() throws Exception {
        Map<String, String> body = Map.of(
                "email", "user@example.com",
                "name", "Test User",
                "password", "Pass1@word",
                "passwordConfirm", "Pass1@word",
                "birthDate", "2000-01-01",
                "countryCode", "KR",
                "phoneNumber", "+821012345678"
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("성공"));
    }

    @Test
    @DisplayName("POST /api/auth/signup - 이메일 형식 오류 시 400")
    void signup_이메일형식오류_400() throws Exception {
        Map<String, String> body = Map.of(
                "email", "not-an-email",
                "name", "Test User",
                "password", "Pass1@word",
                "passwordConfirm", "Pass1@word",
                "birthDate", "2000-01-01",
                "countryCode", "KR",
                "phoneNumber", "+821012345678"
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("POST /api/auth/signup - 비밀번호 정규식 불통 시 400")
    void signup_비밀번호정규식위반_400() throws Exception {
        Map<String, String> body = Map.of(
                "email", "user@example.com",
                "name", "Test User",
                "password", "short",  // 8자 미만, 특수문자 없음
                "passwordConfirm", "short",
                "birthDate", "2000-01-01",
                "countryCode", "KR",
                "phoneNumber", "+821012345678"
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("POST /api/auth/signup - 이메일 중복 시 409")
    void signup_이메일중복_409() throws Exception {
        doThrow(new AppException(ErrorCode.EMAIL_ALREADY_EXISTS))
                .when(authService).signup(any());

        Map<String, String> body = Map.of(
                "email", "dup@example.com",
                "name", "Test User",
                "password", "Pass1@word",
                "passwordConfirm", "Pass1@word",
                "birthDate", "2000-01-01",
                "countryCode", "KR",
                "phoneNumber", "+821012345678"
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("EMAIL_ALREADY_EXISTS"));
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/login - 로그인 성공 200")
    void login_성공_200() throws Exception {
        User user = User.builder()
                .id(1L).email("user@example.com").name("Test User")
                .birthDate("2000-01-01").countryCode("KR").phoneNumber("+821012345678")
                .provider(User.Provider.LOCAL).role(User.Role.USER).build();

        given(authService.login(any()))
                .willReturn(new LoginResponse("access-token", "refresh-token", user));

        Map<String, String> body = Map.of(
                "email", "user@example.com",
                "password", "Pass1@word"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.user.email").value("user@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login - 자격증명 오류 시 401")
    void login_자격증명오류_401() throws Exception {
        given(authService.login(any()))
                .willThrow(new AppException(ErrorCode.INVALID_CREDENTIALS));

        Map<String, String> body = Map.of(
                "email", "user@example.com",
                "password", "WrongPass1@"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("POST /api/auth/login - 필수 필드 누락 시 400")
    void login_필수필드_누락_400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ── refresh ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/refresh - 토큰 갱신 성공")
    void refresh_성공_200() throws Exception {
        given(authService.refresh(any()))
                .willReturn(Map.of("accessToken", "new-access-token"));

        Map<String, String> body = Map.of("refreshToken", "valid-refresh-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - 유효하지 않은 토큰 401")
    void refresh_유효하지않은토큰_401() throws Exception {
        given(authService.refresh(any()))
                .willThrow(new AppException(ErrorCode.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."));

        Map<String, String> body = Map.of("refreshToken", "bad-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }
}
