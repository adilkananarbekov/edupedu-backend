package com.edupedu.app.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edupedu.app.exception.ResourceNotFoundException;
import com.edupedu.app.model.StudentGroup;

import com.edupedu.app.repository.StudentGroupRepository;
import com.edupedu.app.response.StudentGroupResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentGroupService {

    private final StudentGroupRepository studentGroupRepository;

    @Transactional(readOnly = true)
    public List<StudentGroupResponse> getAllStudentGroups() {
        return studentGroupRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentGroupResponse getStudentGroupById(Long id) {
        return studentGroupRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("StudentGroup", "id", id));
    }

    @Transactional
    public StudentGroupResponse createStudentGroup(StudentGroup studentGroup) {
        return mapToResponse(studentGroupRepository.save(studentGroup));
    }

    @Transactional
    public StudentGroupResponse updateStudentGroup(Long id, StudentGroup updatedStudentGroup) {
        StudentGroup existingStudentGroup = studentGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudentGroup", "id", id));

        existingStudentGroup.setName(updatedStudentGroup.getName());
        existingStudentGroup.setYear(updatedStudentGroup.getYear());
        existingStudentGroup.setFaculty(updatedStudentGroup.getFaculty());
        existingStudentGroup.setUniversity(updatedStudentGroup.getUniversity());

        return mapToResponse(studentGroupRepository.save(existingStudentGroup));
    }

    @Transactional
    public void deleteStudentGroup(Long id) {
        StudentGroup studentGroup = studentGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudentGroup", "id", id));
        studentGroupRepository.delete(studentGroup);
    }

    private StudentGroupResponse mapToResponse(StudentGroup studentGroup) {
        var faculty = studentGroup.getFaculty();
        var students = studentGroup.getStudents();

        return new StudentGroupResponse(
                studentGroup.getId(),
                studentGroup.getName(),
                studentGroup.getYear(),
                faculty != null ? faculty.getMonthlyFee() : null,
                students != null ? students.size() : 0
        );
    }
}
