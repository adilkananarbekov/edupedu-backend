package com.edupedu.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edupedu.app.exception.ResourceNotFoundException;
import com.edupedu.app.model.Grade;
import com.edupedu.app.model.Student;
import com.edupedu.app.model.Subject;
import com.edupedu.app.model.Teacher;
import com.edupedu.app.model.User;
import com.edupedu.app.repository.GradeRepository;
import com.edupedu.app.repository.StudentRepository;
import com.edupedu.app.repository.SubjectRepository;
import com.edupedu.app.repository.TeacherRepository;
import com.edupedu.app.repository.TakenClassRepository;
import com.edupedu.app.repository.UserRepository;
import com.edupedu.app.request.CreateGradeRequest;
import com.edupedu.app.request.GradeDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final TakenClassRepository takenClassRepository;
    // private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<GradeDTO> getStudentGrades(Long studentId) {
        return gradeRepository.findByStudentIdOrderByDateDesc(studentId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GradeDTO> getAllGrades() {
        return gradeRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GradeDTO> getStudentGradesBySubject(Long studentId, Long subjectId) {
        return gradeRepository.findByStudentIdAndSubjectId(studentId, subjectId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GradeDTO> getGradesBySubject(Long subjectId) {
        return gradeRepository.findAll()
                .stream()
                .filter(g -> g.getTakenClass().getClazz().getSubject().getId().equals(subjectId))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Double> getStudentGradeAverages(Long studentId) {
        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        Map<String, Double> averages = new HashMap<>();

        grades.stream()
                .collect(Collectors.groupingBy(g -> g.getTakenClass().getClazz().getSubject().getName()))
                .forEach((subjectName, subjectGrades) -> {
                    double avg = subjectGrades.stream()
                            .mapToDouble(Grade::getValue)
                            .average()
                            .orElse(0.0);
                    averages.put(subjectName, Math.round(avg * 100.0) / 100.0);
                });

        return averages;
    }

    @Transactional(readOnly = true)
    public Map<String, Double> getGradeAverages() {
        List<Grade> grades = gradeRepository.findAll();
        Map<String, Double> averages = new HashMap<>();

        grades.stream()
                .collect(Collectors.groupingBy(g -> g.getTakenClass().getClazz().getSubject().getName()))
                .forEach((subjectName, subjectGrades) -> {
                    double avg = subjectGrades.stream()
                            .mapToDouble(Grade::getValue)
                            .average()
                            .orElse(0.0);
                    averages.put(subjectName, Math.round(avg * 100.0) / 100.0);
                });

        return averages;
    }

    @Transactional
    public GradeDTO createGrade(CreateGradeRequest request, Long actorUserId) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", request.studentId()));

        // Subject subject = subjectRepository.findById(request.subjectId())
        //         .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.subjectId()));

        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", actorUserId));
        // Teacher teacher = resolveTeacherForGrade(actor, subject);

        Grade grade = Grade.builder()
                .takenClass(takenClassRepository.findById(request.takenClassId())
                        .orElseThrow(() -> new ResourceNotFoundException("TakenClass", "id", request.takenClassId())))
                .value(request.value())
                .maxValue(request.maxValue() != null ? request.maxValue() : 100.0)
                .gradeType(request.gradeType())
                .description(request.description())
                .date(request.date())
                .build();

        grade = gradeRepository.save(grade);
        // auditLogService.log("CREATE_GRADE",
        //         "User " + actor.getFullName() + " added grade " + grade.getValue() + " for student "
        //                 + student.getUser().getFullName(),
        //         actor);
        return mapToDTO(grade);
    }

    @Transactional
    public void deleteGrade(Long gradeId) {
        if (!gradeRepository.existsById(gradeId)) {
            throw new ResourceNotFoundException("Grade", "id", gradeId);
        }
        gradeRepository.deleteById(gradeId);
    }

    private GradeDTO mapToDTO(Grade grade) {
        return GradeDTO.builder()
                .id(grade.getId())
                .studentId(grade.getTakenClass().getSemester().getStudent().getId())
                .takenClassId(grade.getTakenClass().getId())
                .value(grade.getValue())
                .maxValue(grade.getMaxValue())
                .gradeType(grade.getGradeType())
                .description(grade.getDescription())
                .date(grade.getDate())
                .createdAt(grade.getCreatedAt())
                .build();
    }

    private Teacher resolveTeacherForGrade(User actor, Subject subject) {
        return teacherRepository.findByUserId(actor.getId())
                .or(() -> teacherRepository.findAll().stream()
                        .filter(teacher -> teacher.getSubjects().stream()
                                .anyMatch(teacherSubject -> teacherSubject.getId().equals(subject.getId())))
                        .findFirst())
                .or(() -> teacherRepository.findAll().stream().findFirst())
                .orElseThrow(() -> new IllegalStateException("No teacher profile available to assign grade"));
    }
}

