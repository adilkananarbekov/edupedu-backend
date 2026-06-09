package com.edupedu.app.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StudentGroupResponse(
        @JsonProperty("id")
        Long id,

        @JsonProperty("name")
        String name,

        @JsonProperty("year")
        Integer year,

        @JsonProperty("monthly_fee")
        Integer monthlyFee,

        @JsonProperty("student_count")
        Integer studentCount
) {
}
