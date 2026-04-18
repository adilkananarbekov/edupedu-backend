package com.edupedu.app.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CuratorAssignmentResponse(
        @JsonProperty("curator_id")
        Long curatorId,

        @JsonProperty("teacher_id")
        Long teacherId,

        @JsonProperty("teacher_user_id")
        Long teacherUserId,

        @JsonProperty("teacher_full_name")
        String teacherFullName,

        @JsonProperty("teacher_email")
        String teacherEmail,

        @JsonProperty("teacher_phone")
        String teacherPhone,

        @JsonProperty("student_group_id")
        Long studentGroupId,

        @JsonProperty("student_group_name")
        String studentGroupName
) {
}
