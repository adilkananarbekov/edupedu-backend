package com.edupedu.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.edupedu.app.model.Teacher;
import com.edupedu.app.model.User;

@Repository
public interface TeacherRepository  extends JpaRepository<Teacher, Long>{
    Optional<Teacher> findByUserId(Long userId);
    Optional<Teacher> findByUserEmail(String email);

    @Query("SELECT t FROM Teacher t WHERE t.user.university.id = :universityId")
    List<Teacher> findAllByUniversityId(@Param("universityId") Long universityId);
}   
