package com.vitaltrip.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final String message;
    private final T data;
    private final String errorCode;

    private ApiResponse(String message, T data, String errorCode) {
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("성공", data, null);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>("성공", null, null);
    }

    public static ApiResponse<Void> successWithMessage(String message) {
        return new ApiResponse<>(message, null, null);
    }

    public static ApiResponse<Void> error(String message, String errorCode) {
        return new ApiResponse<>(message, null, errorCode);
    }
}
