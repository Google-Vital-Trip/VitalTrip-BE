package com.vitaltrip.firstaid.service;

import com.vitaltrip.firstaid.dto.FirstAidAdviceResponse;
import com.vitaltrip.firstaid.dto.FirstAidRequest;
import com.vitaltrip.location.dto.IdentifyCountryResponse;
import com.vitaltrip.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirstAidService {

    private final LocationService locationService;
    private final OpenAIService openAIService;

    public FirstAidAdviceResponse getAdvice(FirstAidRequest request) {
        IdentifyCountryResponse locationInfo = locationService.identifyCountry(
                request.getLatitude(), request.getLongitude());

        return openAIService.getAdvice(
                request.getSymptomType().name(),
                request.getSymptomDetail(),
                request.getSymptomType().getAdditionalGuidance(),
                locationInfo
        );
    }
}
