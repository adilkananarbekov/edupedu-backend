package com.edupedu.app.service;

import com.edupedu.app.exception.ResourceNotFoundException;
import com.edupedu.app.model.University;
import com.edupedu.app.model.User;
import com.edupedu.app.model.enums.Role;
import com.edupedu.app.repository.UniversityRepository;
import com.edupedu.app.repository.UserRepository;
import com.edupedu.app.request.UserCreateRequest;
import com.edupedu.app.request.UserUpdateRequest;
import com.edupedu.app.response.StudentResponse;
import com.edupedu.app.response.TeacherResponse;
import com.edupedu.app.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UniversityRepository universityRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Object getCurrentUser(User principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        if (user.getRole() == Role.ROLE_STUDENT && user.getStudent() != null) {
            return mapToStudentResponse(user);
        }

        if (user.getRole() == Role.ROLE_TEACHER && user.getTeacher() != null) {
            return mapToTeacherResponse(user);
        }

        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("User with email '" + request.email() + "' already exists");
        }

        User user = User.builder()
                .email(request.email())
                .phone(request.phone())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(request.role())
                .emailVerified(false)
                .enabled(true)
                .locked(false)
                .expired(false)
                .build();

        if (request.universityId() != null) {
            University university = universityRepository.findById(request.universityId())
                    .orElseThrow(() -> new ResourceNotFoundException("University", "id", request.universityId()));
            user.setUniversity(university);
        }

        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new IllegalArgumentException("User with email '" + request.email() + "' already exists");
            }
            user.setEmail(request.email());
        }

        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.emailVerified() != null) {
            user.setEmailVerified(request.emailVerified());
        }
        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }
        if (request.locked() != null) {
            user.setLocked(request.locked());
        }
        if (request.expired() != null) {
            user.setExpired(request.expired());
        }
        if (request.universityId() != null) {
            University university = universityRepository.findById(request.universityId())
                    .orElseThrow(() -> new ResourceNotFoundException("University", "id", request.universityId()));
            user.setUniversity(university);
        }

        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userRepository.delete(user);
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getRole(),
                user.getUniversity() != null ? user.getUniversity().getId() : null,
                user.isEmailVerified(),
                user.isEnabled(),
                user.isLocked(),
                user.isExpired(),
                user.getCreatedAt(),
                user.getLastModifiedAt()
        );
    }

    private StudentResponse mapToStudentResponse(User user) {
        var student = user.getStudent();
        var studentGroup = student.getStudentGroup();

        return new StudentResponse(
                student.getId(),
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                student.getStudentNumber(),
                student.getAccountNumber(),
                student.getParentPhone(),
                studentGroup != null ? studentGroup.getId() : null,
                studentGroup != null ? studentGroup.getName() : null,
                user.getUniversity() != null ? user.getUniversity().getId() : null,
                user.isEmailVerified(),
                user.isEnabled(),
                user.isLocked(),
                user.isExpired(),
                user.getCreatedAt(),
                user.getLastModifiedAt()
        );
    }

    private TeacherResponse mapToTeacherResponse(User user) {
        var teacher = user.getTeacher();
        var curator = teacher.getCurator();
        var curatedGroup = curator != null ? curator.getStudentGroup() : null;

        var subjectIds = teacher.getSubjects() != null
                ? teacher.getSubjects().stream().map(subject -> subject.getId()).toList()
                : List.<Long>of();
        var subjectNames = teacher.getSubjects() != null
                ? teacher.getSubjects().stream().map(subject -> subject.getName()).toList()
                : List.<String>of();

        return new TeacherResponse(
                teacher.getId(),
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                teacher.getEmployeeNumber(),
                subjectIds,
                subjectNames,
                curator != null ? curator.getId() : null,
                curatedGroup != null ? curatedGroup.getName() : null,
                user.getUniversity() != null ? user.getUniversity().getId() : null,
                user.isEmailVerified(),
                user.isEnabled(),
                user.isLocked(),
                user.isExpired(),
                user.getCreatedAt(),
                user.getLastModifiedAt()
        );
    }

    public List<UserResponse> getAllUsersFromUniversity(Long universityId) {
        return userRepository.findAllByUniversityId(universityId).stream().map(user -> mapToResponse(user)).toList();
    }


}
