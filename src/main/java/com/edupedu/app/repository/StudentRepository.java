package com.edupedu.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.edupedu.app.model.Student;
import com.edupedu.app.model.StudentGroup;
import com.edupedu.app.model.User;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByStudentGroup(StudentGroup studentGroup);
    List<Student> findByStudentGroupId(Long studentGroupId);
    boolean existsByStudentGroupId(Long studentGroupId);
    
    Optional<Student> findByUserId(Long userId);
    Optional<Student> findByUserEmail(String email);

    boolean existsByUserId(Long userId);

    @Query("SELECT s FROM Student s WHERE s.studentGroup.faculty.university.id = :universityId")
    List<Student> findAllByUniversityId(@Param("universityId") Long universityId);

    @Query("SELECT s FROM Student s WHERE s.studentGroup.faculty.university.id = :universityId AND s.studentGroup IS NULL")
    List<Student> findAllByUniversityIdAndStudentGroupIsNull(@Param("universityId") Long universityId);
}
