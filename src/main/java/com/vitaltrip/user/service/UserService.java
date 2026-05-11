package com.vitaltrip.user.service;

import com.vitaltrip.common.exception.AppException;
import com.vitaltrip.common.response.ErrorCode;
import com.vitaltrip.user.dto.ProfileResponse;
import com.vitaltrip.user.dto.UpdateProfileRequest;
import com.vitaltrip.user.entity.User;
import com.vitaltrip.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        return new ProfileResponse(user);
    }

    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        user.setName(request.getName());
        user.setBirthDate(request.getBirthDate());
        user.setCountryCode(request.getCountryCode());
        user.setPhoneNumber(request.getPhoneNumber());
    }
}
