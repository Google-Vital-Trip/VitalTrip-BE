package com.vitaltrip.user.service;

import com.vitaltrip.common.exception.AppException;
import com.vitaltrip.common.response.ErrorCode;
import com.vitaltrip.user.dto.ProfileResponse;
import com.vitaltrip.user.dto.UpdateProfileRequest;
import com.vitaltrip.user.entity.User;
import com.vitaltrip.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("프로필 조회 성공")
    void getProfile_성공() {
        User user = buildUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        ProfileResponse profile = userService.getProfile(1L);

        assertThat(profile.getId()).isEqualTo(1L);
        assertThat(profile.getEmail()).isEqualTo("user@example.com");
        assertThat(profile.getName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("프로필 조회 실패 - 유저 없음")
    void getProfile_유저없음_예외() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(99L))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    @DisplayName("프로필 수정 성공")
    void updateProfile_성공() {
        User user = buildUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UpdateProfileRequest req = new UpdateProfileRequest();
        setField(req, "name", "Updated Name");
        setField(req, "birthDate", "1995-06-15");
        setField(req, "countryCode", "US");
        setField(req, "phoneNumber", "+12025551234");

        userService.updateProfile(1L, req);

        assertThat(user.getName()).isEqualTo("Updated Name");
        assertThat(user.getBirthDate()).isEqualTo("1995-06-15");
        assertThat(user.getCountryCode()).isEqualTo("US");
        assertThat(user.getPhoneNumber()).isEqualTo("+12025551234");
    }

    @Test
    @DisplayName("프로필 수정 실패 - 유저 없음")
    void updateProfile_유저없음_예외() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        UpdateProfileRequest req = new UpdateProfileRequest();
        setField(req, "name", "Name");
        setField(req, "birthDate", "1995-01-01");
        setField(req, "countryCode", "KR");
        setField(req, "phoneNumber", "+821012345678");

        assertThatThrownBy(() -> userService.updateProfile(99L, req))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getErrorCode())
                        .isEqualTo(ErrorCode.NOT_FOUND));
    }

    private User buildUser(Long id) {
        return User.builder()
                .id(id)
                .email("user@example.com")
                .name("Test User")
                .password("encoded-pass")
                .birthDate("2000-01-01")
                .countryCode("KR")
                .phoneNumber("+821012345678")
                .provider(User.Provider.LOCAL)
                .role(User.Role.USER)
                .build();
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
