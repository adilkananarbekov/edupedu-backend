package com.edupedu.app.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CuratorStudentOverviewResponse(
        @JsonProperty("student_id")
        Long studentId,

        @JsonProperty("user_id")
        Long userId,

        @JsonProperty("full_name")
        String fullName,

        @JsonProperty("parent_phone")
        String parentPhone,

        @JsonProperty("average_grade")
        Double averageGrade,

        @JsonProperty("absences")
        Long absences,

        @JsonProperty("at_risk")
        Boolean atRisk
) {
}
