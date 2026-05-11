package com.vitaltrip.firstaid.controller;

import com.vitaltrip.common.response.ApiResponse;
import com.vitaltrip.firstaid.dto.FirstAidAdviceResponse;
import com.vitaltrip.firstaid.dto.FirstAidRequest;
import com.vitaltrip.firstaid.service.FirstAidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/first-aid")
@RequiredArgsConstructor
public class FirstAidController {

    private final FirstAidService firstAidService;

    @PostMapping("/advice")
    public ResponseEntity<ApiResponse<FirstAidAdviceResponse>> getAdvice(
            @Valid @RequestBody FirstAidRequest request) {
        FirstAidAdviceResponse result = firstAidService.getAdvice(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
