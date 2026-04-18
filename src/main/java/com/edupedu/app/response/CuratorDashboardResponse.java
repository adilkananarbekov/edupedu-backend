package com.edupedu.app.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CuratorDashboardResponse(
        @JsonProperty("curator_id")
        Long curatorId,

        @JsonProperty("teacher_id")
        Long teacherId,

        @JsonProperty("student_group_id")
        Long studentGroupId,

        @JsonProperty("student_group_name")
        String studentGroupName,

        @JsonProperty("students_count")
        Integer studentsCount,

        @JsonProperty("group_average_grade")
        Double groupAverageGrade,

        @JsonProperty("total_absences")
        Long totalAbsences,

        @JsonProperty("total_lates")
        Long totalLates,

        @JsonProperty("at_risk_students")
        List<CuratorRiskStudentResponse> atRiskStudents
) {
}
