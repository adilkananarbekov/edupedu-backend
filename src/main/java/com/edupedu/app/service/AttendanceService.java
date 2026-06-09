package com.edupedu.app.service;





import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edupedu.app.exception.ResourceNotFoundException;
import com.edupedu.app.model.Attendance;
import com.edupedu.app.model.Schedule;
import com.edupedu.app.model.Student;
import com.edupedu.app.model.User;
import com.edupedu.app.model.enums.AttendanceStatus;
import com.edupedu.app.model.enums.Role;
import com.edupedu.app.repository.AttendanceRepository;
import com.edupedu.app.repository.ScheduleRepository;
import com.edupedu.app.repository.StudentRepository;
import com.edupedu.app.repository.UserRepository;
import com.edupedu.app.request.AttendanceDTO;
import com.edupedu.app.request.MarkAttendanceRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AttendanceDTO> getStudentAttendance(Long studentId) {
        return attendanceRepository.findByStudentId(studentId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceDTO> getAllAttendance() {
        return attendanceRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceDTO> getStudentAttendanceByDateRange(Long studentId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByStudentAndDateRange(studentId, startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceDTO> getAttendanceByDateRange(LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findAll()
                .stream()
                .filter(a -> !a.getDate().isBefore(startDate) && !a.getDate().isAfter(endDate))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getStudentAttendanceStats(Long studentId) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("present", attendanceRepository.countByStudentAndStatus(studentId, AttendanceStatus.PRESENT));
        stats.put("absent", attendanceRepository.countByStudentAndStatus(studentId, AttendanceStatus.ABSENT));
        stats.put("late", attendanceRepository.countByStudentAndStatus(studentId, AttendanceStatus.LATE));
        stats.put("excused", attendanceRepository.countByStudentAndStatus(studentId, AttendanceStatus.EXCUSED));
        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getAttendanceStats() {
        Map<String, Long> stats = new HashMap<>();
        List<Attendance> records = attendanceRepository.findAll();
        stats.put("present", records.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count());
        stats.put("absent", records.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count());
        stats.put("late", records.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count());
        stats.put("excused", records.stream().filter(a -> a.getStatus() == AttendanceStatus.EXCUSED).count());
        return stats;
    }

    @Transactional
    public List<AttendanceDTO> markAttendance(MarkAttendanceRequest request, Long markedByUserId) {
        Schedule schedule = scheduleRepository.findById(request.scheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", "id", request.scheduleId()));

        User markedBy = userRepository.findById(markedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", markedByUserId));

        // 24h check: only Admin can change attendance after 24 hours
        if (markedBy.getRole() != Role.ROLE_ADMIN && markedBy.getRole() != Role.ROLE_UNIVERSITY_ADMIN) {
            LocalDateTime now = java.time.LocalDateTime.now();
            LocalDateTime lessonStart = request.date().atTime(schedule.getStartTime());
            if (now.isAfter(lessonStart.plusHours(24))) {
                throw new IllegalArgumentException("Attendance cannot be changed after 24 hours of the lesson");
            }
        }

        return request.attendanceRecords().stream()
                .map(record -> {
                    Student student = studentRepository.findById(record.studentId())
                            .orElseThrow(() -> new ResourceNotFoundException("Student", "id", record.studentId()));

                    // Check if attendance already exists for this student/schedule/date
                    Attendance attendance = attendanceRepository
                            .findByStudentIdAndScheduleIdAndDate(record.studentId(), request.scheduleId(),
                                    request.date())
                            .orElse(Attendance.builder()
                                    .student(student)
                                    .schedule(schedule)
                                    .date(request.date())
                                    .build());

                    attendance.setStatus(record.status());
                    attendance.setNotes(record.notes());
                    attendance.setMarkedBy(markedBy);

                    attendance = attendanceRepository.save(attendance);
                    return mapToDTO(attendance);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceDTO> getScheduleAttendance(Long scheduleId, LocalDate date) {
        return attendanceRepository.findByScheduleId(scheduleId)
                .stream()
                .filter(a -> a.getDate().equals(date))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private AttendanceDTO mapToDTO(Attendance attendance) {
        return AttendanceDTO.builder()
                .id(attendance.getId())
                .studentId(attendance.getStudent().getId())
                .scheduleId(attendance.getSchedule().getId())
                .date(attendance.getDate())
                .status(attendance.getStatus())
                .notes(attendance.getNotes())
                .markedByName(attendance.getMarkedBy() != null ? attendance.getMarkedBy().getFullName() : null)
                .markedAt(attendance.getMarkedAt())
                .build();
    }
}
