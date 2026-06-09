package com.edupedu.app.service;

import com.edupedu.app.exception.ResourceNotFoundException;
import com.edupedu.app.model.Student;
import com.edupedu.app.model.StudentGroup;
import com.edupedu.app.model.User;
import com.edupedu.app.model.enums.Role;
import com.edupedu.app.repository.StudentGroupRepository;
import com.edupedu.app.repository.StudentRepository;
import com.edupedu.app.repository.UserRepository;
import com.edupedu.app.request.StudentCreateRequest;
import com.edupedu.app.request.StudentDTO;
import com.edupedu.app.request.StudentUpdateRequest;
import com.edupedu.app.response.StudentResponse;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import com.edupedu.app.request.BulkAssignRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final StudentGroupRepository studentGroupRepository;

    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        return mapToResponse(student);
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentByUserId(Long userId) {
        Student student = studentRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Student", "userId", userId));
        return mapToResponse(student);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsByGroupId(Long groupId) {
        return studentRepository.findByStudentGroupId(groupId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StudentResponse createStudent(StudentCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.userId()));

        if (user.getRole() != Role.ROLE_STUDENT) {
            throw new IllegalArgumentException("User role must be ROLE_STUDENT");
        }

        if (studentRepository.existsByUserId(request.userId())) {
            throw new IllegalArgumentException("Student profile already exists for this user");
        }

        Student student = Student.builder()
                .user(user)
                .studentNumber(request.studentNumber())
                .accountNumber(request.accountNumber())
                .parentPhone(request.parentPhone())
                .build();

        if (request.studentGroupId() != null) {
            StudentGroup studentGroup = studentGroupRepository.findById(request.studentGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("StudentGroup", "id", request.studentGroupId()));
            student.setStudentGroup(studentGroup);
        }

        student = studentRepository.save(student);
        return mapToResponse(student);
    }

    @Transactional
    public StudentResponse updateStudent(Long id, StudentUpdateRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        if (request.studentNumber() != null) {
            student.setStudentNumber(request.studentNumber());
        }
        if (request.accountNumber() != null) {
            student.setAccountNumber(request.accountNumber());
        }
        if (request.parentPhone() != null) {
            student.setParentPhone(request.parentPhone());
        }

        if (request.studentGroupId() != null) {
            StudentGroup studentGroup = studentGroupRepository.findById(request.studentGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("StudentGroup", "id", request.studentGroupId()));
            student.setStudentGroup(studentGroup);
        }

        student = studentRepository.save(student);
        return mapToResponse(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        studentRepository.delete(student);
    }

    private StudentResponse mapToResponse(Student student) {
        var studentGroup = student.getStudentGroup();

        return new StudentResponse(
                student.getId(),
                student.getUser().getId(),
                student.getUser().getEmail(),
                student.getUser().getFirstName(),
                student.getUser().getLastName(),
                student.getUser().getFullName(),
                student.getStudentNumber(),
                student.getAccountNumber(),
                student.getParentPhone(),
                studentGroup != null ? studentGroup.getId() : null,
                studentGroup != null ? studentGroup.getName() : null,
                student.getUser().getUniversity() != null ? student.getUser().getUniversity().getId() : null,
                student.getUser().isEmailVerified(),
                student.getUser().isEnabled(),
                student.getUser().isLocked(),
                student.getUser().isExpired(),
                student.getUser().getCreatedAt(),
                student.getUser().getLastModifiedAt()
        );
    }

    public List<Student> getAllStudentsFromUniversity(Long universityId) {
        return studentRepository.findAllByUniversityId(universityId);
    }

    public List<StudentResponse> getUnassignedStudents(Long universityId) {
        return studentRepository.findAllByUniversityIdAndStudentGroupIsNull(universityId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private StudentDTO mapToStudentDTO(Student student) {
                return new StudentDTO(
                                student.getId(),
                                student.getUser().getId(),
                                student.getUser().getFullName(),
                                student.getUser().getEmail(),
                                student.getStudentGroup() != null ? student.getStudentGroup().getId() : null,
                                student.getStudentGroup() != null ? student.getStudentGroup().getName() : null,
                                student.getStudentNumber(),
                                student.getAccountNumber());
        }


    public StudentDTO updateStudentGroup(Long studentId, Long studentGroupId) {
        Student student = studentRepository.findById(studentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
        if (studentGroupId != null) {
            StudentGroup studentGroup = studentGroupRepository.findById(studentGroupId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentGroup", "id", studentGroupId));
            student.setStudentGroup(studentGroup);
        } else {
            student.setStudentGroup(null);
        }
        student = studentRepository.save(student);
        return mapToStudentDTO(student);
    }

    public List<StudentDTO> bulkAssignStudentsToClass(BulkAssignRequest request) {
                StudentGroup finalClassGroup = null;
                if (request.classGroupId() != null) {
                        finalClassGroup = studentGroupRepository.findById(request.classGroupId())
                                        .orElseThrow(() -> new ResourceNotFoundException("ClassGroup", "id",
                                                        request.classGroupId()));
                }

                final StudentGroup classGroup = finalClassGroup;
                List<Student> students = request.studentIds().stream()
                                .map(id -> studentRepository.findById(id)
                                                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id)))
                                .peek(s -> s.setStudentGroup(classGroup))
                                .map(studentRepository::save)
                                .collect(Collectors.toList());

                return students.stream().map(this::mapToStudentDTO).collect(Collectors.toList());
        }
}
