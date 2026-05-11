package com.vitaltrip.admin.service;

import com.vitaltrip.admin.dto.AdminUserResponse;
import com.vitaltrip.admin.dto.PagedResponse;
import com.vitaltrip.user.entity.User;
import com.vitaltrip.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    @DisplayName("유저 목록 조회 성공 - 정상 size")
    void getUsers_정상_size() {
        List<User> users = List.of(buildUser(1L), buildUser(2L));
        PageImpl<User> page = new PageImpl<>(users, PageRequest.of(0, 20), 2);
        given(userRepository.findAll(any(Pageable.class))).willReturn(page);

        PagedResponse<AdminUserResponse> result = adminService.getUsers(0, 20);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getPage()).isEqualTo(0);
    }

    @Test
    @DisplayName("size가 100 초과이면 100으로 클램핑")
    void getUsers_size_초과_100으로_클램핑() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        given(userRepository.findAll(captor.capture()))
                .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 100), 0));

        adminService.getUsers(0, 150);

        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    @DisplayName("size가 1 미만이면 1로 클램핑")
    void getUsers_size_0이하_1로_클램핑() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        given(userRepository.findAll(captor.capture()))
                .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 1), 0));

        adminService.getUsers(0, 0);

        assertThat(captor.getValue().getPageSize()).isEqualTo(1);
    }

    @Test
    @DisplayName("size -1도 1로 클램핑")
    void getUsers_음수size_1로_클램핑() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        given(userRepository.findAll(captor.capture()))
                .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 1), 0));

        adminService.getUsers(0, -5);

        assertThat(captor.getValue().getPageSize()).isEqualTo(1);
    }

    @Test
    @DisplayName("페이지 메타정보가 올바르게 매핑됨")
    void getUsers_페이지_메타정보_매핑() {
        // page=1, size=5, total=12 → 3 pages (0,1,2). page 1 = middle (not first, not last)
        List<User> users = List.of(buildUser(1L), buildUser(2L), buildUser(3L), buildUser(4L), buildUser(5L));
        PageImpl<User> page = new PageImpl<>(users, PageRequest.of(1, 5), 12);
        given(userRepository.findAll(any(Pageable.class))).willReturn(page);

        PagedResponse<AdminUserResponse> result = adminService.getUsers(1, 5);

        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getTotalElements()).isEqualTo(12);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.isFirst()).isFalse();
        assertThat(result.isLast()).isFalse();
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.isHasPrevious()).isTrue();
        assertThat(result.getContent()).hasSize(5);
    }

    private User buildUser(Long id) {
        return User.builder()
                .id(id)
                .email("user" + id + "@example.com")
                .name("User " + id)
                .birthDate("2000-01-01")
                .countryCode("KR")
                .phoneNumber("+821012345678")
                .provider(User.Provider.LOCAL)
                .role(User.Role.USER)
                .build();
    }
}
