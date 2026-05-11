package com.vitaltrip.firstaid.dto;

import com.vitaltrip.location.dto.IdentifyCountryResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FirstAidAdviceResponse {
    private final String content;
    private final String summary;
    private final String recommendedAction;
    private final IdentifyCountryResponse identificationResponse;
    private final String disclaimer;
    private final int confidence;
    private final List<String> blogLinks;
}
