package com.vitaltrip.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "이름을 입력해주세요.")
    @Size(min = 1, max = 100, message = "이름은 1~100자 사이여야 합니다.")
    private String name;

    @NotBlank(message = "생년월일을 입력해주세요.")
    private String birthDate;

    @NotBlank(message = "국가 코드를 입력해주세요.")
    @Size(min = 2, max = 10, message = "국가 코드는 2~10자 사이여야 합니다.")
    private String countryCode;

    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(regexp = "^\\+[0-9]{1,15}$", message = "전화번호는 E.164 형식이어야 합니다.")
    private String phoneNumber;
}
