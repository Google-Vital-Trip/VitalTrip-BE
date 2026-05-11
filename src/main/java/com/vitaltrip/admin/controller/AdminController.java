package com.vitaltrip.admin.controller;

import com.vitaltrip.admin.dto.AdminUserResponse;
import com.vitaltrip.admin.dto.PagedResponse;
import com.vitaltrip.admin.service.AdminService;
import com.vitaltrip.common.response.ApiResponse;
import com.vitaltrip.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Admin", description = "관리자 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "전체 유저 목록 조회", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PagedResponse<AdminUserResponse>>> getUsers(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (1~100)") @RequestParam(defaultValue = "20") int size) {
        PagedResponse<AdminUserResponse> result = adminService.getUsers(page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "관리자 여부 확인 (인증 없이도 호출 가능)")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> adminMe(
            @AuthenticationPrincipal User currentUser) {
        boolean isAdmin = currentUser != null && currentUser.getRole() == User.Role.ADMIN;
        return ResponseEntity.ok(ApiResponse.success(Map.of("isAdmin", isAdmin)));
    }
}
