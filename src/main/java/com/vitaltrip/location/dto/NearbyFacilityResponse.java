package com.vitaltrip.location.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NearbyFacilityResponse {
    private final String name;
    private final String address;
    private final String phoneNumber;
    private final double latitude;
    private final double longitude;
    private final double distance;
    private final Boolean openNow;
    private final List<String> openingHours;
    private final String websiteUrl;
}
