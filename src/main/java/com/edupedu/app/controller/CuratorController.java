package com.edupedu.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edupedu.app.request.AssignCuratorRequest;
import com.edupedu.app.response.CuratorAssignmentResponse;
import com.edupedu.app.response.CuratorDashboardResponse;
import com.edupedu.app.service.TeacherCuratorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CuratorController {

    private final TeacherCuratorService teacherCuratorService;

    @PostMapping("/admin/curators/assign")
    public ResponseEntity<CuratorAssignmentResponse> assignCurator(@RequestBody @Valid AssignCuratorRequest request) {
        return new ResponseEntity<>(
                teacherCuratorService.assignTeacherToGroup(request.teacherId(), request.studentGroupId()),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/admin/curators/teacher/{teacherId}")
    public ResponseEntity<CuratorAssignmentResponse> unassignCurator(@PathVariable Long teacherId) {
        return new ResponseEntity<>(teacherCuratorService.unassignTeacher(teacherId), HttpStatus.OK);
    }

    @GetMapping("/admin/curators/teacher/{teacherId}")
    public ResponseEntity<CuratorAssignmentResponse> getCuratorByTeacher(@PathVariable Long teacherId) {
        return new ResponseEntity<>(teacherCuratorService.getByTeacherId(teacherId), HttpStatus.OK);
    }

    @GetMapping("/admin/curators/student-group/{studentGroupId}")
    public ResponseEntity<CuratorAssignmentResponse> getCuratorByStudentGroup(@PathVariable Long studentGroupId) {
        return new ResponseEntity<>(teacherCuratorService.getByStudentGroupId(studentGroupId), HttpStatus.OK);
    }

    @GetMapping("/admin/curators")
    public ResponseEntity<java.util.List<CuratorAssignmentResponse>> getAllCurators() {
        return new ResponseEntity<>(teacherCuratorService.getAllAssignments(), HttpStatus.OK);
    }

    @GetMapping("/teacher/curators/dashboard/me")
    public ResponseEntity<CuratorDashboardResponse> getMyCuratorDashboard(Authentication authentication) {
        return new ResponseEntity<>(
                teacherCuratorService.getDashboardForCurrentTeacher(authentication.getName()),
                HttpStatus.OK
        );
    }

    @GetMapping("/student/curators/me")
    public ResponseEntity<CuratorAssignmentResponse> getMyCurator(Authentication authentication) {
        return new ResponseEntity<>(
                teacherCuratorService.getForCurrentStudent(authentication.getName()),
                HttpStatus.OK
        );
    }
}
