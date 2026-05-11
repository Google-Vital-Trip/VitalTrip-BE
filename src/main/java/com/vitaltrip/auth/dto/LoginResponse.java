package com.vitaltrip.auth.dto;

import com.vitaltrip.user.entity.User;
import lombok.Getter;

@Getter
public class LoginResponse {

    private final String accessToken;
    private final String refreshToken;
    private final UserInfo user;

    public LoginResponse(String accessToken, String refreshToken, User user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = new UserInfo(user.getId(), user.getEmail(), user.getName());
    }

    @Getter
    public static class UserInfo {
        private final Long id;
        private final String email;
        private final String name;

        public UserInfo(Long id, String email, String name) {
            this.id = id;
            this.email = email;
            this.name = name;
        }
    }
}
