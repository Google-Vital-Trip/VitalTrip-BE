package com.vitaltrip.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitaltrip.common.exception.AppException;
import com.vitaltrip.common.exception.GlobalExceptionHandler;
import com.vitaltrip.common.response.ErrorCode;
import com.vitaltrip.user.dto.ProfileResponse;
import com.vitaltrip.user.entity.User;
import com.vitaltrip.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .name("Test User")
                .birthDate("2000-01-01")
                .countryCode("KR")
                .phoneNumber("+821012345678")
                .provider(User.Provider.LOCAL)
                .role(User.Role.USER)
                .build();

        // standaloneSetup 환경에서 @AuthenticationPrincipal 주입을 위해 SecurityContext 설정
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /api/user/profile - 프로필 조회 성공 200")
    void getProfile_성공_200() throws Exception {
        given(userService.getProfile(1L)).willReturn(new ProfileResponse(testUser));

        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.name").value("Test User"))
                .andExpect(jsonPath("$.data.countryCode").value("KR"))
                .andExpect(jsonPath("$.data.provider").value("LOCAL"));
    }

    @Test
    @DisplayName("GET /api/user/profile - 유저 없음 404")
    void getProfile_유저없음_404() throws Exception {
        given(userService.getProfile(1L))
                .willThrow(new AppException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("PUT /api/user/profile - 프로필 수정 성공 200")
    void updateProfile_성공_200() throws Exception {
        Map<String, String> body = Map.of(
                "name", "Updated Name",
                "birthDate", "1995-06-15",
                "countryCode", "US",
                "phoneNumber", "+12025551234"
        );

        mockMvc.perform(put("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("성공"));
    }

    @Test
    @DisplayName("PUT /api/user/profile - 필수 필드 누락 시 400")
    void updateProfile_필수필드_누락_400() throws Exception {
        // name만 있고 나머지 필수 필드(birthDate, countryCode, phoneNumber) 없음
        Map<String, String> body = Map.of("name", "Only Name");

        mockMvc.perform(put("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("PUT /api/user/profile - 전화번호 형식 오류 시 400")
    void updateProfile_전화번호형식오류_400() throws Exception {
        Map<String, String> body = Map.of(
                "name", "Test User",
                "birthDate", "2000-01-01",
                "countryCode", "KR",
                "phoneNumber", "01012345678"  // + 없음 → E.164 위반
        );

        mockMvc.perform(put("/api/user/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }
}
