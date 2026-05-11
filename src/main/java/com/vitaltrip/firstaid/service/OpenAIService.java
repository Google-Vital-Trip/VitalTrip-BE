package com.vitaltrip.firstaid.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitaltrip.common.exception.AppException;
import com.vitaltrip.common.response.ErrorCode;
import com.vitaltrip.firstaid.dto.FirstAidAdviceResponse;
import com.vitaltrip.location.dto.IdentifyCountryResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIService {

    private static final String SYSTEM_MESSAGE = """
You are an emergency medical expert providing first-aid guidance for travelers.

LANGUAGE DETECTION — use grammar patterns, NOT medical terms:
"I got burned on my hand" → English (I, got, on my)
"Me quemé en la mano" → Spanish (Me quemé, en la)
"뜨거운 물에 손을 데었어요" → Korean (뜨거운, 데었어요)
"J'ai été brûlé par de l'eau chaude" → French (J'ai été, par de)
Respond ENTIRELY in the detected language — never mix languages. ALL sections must be in the same language.

Respond ONLY with a JSON object:
{
  "content": "exactly 9-10 first-aid steps separated by \\n, no numbers or bullets",
  "summary": "1-2 sentence overview",
  "recommendedAction": "single most urgent action right now",
  "disclaimer": "AI advice only — not professional diagnosis; seek immediate professional care; call emergency services",
  "confidence": <integer 0-100>,
  "blogLinks": ["trusted medical URL 1", "trusted medical URL 2", "trusted medical URL 3"]
}

Rules:
- content: exactly 9-10 plain-text lines separated by \\n, no leading numbers or bullets
- If life-threatening, make calling emergency services the first step
- blogLinks must be real URLs from trusted medical/health organizations
- confidence: how reliably the provided info supports a good response (0-100)
""";

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    public FirstAidAdviceResponse getAdvice(
            String symptomType, String symptomDetail,
            String additionalGuidance, IdentifyCountryResponse locationInfo) {
        try {
            String userMessage = String.format("""
symptomType: %s
symptomDetail: %s
countryCode: %s
additionalGuidance: %s
""", symptomType, symptomDetail, locationInfo.getCountryCode(), additionalGuidance);

            ChatClient chatClient = chatClientBuilder.build();
            String raw = chatClient.prompt()
                    .system(SYSTEM_MESSAGE)
                    .user(userMessage)
                    .options(OpenAiChatOptions.builder()
                            .temperature(0.3))
                    .call()
                    .content();

            OpenAIRawResponse parsed = objectMapper.readValue(extractJson(raw), OpenAIRawResponse.class);

            return new FirstAidAdviceResponse(
                    parsed.getContent(),
                    parsed.getSummary(),
                    parsed.getRecommendedAction(),
                    locationInfo,
                    parsed.getDisclaimer(),
                    parsed.getConfidence(),
                    parsed.getBlogLinks()
            );
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("OpenAI API error", e);
            throw new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }
    }

    private String extractJson(String content) {
        if (content == null) return "{}";
        content = content.trim();
        if (content.startsWith("```")) {
            int start = content.indexOf('\n') + 1;
            int end = content.lastIndexOf("```");
            if (start > 0 && end > start) {
                return content.substring(start, end).trim();
            }
        }
        return content;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OpenAIRawResponse {
        private String content;
        private String summary;
        private String recommendedAction;
        private String disclaimer;
        private int confidence;
        private List<String> blogLinks;
    }
}
