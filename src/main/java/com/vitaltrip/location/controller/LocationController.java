package com.vitaltrip.location.controller;

import com.vitaltrip.common.response.ApiResponse;
import com.vitaltrip.location.dto.IdentifyCountryRequest;
import com.vitaltrip.location.dto.IdentifyCountryResponse;
import com.vitaltrip.location.dto.NearbyFacilityResponse;
import com.vitaltrip.location.service.GooglePlacesService;
import com.vitaltrip.location.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    private final GooglePlacesService googlePlacesService;

    @PostMapping("/identify-country")
    public ResponseEntity<ApiResponse<IdentifyCountryResponse>> identifyCountry(
            @Valid @RequestBody IdentifyCountryRequest request) {
        IdentifyCountryResponse result = locationService.identifyCountry(
                request.getLatitude(), request.getLongitude());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<NearbyFacilityResponse>>> nearby(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5000") int radius,
            @RequestParam(defaultValue = "hospital") String type,
            @RequestParam(defaultValue = "en") String language) {

        int clampedRadius = Math.min(Math.max(radius, 500), 50000);
        List<NearbyFacilityResponse> results =
                googlePlacesService.searchNearby(latitude, longitude, clampedRadius, type, language);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
