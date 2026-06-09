package com.edupedu.app.service;

import com.edupedu.app.exception.ResourceNotFoundException;
import com.edupedu.app.model.Subject;
import com.edupedu.app.model.Teacher;
import com.edupedu.app.model.User;
import com.edupedu.app.model.enums.Role;
import com.edupedu.app.repository.SubjectRepository;
import com.edupedu.app.repository.TeacherRepository;
import com.edupedu.app.repository.UserRepository;
import com.edupedu.app.request.TeacherCreateRequest;
import com.edupedu.app.request.TeacherDTO;
import com.edupedu.app.request.TeacherUpdateRequest;
import com.edupedu.app.response.TeacherResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    @Transactional(readOnly = true)
    public List<TeacherResponse> getAllTeachers() {
        return teacherRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeacherResponse getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));
        return mapToResponse(teacher);
    }

    @Transactional(readOnly = true)
    public TeacherResponse getTeacherByUserId(Long userId) {
        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "userId", userId));
        return mapToResponse(teacher);
    }

    @Transactional
    public TeacherResponse createTeacher(TeacherCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.userId()));

        if (user.getRole() != Role.ROLE_TEACHER) {
            throw new IllegalArgumentException("User role must be ROLE_TEACHER");
        }

        if (teacherRepository.findByUserId(request.userId()).isPresent()) {
            throw new IllegalArgumentException("Teacher profile already exists for this user");
        }

        Teacher teacher = Teacher.builder()
                .user(user)
                .employeeNumber(request.employeeNumber())
                .build();

        if (request.subjectIds() != null && !request.subjectIds().isEmpty()) {
            Set<Subject> subjects = subjectRepository.findAllByIdIn(request.subjectIds());
            if (subjects.size() != request.subjectIds().size()) {
                throw new ResourceNotFoundException("Subject", "ids", request.subjectIds());
            }
            teacher.setSubjects(subjects);
        }

        teacher = teacherRepository.save(teacher);
        return mapToResponse(teacher);
    }

    @Transactional
    public TeacherResponse updateTeacher(Long id, TeacherUpdateRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));

        if (request.employeeNumber() != null) {
            teacher.setEmployeeNumber(request.employeeNumber());
        }

        if (request.subjectIds() != null) {
            Set<Subject> subjects = subjectRepository.findAllByIdIn(request.subjectIds());
            if (subjects.size() != request.subjectIds().size()) {
                throw new ResourceNotFoundException("Subject", "ids", request.subjectIds());
            }
            teacher.setSubjects(subjects);
        }

        teacher = teacherRepository.save(teacher);
        return mapToResponse(teacher);
    }

    @Transactional
    public void deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));
        teacherRepository.delete(teacher);
    }

    private TeacherResponse mapToResponse(Teacher teacher) {
        List<Long> subjectIds = teacher.getSubjects() != null
                ? teacher.getSubjects().stream().map(Subject::getId).collect(Collectors.toList())
                : List.of();
        List<String> subjectNames = teacher.getSubjects() != null
                ? teacher.getSubjects().stream().map(Subject::getName).collect(Collectors.toList())
                : List.of();

        var curator = teacher.getCurator();
        var curatedGroup = curator != null ? curator.getStudentGroup() : null;

        return new TeacherResponse(
                teacher.getId(),
                teacher.getUser().getId(),
                teacher.getUser().getEmail(),
                teacher.getUser().getFirstName(),
                teacher.getUser().getLastName(),
                teacher.getUser().getFullName(),
                teacher.getEmployeeNumber(),
                subjectIds,
                subjectNames,
                curator != null ? curator.getId() : null,
                curatedGroup != null ? curatedGroup.getName() : null,
                teacher.getUser().getUniversity() != null ? teacher.getUser().getUniversity().getId() : null,
                teacher.getUser().isEmailVerified(),
                teacher.getUser().isEnabled(),
                teacher.getUser().isLocked(),
                teacher.getUser().isExpired(),
                teacher.getUser().getCreatedAt(),
                teacher.getUser().getLastModifiedAt()
        );
    }

    public List<Teacher> getAllTeachersFromUniversity(Long universityId) {
        return teacherRepository.findAllByUniversityId(universityId);
    }

    public TeacherDTO updateTeacherSubjects(
                        Long teacherId,
                        List<Long> subjectIds) {
                Teacher teacher = teacherRepository.findById(teacherId)
                                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));

                Set<Subject> subjects = subjectIds.stream()
                                .map(id -> subjectRepository.findById(id)
                                                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id)))
                                .collect(Collectors.toSet());

                teacher.setSubjects(subjects);
                teacher = teacherRepository.save(teacher);
                return mapToTeacherDTO(teacher);
        }

        private TeacherDTO mapToTeacherDTO(Teacher teacher) {
                return new TeacherDTO(
                                teacher.getId(),
                                teacher.getUser().getId(),
                                teacher.getUser().getFullName(),
                                teacher.getUser().getEmail(),
                                teacher.getSubjects().stream().map(Subject::getName).collect(Collectors.toList()),
                                teacher.getEmployeeNumber());
        }

}
