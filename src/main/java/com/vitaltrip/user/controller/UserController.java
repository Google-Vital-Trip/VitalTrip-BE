package com.vitaltrip.user.controller;

import com.vitaltrip.common.exception.AppException;
import com.vitaltrip.common.response.ApiResponse;
import com.vitaltrip.common.response.ErrorCode;
import com.vitaltrip.user.dto.ProfileResponse;
import com.vitaltrip.user.dto.UpdateProfileRequest;
import com.vitaltrip.user.entity.User;
import com.vitaltrip.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) throw new AppException(ErrorCode.UNAUTHORIZED);
        ProfileResponse profile = userService.getProfile(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        if (currentUser == null) throw new AppException(ErrorCode.UNAUTHORIZED);
        userService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
