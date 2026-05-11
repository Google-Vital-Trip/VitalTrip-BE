package com.vitaltrip.location.controller;

import com.vitaltrip.common.response.ApiResponse;
import com.vitaltrip.location.dto.IdentifyCountryRequest;
import com.vitaltrip.location.dto.IdentifyCountryResponse;
import com.vitaltrip.location.dto.NearbyFacilityResponse;
import com.vitaltrip.location.service.GooglePlacesService;
import com.vitaltrip.location.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Location", description = "위치 식별 및 주변 시설 API")
@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    private final GooglePlacesService googlePlacesService;

    @Operation(summary = "현재 위치 국가 식별")
    @PostMapping("/identify-country")
    public ResponseEntity<ApiResponse<IdentifyCountryResponse>> identifyCountry(
            @Valid @RequestBody IdentifyCountryRequest request) {
        IdentifyCountryResponse result = locationService.identifyCountry(
                request.getLatitude(), request.getLongitude());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "주변 의료 시설 검색")
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<NearbyFacilityResponse>>> nearby(
            @Parameter(description = "위도") @RequestParam Double latitude,
            @Parameter(description = "경도") @RequestParam Double longitude,
            @Parameter(description = "검색 반경(m), 500~50000, 기본값 5000") @RequestParam(defaultValue = "5000") int radius,
            @Parameter(description = "시설 유형 (hospital, pharmacy 등)") @RequestParam(defaultValue = "hospital") String type,
            @Parameter(description = "결과 언어 코드 (en, ko 등)") @RequestParam(defaultValue = "en") String language) {

        int clampedRadius = Math.min(Math.max(radius, 500), 50000);
        List<NearbyFacilityResponse> results =
                googlePlacesService.searchNearby(latitude, longitude, clampedRadius, type, language);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
