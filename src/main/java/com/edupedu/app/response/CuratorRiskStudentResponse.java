package com.edupedu.app.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CuratorRiskStudentResponse(
        @JsonProperty("student_id")
        Long studentId,

        @JsonProperty("user_id")
        Long userId,

        @JsonProperty("full_name")
        String fullName,

        @JsonProperty("average_grade")
        Double averageGrade,

        @JsonProperty("absences")
        Long absences,

        @JsonProperty("risk_reasons")
        List<String> riskReasons
) {
}
