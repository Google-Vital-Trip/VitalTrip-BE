package com.vitaltrip.admin.service;

import com.vitaltrip.admin.dto.AdminUserResponse;
import com.vitaltrip.admin.dto.PagedResponse;
import com.vitaltrip.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PagedResponse<AdminUserResponse> getUsers(int page, int size) {
        int clampedSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageable = PageRequest.of(page, clampedSize, Sort.by("id").ascending());
        return PagedResponse.from(userRepository.findAll(pageable), AdminUserResponse::new);
    }
}
