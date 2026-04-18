package com.edupedu.app.response;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ApiErrorResponse(
        @JsonProperty("timestamp")
        LocalDateTime timestamp,

        @JsonProperty("status")
        Integer status,

        @JsonProperty("error")
        String error,

        @JsonProperty("message")
        String message,

        @JsonProperty("path")
        String path,

        @JsonProperty("validation_errors")
        Map<String, String> validationErrors
) {
}
