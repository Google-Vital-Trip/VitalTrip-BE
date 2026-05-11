package com.vitaltrip.firstaid.controller;

import com.vitaltrip.common.response.ApiResponse;
import com.vitaltrip.firstaid.dto.FirstAidAdviceResponse;
import com.vitaltrip.firstaid.dto.FirstAidRequest;
import com.vitaltrip.firstaid.service.FirstAidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "First Aid", description = "응급처치 AI 조언 API")
@RestController
@RequestMapping("/api/first-aid")
@RequiredArgsConstructor
public class FirstAidController {

    private final FirstAidService firstAidService;

    @Operation(summary = "응급처치 AI 조언 조회")
    @PostMapping("/advice")
    public ResponseEntity<ApiResponse<FirstAidAdviceResponse>> getAdvice(
            @Valid @RequestBody FirstAidRequest request) {
        FirstAidAdviceResponse result = firstAidService.getAdvice(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
