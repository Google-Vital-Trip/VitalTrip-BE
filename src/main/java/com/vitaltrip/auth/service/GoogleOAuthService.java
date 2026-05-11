package com.vitaltrip.auth.service;

import com.vitaltrip.common.config.AppProperties;
import com.vitaltrip.common.config.GoogleProperties;
import com.vitaltrip.common.security.JwtUtil;
import com.vitaltrip.user.entity.User;
import com.vitaltrip.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RestClient restClient;
    private final GoogleProperties googleProperties;
    private final AppProperties appProperties;

    public String buildLoginUrl() {
        return UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", googleProperties.clientId())
                .queryParam("redirect_uri", googleProperties.callbackUrl())
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("access_type", "offline")
                .toUriString();
    }

    @Transactional
    public String handleCallback(String code, String error) {
        String errorBase = appProperties.frontendUrl() + "/auth/callback?error=true&errorCode=OAUTH_ERROR&message=";
        try {
            if (error != null || code == null) {
                return errorBase + encode("OAuth 인증이 취소되었습니다.");
            }

            String googleAccessToken = exchangeCodeForToken(code);
            Map<String, Object> userInfo = fetchGoogleUserInfo(googleAccessToken);

            String googleId = (String) userInfo.get("sub");
            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");
            String picture = (String) userInfo.get("picture");

            Optional<User> byGoogleId = userRepository.findByGoogleId(googleId);
            Optional<User> byEmail = userRepository.findByEmail(email);
            User user = byGoogleId.orElse(byEmail.orElse(null));

            if (user != null) {
                if (user.getGoogleId() == null) {
                    user.setGoogleId(googleId);
                }
                String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
                String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());
                user.setRefreshToken(refreshToken);

                return appProperties.frontendUrl() + "/auth/callback?success=true"
                        + "&accessToken=" + encode(accessToken)
                        + "&refreshToken=" + encode(refreshToken)
                        + "&email=" + encode(user.getEmail())
                        + "&name=" + encode(user.getName())
                        + "&profileImageUrl=" + encode(user.getProfileImageUrl() != null ? user.getProfileImageUrl() : "");
            } else {
                String tempToken = jwtUtil.generateTempToken(email, googleId);
                return appProperties.frontendUrl() + "/auth/callback?needsProfile=true"
                        + "&tempToken=" + encode(tempToken)
                        + "&email=" + encode(email)
                        + "&name=" + encode(name != null ? name : "")
                        + "&profileImageUrl=" + encode(picture != null ? picture : "");
            }
        } catch (Exception e) {
            log.error("Google OAuth callback error", e);
            return errorBase + encode("OAuth 인증 처리 중 오류가 발생했습니다.");
        }
    }

    @SuppressWarnings("unchecked")
    private String exchangeCodeForToken(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", googleProperties.clientId());
        body.add("client_secret", googleProperties.clientSecret());
        body.add("redirect_uri", googleProperties.callbackUrl());
        body.add("grant_type", "authorization_code");

        Map<String, Object> response = restClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(Map.class);

        return (String) response.get("access_token");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchGoogleUserInfo(String accessToken) {
        return restClient.get()
                .uri(USERINFO_URL)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);
    }

    private String encode(String value) {
        return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
    }
}
