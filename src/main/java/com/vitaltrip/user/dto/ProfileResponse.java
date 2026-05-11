package com.vitaltrip.user.dto;

import com.vitaltrip.user.entity.User;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class ProfileResponse {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private final Long id;
    private final String email;
    private final String name;
    private final String googleId;
    private final String birthDate;
    private final String countryCode;
    private final String phoneNumber;
    private final String profileImageUrl;
    private final String provider;
    private final String role;
    private final String createdAt;
    private final String updatedAt;

    public ProfileResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.googleId = user.getGoogleId();
        this.birthDate = user.getBirthDate();
        this.countryCode = user.getCountryCode();
        this.phoneNumber = user.getPhoneNumber();
        this.profileImageUrl = user.getProfileImageUrl();
        this.provider = user.getProvider().name();
        this.role = user.getRole().name();
        this.createdAt = user.getCreatedAt() != null ? user.getCreatedAt().format(FORMATTER) : null;
        this.updatedAt = user.getUpdatedAt() != null ? user.getUpdatedAt().format(FORMATTER) : null;
    }
}
