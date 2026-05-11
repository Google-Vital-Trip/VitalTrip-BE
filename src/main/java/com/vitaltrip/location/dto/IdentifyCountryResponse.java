package com.vitaltrip.location.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IdentifyCountryResponse {
    private final String countryCode;
    private final String countryName;
    private final double latitude;
    private final double longitude;
    private final EmergencyContactDto emergencyContact;
}
