package com.vitaltrip.auth.service;

import com.vitaltrip.auth.dto.*;
import com.vitaltrip.common.exception.AppException;
import com.vitaltrip.common.response.ErrorCode;
import com.vitaltrip.common.security.JwtUtil;
import com.vitaltrip.user.entity.User;
import com.vitaltrip.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public boolean checkEmail(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Transactional
    public void signup(SignupRequest request) {
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "비밀번호가 일치하지 않습니다.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .birthDate(request.getBirthDate())
                .countryCode(request.getCountryCode())
                .phoneNumber(request.getPhoneNumber())
                .provider(User.Provider.LOCAL)
                .role(User.Role.USER)
                .build();
        userRepository.save(user);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (user.getProvider() == User.Provider.GOOGLE) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, "소셜 로그인 계정은 비밀번호 로그인을 사용할 수 없습니다.");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());
        user.setRefreshToken(refreshToken);

        return new LoginResponse(accessToken, refreshToken, user);
    }

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        user.setRefreshToken(null);
    }

    @Transactional
    public Map<String, String> refresh(RefreshRequest request) {
        String token = request.getRefreshToken();
        if (!jwtUtil.validateRefreshToken(token)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다.");
        }

        Long userId = Long.parseLong(jwtUtil.parseRefreshToken(token).getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        if (!token.equals(user.getRefreshToken())) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "리프레시 토큰이 일치하지 않습니다.");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        return Map.of("accessToken", accessToken);
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        if (user.getProvider() == User.Provider.GOOGLE) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "소셜 로그인 계정은 비밀번호를 변경할 수 없습니다.");
        }
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "새 비밀번호가 일치하지 않습니다.");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, "현재 비밀번호가 올바르지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Transactional
    public Map<String, String> adminLogin(AdminLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (user.getProvider() == User.Provider.GOOGLE) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, "소셜 로그인 계정은 비밀번호 로그인을 사용할 수 없습니다.");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (user.getRole() != User.Role.ADMIN) {
            throw new AppException(ErrorCode.FORBIDDEN, "관리자 권한이 없습니다.");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());
        user.setRefreshToken(refreshToken);

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    @Transactional
    public String adminRefresh(String refreshToken) {
        if (refreshToken == null || !jwtUtil.validateRefreshToken(refreshToken)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다.");
        }

        Long userId = Long.parseLong(jwtUtil.parseRefreshToken(refreshToken).getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "리프레시 토큰이 일치하지 않습니다.");
        }
        if (user.getRole() != User.Role.ADMIN) {
            throw new AppException(ErrorCode.FORBIDDEN, "관리자 권한이 없습니다.");
        }

        return jwtUtil.generateAccessToken(user.getId(), user.getEmail());
    }
}
