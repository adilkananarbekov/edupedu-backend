package com.edupedu.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edupedu.app.exception.ResourceNotFoundException;
import com.edupedu.app.model.Announcement;
import com.edupedu.app.model.Student;
import com.edupedu.app.model.StudentGroup;
import com.edupedu.app.model.User;
import com.edupedu.app.model.enums.Role;
import com.edupedu.app.repository.AnnouncementRepository;

import com.edupedu.app.repository.StudentGroupRepository;
import com.edupedu.app.repository.StudentRepository;
import com.edupedu.app.repository.UserRepository;
import com.edupedu.app.request.AnnouncementDTO;
import com.edupedu.app.request.CreateAnnouncementRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final StudentRepository studentRepository;

    @Transactional(readOnly = true)
    public List<AnnouncementDTO> getAnnouncementsForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        Long studentGroupId = student.getStudentGroup() != null ? student.getStudentGroup().getId() : null;

        return announcementRepository.findActiveAnnouncementsForStudent(
                Role.ROLE_STUDENT, studentGroupId, LocalDateTime.now())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnnouncementDTO> getAnnouncementsForTeacher() {
        return announcementRepository.findActiveAnnouncementsForRole(Role.ROLE_TEACHER, LocalDateTime.now())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnnouncementDTO> getAllAnnouncements() {
        return announcementRepository.findAllOrderByImportanceAndDate()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AnnouncementDTO createAnnouncement(CreateAnnouncementRequest request, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));

        Announcement announcement = Announcement.builder()
                .title(request.title())
                .content(request.content())
                .author(author)
                .targetRole(request.targetRole())
                .important(request.important() != null ? request.important() : false)
                .expiresAt(request.expiresAt())
                .build();

        if (request.targetStudentGroupId() != null) {
            StudentGroup studentGroup = studentGroupRepository.findById(request.targetStudentGroupId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("ClassGroup", "id", request.targetStudentGroupId()));
            announcement.setTargetStudentGroup(studentGroup);
        }

        announcement = announcementRepository.save(announcement);
        return mapToDTO(announcement);
    }

    @Transactional
    public void deleteAnnouncement(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Announcement", "id", id);
        }
        announcementRepository.deleteById(id);
    }

    private AnnouncementDTO mapToDTO(Announcement announcement) {
        return AnnouncementDTO.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .authorId(announcement.getAuthor().getId())
                .authorName(announcement.getAuthor().getFullName())
                .targetRole(announcement.getTargetRole())
                .targetStudentGroupId(
                        announcement.getTargetStudentGroup() != null ? announcement.getTargetStudentGroup().getId() : null)
                .targetStudentGroupName(
                        announcement.getTargetStudentGroup() != null ? announcement.getTargetStudentGroup().getName()
                                : null)
                .important(announcement.getImportant())
                .expiresAt(announcement.getExpiresAt())
                .createdAt(announcement.getCreatedAt())
                .build();
    }
}
