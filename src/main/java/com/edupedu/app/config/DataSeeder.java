package com.edupedu.app.config;

import com.edupedu.app.model.*;
import com.edupedu.app.model.Module;
import com.edupedu.app.model.enums.AttendanceStatus;
import com.edupedu.app.model.enums.ClassStatus;
import com.edupedu.app.model.enums.Role;
import com.edupedu.app.model.enums.ContentType;
import com.edupedu.app.model.enums.CourseTestQuestionType;
import com.edupedu.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("local")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final SubjectRepository subjectRepository;
    private final UniversityRepository universityRepository;
    private final FacultyRepository facultyRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
        private final CuratorRepository curatorRepository;
    private final CourseLessonRepository courseLessonRepository;
    private final CourseTestRepository courseTestRepository;
    private final CourseTestQuestionRepository courseTestQuestionRepository;
    private final CourseTestQuestionChoiceRepository courseTestQuestionChoiceRepository;
    private final ClassRepository classRepository;
    private final ScheduleRepository scheduleRepository;
    private final AttendanceRepository attendanceRepository;
    private final AnnouncementRepository announcementRepository;
    private final SemesterRepository semesterRepository;
    private final TakenClassRepository takenClassRepository;
    private final GradeRepository gradeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        if (userRepository.count() > 0 || universityRepository.count() > 0) {
            log.info("Database already seeded. Skipping seeder.");
            return;
        }

        log.info("Starting database seeding...");

        University university = universityRepository.save(
                University.builder()
                        .name("Alatoo International University")
                        .creditsPerSemester((short) 30)
                        .build()
        );

        Faculty engineering = facultyRepository.save(
                Faculty.builder()
                        .name("Engineering")
                        .code("ENG")
                        .monthlyFee(3000)
                        .university(university)
                        .build()
        );

        Subject math = subjectRepository.save(
                Subject.builder().name("Mathematics").description("Advanced Mathematics").credits(4).build()
        );
        Subject physics = subjectRepository.save(
                Subject.builder().name("Physics").description("Physics and Mechanics").credits(3).build()
        );
        Subject cs = subjectRepository.save(
                Subject.builder().name("Computer Science").description("IT and Programming").credits(3).build()
        );

        StudentGroup group10A = studentGroupRepository.save(
                StudentGroup.builder()
                        .name("10A")
                        .year(10)
                        .faculty(engineering)
                        .university(university)
                        .build()
        );
        StudentGroup group11A = studentGroupRepository.save(
                StudentGroup.builder()
                        .name("11A")
                        .year(11)
                        .faculty(engineering)
                        .university(university)
                        .build()
        );

        userRepository.save(
                User.builder()
                        .email("superadmin@edupage.com")
                        .password(passwordEncoder.encode("super123"))
                        .firstName("Super")
                        .lastName("Admin")
                        .role(Role.ROLE_ADMIN)
                        .enabled(true)
                        .build()
        );

        userRepository.save(
                User.builder()
                        .email("admin@edupage.com")
                        .password(passwordEncoder.encode("admin123"))
                        .firstName("System")
                        .lastName("Admin")
                        .role(Role.ROLE_UNIVERSITY_ADMIN)
                        .university(university)
                        .enabled(true)
                        .build()
        );

        // userRepository.save(
        //         User.builder()
        //                 .email("finance@edupage.com")
        //                 .password(passwordEncoder.encode("finance123"))
        //                 .firstName("Main")
        //                 .lastName("Accountant")
        //                 .role(Role.ROLE_ACCOUNTANT)
        //                 .university(university)
        //                 .build()
        // );

        User teacherUser = userRepository.save(
                User.builder()
                        .email("teacher@edupage.com")
                        .password(passwordEncoder.encode("teacher123"))
                        .firstName("John")
                        .lastName("Smith")
                        .role(Role.ROLE_TEACHER)
                        .university(university)
                        .enabled(true)
                        .build()
        );

        Set<Subject> teacherSubjects = new HashSet<>();
        teacherSubjects.add(math);
        teacherSubjects.add(physics);
        Teacher johnTeacher = teacherRepository.save(
                Teacher.builder()
                        .user(teacherUser)
                        .subjects(teacherSubjects)
                        .employeeNumber("T001")
                        .build()
        );

        User teacherUser1 = userRepository.save(
                User.builder()
                        .email("teacher1@edupage.com")
                        .password(passwordEncoder.encode("teacher123"))
                        .firstName("Jane")
                        .lastName("Doe")
                        .role(Role.ROLE_TEACHER)
                        .university(university)
                        .enabled(true)
                        .build()
        );

        Set<Subject> teacherSubjects1 = new HashSet<>();
        teacherSubjects1.add(cs);
        Teacher janeTeacher = teacherRepository.save(
                Teacher.builder()
                        .user(teacherUser1)
                        .subjects(teacherSubjects1)
                        .employeeNumber("T002")
                        .build()
        );

        curatorRepository.save(
                Curator.builder()
                        .teacher(johnTeacher)
                        .studentGroup(group10A)
                        .build()
        );

        User studentUser1 = userRepository.save(
                User.builder()
                        .email("student1@edupage.com")
                        .password(passwordEncoder.encode("student123"))
                        .firstName("Alice")
                        .lastName("Johnson")
                        .role(Role.ROLE_STUDENT)
                        .university(university)
                        .enabled(true)
                        .build()
        );

        Student alice = studentRepository.save(
                Student.builder()
                        .user(studentUser1)
                        .studentGroup(group10A)
                        .studentNumber("S001")
                        .accountNumber("10000001")
                        .parentPhone("+996700000001")
                        .build()
        );

        User studentUser2 = userRepository.save(
                User.builder()
                        .email("student2@edupage.com")
                        .password(passwordEncoder.encode("student123"))
                        .firstName("Bob")
                        .lastName("Williams")
                        .role(Role.ROLE_STUDENT)
                        .university(university)
                        .enabled(true)
                        .build()
        );

        Student bob = studentRepository.save(
                Student.builder()
                        .user(studentUser2)
                        .studentGroup(group10A)
                        .studentNumber("S002")
                        .accountNumber("10000002")
                        .parentPhone("+996700000002")
                        .build()
        );

        User studentUser4 = userRepository.save(
                User.builder()
                        .email("student4@edupage.com")
                        .password(passwordEncoder.encode("student123"))
                        .firstName("Diana")
                        .lastName("Keller")
                        .role(Role.ROLE_STUDENT)
                        .university(university)
                        .enabled(true)
                        .build()
        );

        Student diana = studentRepository.save(
                Student.builder()
                        .user(studentUser4)
                        .studentGroup(group10A)
                        .studentNumber("S004")
                        .accountNumber("10000004")
                        .parentPhone("+996700000004")
                        .build()
        );

        User studentUser3 = userRepository.save(
                User.builder()
                        .email("student3@edupage.com")
                        .password(passwordEncoder.encode("student123"))
                        .firstName("Charlie")
                        .lastName("Brown")
                        .role(Role.ROLE_STUDENT)
                        .university(university)
                        .enabled(true)
                        .build()
        );

        Student charlie = studentRepository.save(
                Student.builder()
                        .user(studentUser3)
                        .studentGroup(group11A)
                        .studentNumber("S003")
                        .accountNumber("10000003")
                        .parentPhone("+996700000003")
                        .build()
        );

        User studentUser5 = userRepository.save(
                User.builder()
                        .email("student5@edupage.com")
                        .password(passwordEncoder.encode("student123"))
                        .firstName("Emma")
                        .lastName("Stone")
                        .role(Role.ROLE_STUDENT)
                        .university(university)
                        .enabled(true)
                        .build()
        );

        Student emma = studentRepository.save(
                Student.builder()
                        .user(studentUser5)
                        .studentGroup(group11A)
                        .studentNumber("S005")
                        .accountNumber("10000005")
                        .parentPhone("+996700000005")
                        .build()
        );

        User studentUser6 = userRepository.save(
                User.builder()
                        .email("student6@edupage.com")
                        .password(passwordEncoder.encode("student123"))
                        .firstName("Farid")
                        .lastName("Ibraev")
                        .role(Role.ROLE_STUDENT)
                        .university(university)
                        .enabled(true)
                        .build()
        );

        Student farid = studentRepository.save(
                Student.builder()
                        .user(studentUser6)
                        .studentGroup(group11A)
                        .studentNumber("S006")
                        .accountNumber("10000006")
                        .parentPhone("+996700000006")
                        .build()
        );

        com.edupedu.app.model.Class mathClass10 = classRepository.save(
                com.edupedu.app.model.Class.builder()
                        .name("ENG-10A-MATH")
                        .faculty(engineering)
                        .subject(math)
                        .teacher(johnTeacher)
                        .university(university)
                        .classStatus(ClassStatus.IN_PROGRESS)
                        .credits((short) 4)
                        .build()
        );

        com.edupedu.app.model.Class physicsClass10 = classRepository.save(
                com.edupedu.app.model.Class.builder()
                        .name("ENG-10A-PHYS")
                        .faculty(engineering)
                        .subject(physics)
                        .teacher(johnTeacher)
                        .university(university)
                        .classStatus(ClassStatus.IN_PROGRESS)
                        .credits((short) 3)
                        .build()
        );

        com.edupedu.app.model.Class csClass11 = classRepository.save(
                com.edupedu.app.model.Class.builder()
                        .name("ENG-11A-CS")
                        .faculty(engineering)
                        .subject(cs)
                        .teacher(janeTeacher)
                        .university(university)
                        .classStatus(ClassStatus.IN_PROGRESS)
                        .credits((short) 3)
                        .build()
        );

        var today = LocalDate.now();
        var todayDay = today.getDayOfWeek();
        var secondaryDay = todayDay == DayOfWeek.MONDAY ? DayOfWeek.WEDNESDAY : DayOfWeek.MONDAY;
        var tertiaryDay = todayDay == DayOfWeek.FRIDAY ? DayOfWeek.THURSDAY : DayOfWeek.FRIDAY;

        Schedule todayMathSchedule = scheduleRepository.save(
                Schedule.builder()
                        .studentGroup(group10A)
                        .clazz(mathClass10)
                        .dayOfWeek(todayDay)
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(9, 45))
                        .room("A-201")
                        .lessonNumber(1)
                        .build()
        );

        Schedule weekPhysicsSchedule = scheduleRepository.save(
                Schedule.builder()
                        .studentGroup(group10A)
                        .clazz(physicsClass10)
                        .dayOfWeek(secondaryDay)
                        .startTime(LocalTime.of(10, 0))
                        .endTime(LocalTime.of(10, 45))
                        .room("A-105")
                        .lessonNumber(2)
                        .build()
        );

        Schedule todayCsSchedule = scheduleRepository.save(
                Schedule.builder()
                        .studentGroup(group11A)
                        .clazz(csClass11)
                        .dayOfWeek(todayDay)
                        .startTime(LocalTime.of(11, 0))
                        .endTime(LocalTime.of(11, 45))
                        .room("B-301")
                        .lessonNumber(3)
                        .build()
        );

        Schedule weekCsSchedule = scheduleRepository.save(
                Schedule.builder()
                        .studentGroup(group11A)
                        .clazz(csClass11)
                        .dayOfWeek(tertiaryDay)
                        .startTime(LocalTime.of(13, 0))
                        .endTime(LocalTime.of(13, 45))
                        .room("B-301")
                        .lessonNumber(4)
                        .build()
        );

        Semester aliceSemester = semesterRepository.save(
                Semester.builder()
                        .student(alice)
                        .semesterNumber(1)
                        .startDate(today.minusMonths(2))
                        .endDate(today.plusMonths(2))
                        .isActive(true)
                        .gpa(3.7)
                        .totalCredits(7)
                        .build()
        );

        Semester bobSemester = semesterRepository.save(
                Semester.builder()
                        .student(bob)
                        .semesterNumber(1)
                        .startDate(today.minusMonths(2))
                        .endDate(today.plusMonths(2))
                        .isActive(true)
                        .gpa(3.2)
                        .totalCredits(7)
                        .build()
        );

        Semester charlieSemester = semesterRepository.save(
                Semester.builder()
                        .student(charlie)
                        .semesterNumber(1)
                        .startDate(today.minusMonths(2))
                        .endDate(today.plusMonths(2))
                        .isActive(true)
                        .gpa(3.9)
                        .totalCredits(3)
                        .build()
        );

        Semester dianaSemester = semesterRepository.save(
                Semester.builder()
                        .student(diana)
                        .semesterNumber(1)
                        .startDate(today.minusMonths(2))
                        .endDate(today.plusMonths(2))
                        .isActive(true)
                        .gpa(3.6)
                        .totalCredits(7)
                        .build()
        );

        Semester emmaSemester = semesterRepository.save(
                Semester.builder()
                        .student(emma)
                        .semesterNumber(1)
                        .startDate(today.minusMonths(2))
                        .endDate(today.plusMonths(2))
                        .isActive(true)
                        .gpa(3.8)
                        .totalCredits(3)
                        .build()
        );

        Semester faridSemester = semesterRepository.save(
                Semester.builder()
                        .student(farid)
                        .semesterNumber(1)
                        .startDate(today.minusMonths(2))
                        .endDate(today.plusMonths(2))
                        .isActive(true)
                        .gpa(3.1)
                        .totalCredits(3)
                        .build()
        );

        TakenClass aliceMath = takenClassRepository.save(
                TakenClass.builder()
                        .clazz(mathClass10)
                        .semester(aliceSemester)
                        .midtermExamScore(84)
                        .finalExamScore(89)
                        .totalScore(173)
                        .gpa(3.7)
                        .build()
        );

        TakenClass alicePhysics = takenClassRepository.save(
                TakenClass.builder()
                        .clazz(physicsClass10)
                        .semester(aliceSemester)
                        .midtermExamScore(81)
                        .finalExamScore(86)
                        .totalScore(167)
                        .gpa(3.5)
                        .build()
        );

        TakenClass bobMath = takenClassRepository.save(
                TakenClass.builder()
                        .clazz(mathClass10)
                        .semester(bobSemester)
                        .midtermExamScore(72)
                        .finalExamScore(79)
                        .totalScore(151)
                        .gpa(3.0)
                        .build()
        );

        TakenClass bobPhysics = takenClassRepository.save(
                TakenClass.builder()
                        .clazz(physicsClass10)
                        .semester(bobSemester)
                        .midtermExamScore(68)
                        .finalExamScore(74)
                        .totalScore(142)
                        .gpa(2.8)
                        .build()
        );

        TakenClass dianaMath = takenClassRepository.save(
                TakenClass.builder()
                        .clazz(mathClass10)
                        .semester(dianaSemester)
                        .midtermExamScore(86)
                        .finalExamScore(84)
                        .totalScore(170)
                        .gpa(3.6)
                        .build()
        );

        TakenClass dianaPhysics = takenClassRepository.save(
                TakenClass.builder()
                        .clazz(physicsClass10)
                        .semester(dianaSemester)
                        .midtermExamScore(79)
                        .finalExamScore(83)
                        .totalScore(162)
                        .gpa(3.3)
                        .build()
        );

        TakenClass charlieCs = takenClassRepository.save(
                TakenClass.builder()
                        .clazz(csClass11)
                        .semester(charlieSemester)
                        .midtermExamScore(93)
                        .finalExamScore(96)
                        .totalScore(189)
                        .gpa(4.0)
                        .build()
        );

        TakenClass emmaCs = takenClassRepository.save(
                TakenClass.builder()
                        .clazz(csClass11)
                        .semester(emmaSemester)
                        .midtermExamScore(90)
                        .finalExamScore(91)
                        .totalScore(181)
                        .gpa(3.8)
                        .build()
        );

        TakenClass faridCs = takenClassRepository.save(
                TakenClass.builder()
                        .clazz(csClass11)
                        .semester(faridSemester)
                        .midtermExamScore(76)
                        .finalExamScore(79)
                        .totalScore(155)
                        .gpa(3.1)
                        .build()
        );

        List<Grade> grades = new ArrayList<>();
        grades.add(Grade.builder()
                .takenClass(aliceMath)
                .value(88.0)
                .maxValue(100.0)
                .gradeType("QUIZ")
                .description("Linear equations quiz")
                .date(today.minusDays(8))
                .build());
        grades.add(Grade.builder()
                .takenClass(alicePhysics)
                .value(92.0)
                .maxValue(100.0)
                .gradeType("LAB")
                .description("Forces and motion lab")
                .date(today.minusDays(6))
                .build());
        grades.add(Grade.builder()
                .takenClass(bobMath)
                .value(74.0)
                .maxValue(100.0)
                .gradeType("QUIZ")
                .description("Functions checkpoint")
                .date(today.minusDays(7))
                .build());
        grades.add(Grade.builder()
                .takenClass(bobPhysics)
                .value(69.0)
                .maxValue(100.0)
                .gradeType("HOMEWORK")
                .description("Newton laws worksheet")
                .date(today.minusDays(5))
                .build());
        grades.add(Grade.builder()
                .takenClass(charlieCs)
                .value(97.0)
                .maxValue(100.0)
                .gradeType("PROJECT")
                .description("Intro programming mini-project")
                .date(today.minusDays(4))
                .build());
        grades.add(Grade.builder()
                .takenClass(dianaMath)
                .value(85.0)
                .maxValue(100.0)
                .gradeType("QUIZ")
                .description("Algebra practice quiz")
                .date(today.minusDays(3))
                .build());
        grades.add(Grade.builder()
                .takenClass(emmaCs)
                .value(93.0)
                .maxValue(100.0)
                .gradeType("LAB")
                .description("Algorithms lab")
                .date(today.minusDays(2))
                .build());
        grades.add(Grade.builder()
                .takenClass(faridCs)
                .value(78.0)
                .maxValue(100.0)
                .gradeType("HOMEWORK")
                .description("Programming syntax homework")
                .date(today.minusDays(2))
                .build());
        gradeRepository.saveAll(grades);

        List<Attendance> attendanceRecords = new ArrayList<>();
        attendanceRecords.add(buildAttendance(alice, todayMathSchedule, today, AttendanceStatus.PRESENT, "On time", teacherUser));
        attendanceRecords.add(buildAttendance(bob, todayMathSchedule, today, AttendanceStatus.LATE, "Arrived 10 minutes late", teacherUser));
        attendanceRecords.add(buildAttendance(charlie, todayCsSchedule, today, AttendanceStatus.PRESENT, "Prepared for class", teacherUser1));
        attendanceRecords.add(buildAttendance(emma, todayCsSchedule, today, AttendanceStatus.EXCUSED, "School event", teacherUser1));
        attendanceRecords.add(buildAttendance(alice, weekPhysicsSchedule, today.minusDays(2), AttendanceStatus.PRESENT, null, teacherUser));
        attendanceRecords.add(buildAttendance(bob, weekPhysicsSchedule, today.minusDays(2), AttendanceStatus.ABSENT, "Parent notified", teacherUser));
        attendanceRecords.add(buildAttendance(diana, weekPhysicsSchedule, today.minusDays(2), AttendanceStatus.PRESENT, null, teacherUser));
        attendanceRecords.add(buildAttendance(alice, todayMathSchedule, today.minusDays(4), AttendanceStatus.PRESENT, null, teacherUser));
        attendanceRecords.add(buildAttendance(bob, todayMathSchedule, today.minusDays(4), AttendanceStatus.EXCUSED, "Medical note provided", teacherUser));
        attendanceRecords.add(buildAttendance(charlie, weekCsSchedule, today.minusDays(3), AttendanceStatus.PRESENT, null, teacherUser1));
        attendanceRecords.add(buildAttendance(emma, weekCsSchedule, today.minusDays(3), AttendanceStatus.PRESENT, null, teacherUser1));
        attendanceRecords.add(buildAttendance(farid, weekCsSchedule, today.minusDays(3), AttendanceStatus.LATE, "Traffic delay", teacherUser1));
        attendanceRepository.saveAll(attendanceRecords);

        announcementRepository.save(
                Announcement.builder()
                        .title("Weekly leadership briefing")
                        .content("Please review the updated attendance and performance snapshot before Friday.")
                        .author(teacherUser)
                        .targetRole(Role.ROLE_UNIVERSITY_ADMIN)
                        .important(true)
                        .expiresAt(LocalDateTime.now().plusDays(10))
                        .build()
        );

        announcementRepository.save(
                Announcement.builder()
                        .title("Math quiz on Friday")
                        .content("Grade 10A will have a short mathematics quiz this Friday during the first lesson.")
                        .author(teacherUser)
                        .targetStudentGroup(group10A)
                        .important(true)
                        .expiresAt(LocalDateTime.now().plusDays(7))
                        .build()
        );

        announcementRepository.save(
                Announcement.builder()
                        .title("Computer science lab updated")
                        .content("Bring your project notes for the next CS session. Evaluation will count toward the project grade.")
                        .author(teacherUser1)
                        .targetStudentGroup(group11A)
                        .important(false)
                        .expiresAt(LocalDateTime.now().plusDays(7))
                        .build()
        );

        announcementRepository.save(
                Announcement.builder()
                        .title("Campus access reminder")
                        .content("Student IDs are required at the front desk starting next week.")
                        .author(teacherUser)
                        .important(false)
                        .expiresAt(LocalDateTime.now().plusDays(14))
                        .build()
        );

        // Add some lacking data like a sample course and module to make testing easier
        Course csCourse = courseRepository.save(
                Course.builder()
                        .title("Introduction to Computer Science")
                        .description("A comprehensive guide to CS basics")
                        .teacher(janeTeacher)
                        .university(university)
                        .isPublic(true)
                        .build()
        );

        Module module1 = moduleRepository.save(
                Module.builder()
                        .title("Module 1: Getting Started")
                        .course(csCourse)
                        .orderIndex(1)
                        .build()
        );

        courseLessonRepository.save(
                CourseLesson.builder()
                        .title("Variables in Programming")
                        .content("This is an introductory lesson on variables and datatypes. In this lesson, we will cover integer types, strings, and boolean values.")
                        .contentType(ContentType.TEXT)
                        .orderIndex(1)
                        .module(module1)
                        .build()
        );

        CourseTest test1 = courseTestRepository.save(
                CourseTest.builder()
                        .title("Quiz 1: Basics of Programming")
                        .timeLimitMinutes(15)
                        .randomizeQuestions(true)
                        .randomizeChoices(true)
                        .passingScore(60.0)
                        .module(module1)
                        .build()
        );

        CourseTestQuestion q1 = courseTestQuestionRepository.save(
                CourseTestQuestion.builder()
                        .text("What is a variable?")
                        .courseTestQuestionType(CourseTestQuestionType.SINGLE_CHOICE)
                        .orderIndex(1)
                        .courseTest(test1)
                        .build()
        );

        courseTestQuestionChoiceRepository.save(
                CourseTestQuestionChoice.builder()
                        .text("A container for storing data values")
                        .isCorrect(true)
                        .orderIndex(1)
                        .courseTestQuestion(q1)
                        .build()
        );

        courseTestQuestionChoiceRepository.save(
                CourseTestQuestionChoice.builder()
                        .text("A type of loop")
                        .isCorrect(false)
                        .orderIndex(2)
                        .courseTestQuestion(q1)
                        .build()
        );

        courseTestQuestionChoiceRepository.save(
                CourseTestQuestionChoice.builder()
                        .text("A special function")
                        .isCorrect(false)
                        .orderIndex(3)
                        .courseTestQuestion(q1)
                        .build()
        );

        log.info("Database seeded successfully with roles, university, subjects, and sample user data, including course content.");
    }

    private Attendance buildAttendance(
            Student student,
            Schedule schedule,
            LocalDate date,
            AttendanceStatus status,
            String notes,
            User markedBy) {
        return Attendance.builder()
                .student(student)
                .schedule(schedule)
                .date(date)
                .status(status)
                .notes(notes)
                .markedBy(markedBy)
                .markedAt(LocalDateTime.now().minusHours(1))
                .build();
    }
}
