package com.edupedu.app.request;

import jakarta.validation.constraints.NotNull;

public record AssignCuratorRequest(
        @NotNull(message = "Teacher ID is required")
        Long teacherId,

        @NotNull(message = "Student group ID is required")
        Long studentGroupId
) {
}
