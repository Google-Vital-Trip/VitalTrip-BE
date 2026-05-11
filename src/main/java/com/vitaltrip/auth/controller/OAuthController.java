package com.vitaltrip.auth.controller;

import com.vitaltrip.auth.service.GoogleOAuthService;
import com.vitaltrip.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@Tag(name = "OAuth", description = "Google OAuth2 API")
@RestController
@RequiredArgsConstructor
public class OAuthController {

    private final GoogleOAuthService googleOAuthService;

    @Operation(summary = "Google 로그인 URL 조회")
    @GetMapping("/api/oauth2/login-url")
    public ResponseEntity<ApiResponse<Map<String, String>>> getLoginUrl() {
        String url = googleOAuthService.buildLoginUrl();
        return ResponseEntity.ok(ApiResponse.success(Map.of("googleLoginUrl", url)));
    }

    @Operation(summary = "Google OAuth2 콜백 (리다이렉트 처리)")
    @GetMapping("/api/auth/google/callback")
    public void googleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            HttpServletResponse response) throws IOException {
        String redirectUrl = googleOAuthService.handleCallback(code, error);
        response.sendRedirect(redirectUrl);
    }
}
