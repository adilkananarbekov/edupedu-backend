package com.edupedu.app.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edupedu.app.exception.ResourceNotFoundException;
import com.edupedu.app.model.Curator;
import com.edupedu.app.model.Grade;
import com.edupedu.app.model.Student;
import com.edupedu.app.model.StudentGroup;
import com.edupedu.app.model.Teacher;
import com.edupedu.app.model.enums.AttendanceStatus;
import com.edupedu.app.model.enums.Role;
import com.edupedu.app.repository.AttendanceRepository;
import com.edupedu.app.repository.CuratorRepository;
import com.edupedu.app.repository.GradeRepository;
import com.edupedu.app.repository.StudentGroupRepository;
import com.edupedu.app.repository.StudentRepository;
import com.edupedu.app.repository.TeacherRepository;
import com.edupedu.app.response.CuratorAssignmentResponse;
import com.edupedu.app.response.CuratorDashboardResponse;
import com.edupedu.app.response.CuratorRiskStudentResponse;
import com.edupedu.app.response.CuratorStudentOverviewResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeacherCuratorService {

    private final CuratorRepository curatorRepository;
    private final TeacherRepository teacherRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;

    @Transactional
    public CuratorAssignmentResponse assignTeacherToGroup(Long teacherId, Long studentGroupId) {
	Teacher teacher = teacherRepository.findById(teacherId)
		.orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));

	if (teacher.getUser() == null || teacher.getUser().getRole() != Role.ROLE_TEACHER) {
	    throw new IllegalArgumentException("Only ROLE_TEACHER users can be assigned as curators");
	}

	StudentGroup studentGroup = studentGroupRepository.findById(studentGroupId)
		.orElseThrow(() -> new ResourceNotFoundException("StudentGroup", "id", studentGroupId));

	Curator curator = curatorRepository.findByTeacherId(teacherId)
		.orElseGet(() -> Curator.builder().teacher(teacher).build());

	curatorRepository.findByStudentGroupId(studentGroupId)
		.filter(existing -> !existing.getTeacher().getId().equals(teacherId))
		.ifPresent(existing -> {
		    throw new IllegalArgumentException("Student group already has a curator");
		});

	curator.setStudentGroup(studentGroup);
	Curator savedCurator = curatorRepository.save(curator);
	return mapAssignment(savedCurator);
    }

    @Transactional
    public CuratorAssignmentResponse unassignTeacher(Long teacherId) {
	Curator curator = curatorRepository.findByTeacherId(teacherId)
		.orElseThrow(() -> new ResourceNotFoundException("Curator", "teacherId", teacherId));

	curator.setStudentGroup(null);
	Curator savedCurator = curatorRepository.save(curator);
	return mapAssignment(savedCurator);
    }

    @Transactional(readOnly = true)
    public CuratorAssignmentResponse getByTeacherId(Long teacherId) {
	Curator curator = curatorRepository.findByTeacherId(teacherId)
		.orElseThrow(() -> new ResourceNotFoundException("Curator", "teacherId", teacherId));
	return mapAssignment(curator);
    }

    @Transactional(readOnly = true)
    public CuratorAssignmentResponse getByStudentGroupId(Long studentGroupId) {
	Curator curator = curatorRepository.findByStudentGroupId(studentGroupId)
		.orElseThrow(() -> new ResourceNotFoundException("Curator", "studentGroupId", studentGroupId));
	return mapAssignment(curator);
    }

    @Transactional(readOnly = true)
    public List<CuratorAssignmentResponse> getAllAssignments() {
	return curatorRepository.findAll().stream()
		.map(this::mapAssignment)
		.toList();
    }

    @Transactional(readOnly = true)
    public CuratorAssignmentResponse getForCurrentStudent(String studentEmail) {
	Student student = studentRepository.findByUserEmail(studentEmail)
		.orElseThrow(() -> new ResourceNotFoundException("Student", "email", studentEmail));

	if (student.getStudentGroup() == null) {
	    throw new IllegalArgumentException("Student is not assigned to any group");
	}

	Curator curator = curatorRepository.findByStudentGroupId(student.getStudentGroup().getId())
		.orElseThrow(() -> new ResourceNotFoundException("Curator", "studentGroupId", student.getStudentGroup().getId()));

	return mapAssignment(curator);
    }

    @Transactional(readOnly = true)
    public CuratorDashboardResponse getDashboardForCurrentTeacher(String teacherEmail) {
	Teacher teacher = teacherRepository.findByUserEmail(teacherEmail)
		.orElseThrow(() -> new ResourceNotFoundException("Teacher", "email", teacherEmail));

	Curator curator = curatorRepository.findByTeacherId(teacher.getId())
		.orElseThrow(() -> new ResourceNotFoundException("Curator", "teacherId", teacher.getId()));

	StudentGroup studentGroup = curator.getStudentGroup();
	if (studentGroup == null) {
	    throw new IllegalArgumentException("Curator is not assigned to any student group");
	}

	List<Student> students = studentRepository.findByStudentGroupId(studentGroup.getId());
	List<Grade> groupGrades = gradeRepository.findByStudentGroupId(studentGroup.getId());

	Double groupAverageGrade = groupGrades.isEmpty()
		? null
		: round(groupGrades.stream().mapToDouble(Grade::getValue).average().orElse(0.0));

	long totalAbsences = defaultCount(
		attendanceRepository.countByStudentGroupAndStatus(studentGroup.getId(), AttendanceStatus.ABSENT));
	long totalLates = defaultCount(
		attendanceRepository.countByStudentGroupAndStatus(studentGroup.getId(), AttendanceStatus.LATE));

	List<CuratorRiskStudentResponse> atRiskStudents = new ArrayList<>();
	for (Student student : students) {
	    long studentAbsences = defaultCount(
		    attendanceRepository.countByStudentAndStatus(student.getId(), AttendanceStatus.ABSENT));

	    List<Grade> studentGrades = gradeRepository.findByStudentId(student.getId());
	    Double studentAverageGrade = studentGrades.isEmpty()
		    ? null
		    : round(studentGrades.stream().mapToDouble(Grade::getValue).average().orElse(0.0));

	    List<String> riskReasons = new ArrayList<>();
	    if (studentAbsences >= 3) {
		riskReasons.add("HIGH_ABSENCE");
	    }
	    if (studentAverageGrade != null && studentAverageGrade < 60.0) {
		riskReasons.add("LOW_GRADE");
	    }

	    if (!riskReasons.isEmpty()) {
		atRiskStudents.add(new CuratorRiskStudentResponse(
			student.getId(),
			student.getUser() != null ? student.getUser().getId() : null,
			student.getUser() != null ? student.getUser().getFullName() : "Unknown",
			studentAverageGrade,
			studentAbsences,
			riskReasons
		));
	    }
	}

	return new CuratorDashboardResponse(
		curator.getId(),
		teacher.getId(),
		studentGroup.getId(),
		studentGroup.getName(),
		students.size(),
		groupAverageGrade,
		totalAbsences,
		totalLates,
		atRiskStudents
	);
    }

    @Transactional(readOnly = true)
    public List<CuratorStudentOverviewResponse> getStudentsOverviewForCurrentTeacher(String teacherEmail) {
	Teacher teacher = teacherRepository.findByUserEmail(teacherEmail)
		.orElseThrow(() -> new ResourceNotFoundException("Teacher", "email", teacherEmail));

	Curator curator = curatorRepository.findByTeacherId(teacher.getId())
		.orElseThrow(() -> new ResourceNotFoundException("Curator", "teacherId", teacher.getId()));

	StudentGroup studentGroup = curator.getStudentGroup();
	if (studentGroup == null) {
	    throw new IllegalArgumentException("Curator is not assigned to any student group");
	}

	return studentRepository.findByStudentGroupId(studentGroup.getId()).stream()
		.map(student -> {
		    long absences = defaultCount(
			    attendanceRepository.countByStudentAndStatus(student.getId(), AttendanceStatus.ABSENT));

		    List<Grade> studentGrades = gradeRepository.findByStudentId(student.getId());
		    Double averageGrade = studentGrades.isEmpty()
			    ? null
			    : round(studentGrades.stream().mapToDouble(Grade::getValue).average().orElse(0.0));

		    boolean atRisk = absences >= 3 || (averageGrade != null && averageGrade < 60.0);

		    return new CuratorStudentOverviewResponse(
			    student.getId(),
			    student.getUser() != null ? student.getUser().getId() : null,
			    student.getUser() != null ? student.getUser().getFullName() : "Unknown",
			    student.getParentPhone(),
			    averageGrade,
			    absences,
			    atRisk
		    );
		})
		.sorted((a, b) -> a.fullName().compareToIgnoreCase(b.fullName()))
		.toList();
    }

    private CuratorAssignmentResponse mapAssignment(Curator curator) {
	StudentGroup group = curator.getStudentGroup();
	Teacher teacher = curator.getTeacher();

	return new CuratorAssignmentResponse(
		curator.getId(),
		teacher != null ? teacher.getId() : null,
		teacher != null && teacher.getUser() != null ? teacher.getUser().getId() : null,
		teacher != null && teacher.getUser() != null ? teacher.getUser().getFullName() : null,
		teacher != null && teacher.getUser() != null ? teacher.getUser().getEmail() : null,
		teacher != null && teacher.getUser() != null ? teacher.getUser().getPhone() : null,
		group != null ? group.getId() : null,
		group != null ? group.getName() : null
	);
    }

    private long defaultCount(Long count) {
	return count == null ? 0L : count;
    }

    private Double round(double value) {
	return Math.round(value * 100.0) / 100.0;
    }
}
