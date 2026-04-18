package com.edupedu.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.edupedu.app.exception.ResourceNotFoundException;
import com.edupedu.app.model.Student;
import com.edupedu.app.model.Teacher;
import com.edupedu.app.model.University;
import com.edupedu.app.model.UniversityAdmin;
import com.edupedu.app.model.User;
import com.edupedu.app.model.enums.Role;
import com.edupedu.app.repository.StudentGroupRepository;
import com.edupedu.app.repository.UserRepository;
import com.edupedu.app.request.AdminRegistrationRequest;
import com.edupedu.app.request.AuthenticationRequest;
import com.edupedu.app.request.RefreshRequest;
import com.edupedu.app.request.RegistrationRequest;
import com.edupedu.app.response.AdminRegistrationResponse;

import com.edupedu.app.response.AuthenticationResponse;
import com.edupedu.app.security.JwtService;
import com.edupedu.app.security.RefreshTokenService;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final StudentService studentService;
    private final StudentGroupService classGroupService;
    private final UniversityService universityService;
    private final TeacherCuratorService teacherCuratorService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final StudentGroupRepository studentGroupRepository;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;

    public AdminRegistrationResponse<User, ?> adminRegistration(AdminRegistrationRequest request) {
        if(userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("User with email " + request.email() + " already exists");
        }

        if (request.role() == Role.ROLE_STUDENT) {
            return createStudent(request);
        }
        if (request.role() == Role.ROLE_TEACHER) {
            return createTeacher(request);
        }
        if (request.role() == Role.ROLE_UNIVERSITY_ADMIN) {
            return createUniversityAdmin(request);
        }
        // if (request.role() == Role.ROLE_ACCOUNTANT) {
        //     return createAccountant(request);
        // }
        // if (request.role() == Role.ROLE_ADMIN) {
        //     return createAdmin(request);
        // }

        throw new IllegalArgumentException("Invalid role: " + request.role());
    }

    public AdminRegistrationResponse<User, ?> register(RegistrationRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("User with email " + request.email() + " already exists");
        }

        User user = User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .firstName(request.firstName())
            .lastName(request.lastName())
            .role(request.role() != null ? request.role() : Role.ROLE_STUDENT)
            .enabled(true)
            .emailVerified(false)
            .build();

        userRepository.save(user);

        // final String accessToken = jwtService.generateAccessToken(user.getUsername());
        // final String refreshToken = refreshTokenService.issueRefreshToken(user);
        // final String tokenType = "Bearer";

        return new AdminRegistrationResponse<>(true, "User created successfully", user, null);
    }

    // private AdminRegistrationResponse<User, UniversityAdmin> createAdmin(AdminRegistrationRequest request) {
    //     User user = createUser(request);
    //     userRepository.save(user);
    //     return new AdminRegistrationResponse<>(true, "Admin created successfully", user, );
    // }

    private AdminRegistrationResponse<User, UniversityAdmin> createUniversityAdmin(AdminRegistrationRequest request) {
        User user = createUser(request);
        UniversityAdmin universityAdmin = UniversityAdmin.builder()
            .user(user)
            .university(request.university())
            .build();
        user.setUniversityAdmin(universityAdmin);
        userRepository.save(user);
        return new AdminRegistrationResponse<>(true, "University admin created successfully", user, universityAdmin);
    }
        
        private AdminRegistrationResponse<User, Student> createStudent(AdminRegistrationRequest request) {
        User user = createUser(request);
        Student student;
        if (request.studentGroupId() != null) {
            student = Student.builder()
                .user(user)
                .studentGroup(studentGroupRepository.findById(request.studentGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student group not found", "id", request.studentGroupId())))
                .build();
        } else {
            student = Student.builder()
            .user(user)
            .build();
        }

        user.setStudent(student);
        
        userRepository.save(user);
        return new AdminRegistrationResponse<>(true, "Student created successfully", user, student);
    }

    private AdminRegistrationResponse<User, Teacher> createTeacher(AdminRegistrationRequest request) {
        User user = createUser(request);
            
        Teacher teacher = Teacher.builder()
            .user(user)
            .subjects(request.subjects())
            .build();
        user.setTeacher(teacher);
        this.userRepository.save(user);
        return new AdminRegistrationResponse<>(true, "Teacher created successfully", user, teacher);
    }

    private User createUser(AdminRegistrationRequest request) {

        User user = User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .firstName(request.firstName())
            .lastName(request.lastName())
            .enabled(true)
            .emailVerified(false)
            .role(request.role())
            .university(request.university())
            .build();

        return user;
    }

    public AuthenticationResponse refreshToken(final RefreshRequest req) {
        final String tokenType = "Bearer";
        final RefreshTokenService.RotatedTokens refreshedTokens = this.refreshTokenService.rotateRefreshToken(req.refreshToken());
        return new AuthenticationResponse(refreshedTokens.accessToken(), refreshedTokens.refreshToken(), tokenType);
    }

    public AuthenticationResponse login(final AuthenticationRequest request) {
        log.info("Attempting login for user: {}", request.email());
        try {
            final Authentication auth = this.authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
            log.info("Authentication successful for user: {}", request.email());
            final User user = (User) auth.getPrincipal();
            final String token = this.jwtService.generateAccessToken(user.getUsername());
            final String refreshToken = this.refreshTokenService.issueRefreshToken(user);
            final String tokenType = "Bearer";

            return new AuthenticationResponse(token, refreshToken, tokenType);
        } catch (Exception e) {
            log.error("Authentication failed for user: {}. Error: {}", request.email(), e.getMessage());
            throw e;
        }
    }


    public Map<String, String> createPasswordResetTokenForUser(String token, String password, String confirmPassword) {
        Map<String, String> response = new HashMap<>();
        if (!password.equals(confirmPassword)) {
            response.put("status", "error");
            response.put("message", "Passwords do not match");
            return response;
        }

        Optional<User> userOpt = findUserByResetToken(token);
        if (userOpt.isPresent() && isResetTokenValid(userOpt.get())) {
            updatePassword(userOpt.get(), password);
            response.put("status", "success");
            response.put("message", "Password reset successful! Please login.");
            return response;
        }

        response.put("status", "error");
        response.put("message", "Invalid or expired reset token");
        return response;
    }

    public void createPasswordResetTokenForUser(User user) {
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);
    }

    public Optional<User> findUserByResetToken(String token) {
        return userRepository.findByResetToken(token);
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    public boolean isResetTokenValid(User user) {
        return user.getResetTokenExpiry() != null &&
                user.getResetTokenExpiry().isAfter(LocalDateTime.now());
    }

    public Map<String, String> processForgotPassword(String email) {
        Map<String, String> response = new HashMap<>();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            try {
                createPasswordResetTokenForUser(user);
                emailService.sendPasswordResetEmail(user.getEmail(), user.getResetToken());
                response.put("status", "success");
                response.put("message", "Reset email sent");
            } catch (RuntimeException e) {
                response.put("status", "error");
                response.put("message", "Failed to send email");
                log.error("Failed to send email: " + e.getMessage() + ". Please check email configuration.");
            }
        } else {
            log.error("Email not found");
            response.put("status", "error");
            response.put("message", "Email not found");

        }
        return response;
    }
}
