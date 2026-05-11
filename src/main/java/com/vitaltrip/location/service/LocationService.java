package com.vitaltrip.location.service;

import com.vitaltrip.common.exception.AppException;
import com.vitaltrip.common.response.ErrorCode;
import com.vitaltrip.firstaid.constant.EmergencyContacts;
import com.vitaltrip.location.dto.EmergencyContactDto;
import com.vitaltrip.location.dto.IdentifyCountryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
public class LocationService {

    private static final String BIGDATACLOUD_URL =
            "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude={lat}&longitude={lon}&localityLanguage=en";

    private final RestClient restClient;

    public LocationService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(5_000);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @SuppressWarnings("unchecked")
    public IdentifyCountryResponse identifyCountry(double latitude, double longitude) {
        try {
            Map<String, Object> result = restClient.get()
                    .uri(BIGDATACLOUD_URL, latitude, longitude)
                    .retrieve()
                    .body(Map.class);

            String countryCode = result != null ? (String) result.get("countryCode") : null;
            String countryName = result != null ? (String) result.get("countryName") : null;

            if (countryCode == null || countryCode.isBlank()) countryCode = "DEFAULT";
            if (countryName == null) countryName = "";

            EmergencyContactDto emergencyContact = EmergencyContacts.get(countryCode);
            return new IdentifyCountryResponse(countryCode, countryName, latitude, longitude, emergencyContact);
        } catch (Exception e) {
            log.error("BigDataCloud API error", e);
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE);
        }
    }
}
