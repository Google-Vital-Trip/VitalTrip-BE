package com.vitaltrip.firstaid.dto;

import com.vitaltrip.firstaid.constant.SymptomType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FirstAidRequest {

    @NotNull(message = "증상 유형을 입력해주세요.")
    private SymptomType symptomType;

    @NotBlank(message = "증상 상세 내용을 입력해주세요.")
    @Size(min = 10, max = 2000, message = "증상 상세 내용은 10~2000자 사이여야 합니다.")
    private String symptomDetail;

    @NotNull(message = "위도를 입력해주세요.")
    @DecimalMin(value = "-90", message = "위도는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90", message = "위도는 90 이하여야 합니다.")
    private Double latitude;

    @NotNull(message = "경도를 입력해주세요.")
    @DecimalMin(value = "-180", message = "경도는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180", message = "경도는 180 이하여야 합니다.")
    private Double longitude;
}
