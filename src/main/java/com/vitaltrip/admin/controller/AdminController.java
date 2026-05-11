package com.vitaltrip.admin.controller;

import com.vitaltrip.admin.dto.AdminUserResponse;
import com.vitaltrip.admin.dto.PagedResponse;
import com.vitaltrip.admin.service.AdminService;
import com.vitaltrip.common.response.ApiResponse;
import com.vitaltrip.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PagedResponse<AdminUserResponse>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<AdminUserResponse> result = adminService.getUsers(page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> adminMe(
            @AuthenticationPrincipal User currentUser) {
        boolean isAdmin = currentUser != null && currentUser.getRole() == User.Role.ADMIN;
        return ResponseEntity.ok(ApiResponse.success(Map.of("isAdmin", isAdmin)));
    }
}
