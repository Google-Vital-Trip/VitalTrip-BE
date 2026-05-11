package com.vitaltrip.location.service;

import com.vitaltrip.common.exception.AppException;
import com.vitaltrip.common.response.ErrorCode;
import com.vitaltrip.firstaid.constant.EmergencyContacts;
import com.vitaltrip.location.dto.EmergencyContactDto;
import com.vitaltrip.location.dto.IdentifyCountryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class LocationService {

    private static final String BIGDATACLOUD_URL =
            "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude={lat}&longitude={lon}&localityLanguage=en";

    private final RestTemplate restTemplate;

    public LocationService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }

    @SuppressWarnings("unchecked")
    public IdentifyCountryResponse identifyCountry(double latitude, double longitude) {
        try {
            Map<String, Object> result = restTemplate.getForObject(
                    BIGDATACLOUD_URL, Map.class, latitude, longitude);

            String countryCode = result != null ? (String) result.get("countryCode") : null;
            String countryName = result != null ? (String) result.get("countryName") : null;

            if (countryCode == null || countryCode.isBlank()) {
                countryCode = "DEFAULT";
            }
            if (countryName == null) {
                countryName = "";
            }

            EmergencyContactDto emergencyContact = EmergencyContacts.get(countryCode);
            return new IdentifyCountryResponse(countryCode, countryName, latitude, longitude, emergencyContact);
        } catch (Exception e) {
            log.error("BigDataCloud API error", e);
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE);
        }
    }
}
