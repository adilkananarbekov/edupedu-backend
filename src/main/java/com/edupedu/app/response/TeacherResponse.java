package com.edupedu.app.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record TeacherResponse(
        @JsonProperty("id")
        Long id,

        @JsonProperty("user_id")
        Long userId,

        @JsonProperty("email")
        String email,

        @JsonProperty("first_name")
        String firstName,

        @JsonProperty("last_name")
        String lastName,

        @JsonProperty("full_name")
        String fullName,

        @JsonProperty("employee_number")
        String employeeNumber,

        @JsonProperty("subject_ids")
        List<Long> subjectIds,

        @JsonProperty("subject_names")
        List<String> subjectNames,

        @JsonProperty("curator_id")
        Long curatorId,

        @JsonProperty("curated_student_group_name")
        String curatedStudentGroupName,

        @JsonProperty("university_id")
        Long universityId,

        @JsonProperty("email_verified")
        boolean emailVerified,

        @JsonProperty("enabled")
        boolean enabled,

        @JsonProperty("locked")
        boolean locked,

        @JsonProperty("expired")
        boolean expired,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("last_modified_at")
        LocalDateTime lastModifiedAt
) {
}
