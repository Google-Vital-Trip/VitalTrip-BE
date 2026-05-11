package com.vitaltrip.auth.controller;

import com.vitaltrip.auth.dto.*;
import com.vitaltrip.auth.service.AuthService;
import com.vitaltrip.common.exception.AppException;
import com.vitaltrip.common.response.ApiResponse;
import com.vitaltrip.common.response.ErrorCode;
import com.vitaltrip.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "이메일 중복 확인")
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkEmail(@RequestParam String email) {
        boolean available = authService.checkEmail(email);
        return ResponseEntity.ok(ApiResponse.success(Map.of("available", available)));
    }

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "로그아웃", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) throw new AppException(ErrorCode.UNAUTHORIZED);
        authService.logout(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "토큰 갱신")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refresh(@Valid @RequestBody RefreshRequest request) {
        Map<String, String> result = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "비밀번호 변경", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody PasswordChangeRequest request) {
        if (currentUser == null) throw new AppException(ErrorCode.UNAUTHORIZED);
        authService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "관리자 로그인 (쿠키 발급)")
    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<Void>> adminLogin(
            @Valid @RequestBody AdminLoginRequest request,
            HttpServletResponse servletResponse) {
        Map<String, String> tokens = authService.adminLogin(request);

        ResponseCookie accessCookie = buildCookie("adminAccessToken", tokens.get("accessToken"), 3600);
        ResponseCookie refreshCookie = buildCookie("adminRefreshToken", tokens.get("refreshToken"), 2592000);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.successWithMessage("어드민 로그인이 완료되었습니다"));
    }

    @Operation(summary = "관리자 토큰 갱신 (쿠키)")
    @PostMapping("/admin/refresh")
    public ResponseEntity<ApiResponse<Void>> adminRefresh(
            @CookieValue(value = "adminRefreshToken", required = false) String adminRefreshToken) {
        String newAccessToken = authService.adminRefresh(adminRefreshToken);

        ResponseCookie accessCookie = buildCookie("adminAccessToken", newAccessToken, 3600);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .body(ApiResponse.successWithMessage("토큰이 갱신되었습니다"));
    }

    private ResponseCookie buildCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }
}
