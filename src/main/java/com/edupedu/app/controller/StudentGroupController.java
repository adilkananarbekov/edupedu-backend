package com.edupedu.app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edupedu.app.model.StudentGroup;
import com.edupedu.app.response.StudentGroupResponse;
import com.edupedu.app.service.StudentGroupService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StudentGroupController {

    private final StudentGroupService studentGroupService;

    @GetMapping("/student-groups")
    public ResponseEntity<List<StudentGroupResponse>> getAllStudentGroups() {
        return new ResponseEntity<>(studentGroupService.getAllStudentGroups(), HttpStatus.OK);
    }

    @GetMapping("/student-groups/{id}")
    public ResponseEntity<StudentGroupResponse> getStudentGroupById(@PathVariable Long id) {
        return new ResponseEntity<>(studentGroupService.getStudentGroupById(id), HttpStatus.OK);
    }

    @PostMapping("/admin/student-groups")
    public ResponseEntity<StudentGroupResponse> createStudentGroup(@RequestBody @Valid StudentGroup studentGroup) {
        return new ResponseEntity<>(studentGroupService.createStudentGroup(studentGroup), HttpStatus.CREATED);
    }

    @PutMapping("/admin/student-groups/{id}")
    public ResponseEntity<StudentGroupResponse> updateStudentGroup(@PathVariable Long id,
            @RequestBody @Valid StudentGroup studentGroup) {
        return new ResponseEntity<>(studentGroupService.updateStudentGroup(id, studentGroup), HttpStatus.OK);
    }

    @DeleteMapping("/admin/student-groups/{id}")
    public ResponseEntity<Void> deleteStudentGroup(@PathVariable Long id) {
        studentGroupService.deleteStudentGroup(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
