package com.vitaltrip.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "이름을 입력해주세요.")
    @Size(min = 1, max = 100, message = "이름은 1~100자 사이여야 합니다.")
    private String name;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,64}$",
        message = "비밀번호는 8~64자이며 영문, 숫자, 특수문자(!@#$%^&*)를 각 1자 이상 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String passwordConfirm;

    @NotBlank(message = "생년월일을 입력해주세요.")
    private String birthDate;

    @NotBlank(message = "국가 코드를 입력해주세요.")
    @Size(min = 2, max = 10, message = "국가 코드는 2~10자 사이여야 합니다.")
    private String countryCode;

    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(regexp = "^\\+[0-9]{1,15}$", message = "전화번호는 E.164 형식(+로 시작, 최대 15자리)이어야 합니다.")
    private String phoneNumber;
}
