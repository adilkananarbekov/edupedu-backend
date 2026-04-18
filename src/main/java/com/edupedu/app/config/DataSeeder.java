package com.edupedu.app.config;

import com.edupedu.app.model.*;
import com.edupedu.app.model.Module;
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

import java.util.HashSet;
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
}
