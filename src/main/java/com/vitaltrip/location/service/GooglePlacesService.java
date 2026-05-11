package com.vitaltrip.location.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitaltrip.common.exception.AppException;
import com.vitaltrip.common.response.ErrorCode;
import com.vitaltrip.location.dto.NearbyFacilityResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class GooglePlacesService {

    private static final String PLACES_URL = "https://places.googleapis.com/v1/places:searchNearby";
    private static final String FIELD_MASK =
            "places.displayName,places.formattedAddress,places.nationalPhoneNumber,places.location,places.regularOpeningHours,places.websiteUri";
    private static final long CACHE_TTL_MS = 7_200_000L;

    private static final Map<String, List<String>> TYPE_MAP = Map.of(
            "hospital", List.of("hospital", "doctor"),
            "pharmacy", List.of("pharmacy"),
            "emergency", List.of("hospital")
    );

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.google.places-api-key}")
    private String placesApiKey;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public List<NearbyFacilityResponse> searchNearby(
            double latitude, double longitude, int radius, String type, String language) {
        String cacheKey = String.format("%s:%.2f:%.2f:%d:%s", type, latitude, longitude, radius, language);

        CacheEntry cached = cache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.data;
        }

        List<NearbyFacilityResponse> result = fetchFromApi(latitude, longitude, radius, type, language);
        cache.put(cacheKey, new CacheEntry(result));
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<NearbyFacilityResponse> fetchFromApi(
            double latitude, double longitude, int radius, String type, String language) {
        try {
            List<String> includedTypes = TYPE_MAP.getOrDefault(type, List.of("hospital"));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("includedTypes", includedTypes);
            body.put("maxResultCount", 20);
            body.put("languageCode", language);
            body.put("locationRestriction", Map.of(
                    "circle", Map.of(
                            "center", Map.of("latitude", latitude, "longitude", longitude),
                            "radius", (double) radius
                    )
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Goog-Api-Key", placesApiKey);
            headers.set("X-Goog-FieldMask", FIELD_MASK);

            ResponseEntity<Map> response = restTemplate.exchange(
                    PLACES_URL, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

            if (response.getBody() == null) return Collections.emptyList();

            List<Map<String, Object>> places =
                    (List<Map<String, Object>>) response.getBody().getOrDefault("places", Collections.emptyList());

            List<NearbyFacilityResponse> results = new ArrayList<>();
            for (Map<String, Object> place : places) {
                results.add(parseFacility(place, latitude, longitude));
            }
            results.sort(Comparator.comparingDouble(NearbyFacilityResponse::getDistance));
            return results;
        } catch (Exception e) {
            log.error("Google Places API error", e);
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE);
        }
    }

    @SuppressWarnings("unchecked")
    private NearbyFacilityResponse parseFacility(Map<String, Object> place, double originLat, double originLon) {
        Map<String, Object> displayName = (Map<String, Object>) place.get("displayName");
        String name = displayName != null ? (String) displayName.getOrDefault("text", "") : "";

        String address = (String) place.get("formattedAddress");
        String phoneNumber = (String) place.get("nationalPhoneNumber");
        String websiteUrl = (String) place.get("websiteUri");

        Map<String, Object> location = (Map<String, Object>) place.getOrDefault("location", Collections.emptyMap());
        double lat = toDouble(location.get("latitude"));
        double lon = toDouble(location.get("longitude"));

        Map<String, Object> hours = (Map<String, Object>) place.get("regularOpeningHours");
        Boolean openNow = hours != null ? (Boolean) hours.get("openNow") : null;
        List<String> openingHours = hours != null ? (List<String>) hours.get("weekdayDescriptions") : null;

        double distance = calculateDistance(originLat, originLon, lat, lon);
        return new NearbyFacilityResponse(name, address, phoneNumber, lat, lon, distance, openNow, openingHours, websiteUrl);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6_371_000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(R * c / 100.0) / 10.0;
    }

    private double toDouble(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        return 0.0;
    }

    private static class CacheEntry {
        final List<NearbyFacilityResponse> data;
        final long timestamp;

        CacheEntry(List<NearbyFacilityResponse> data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }
    }
}
