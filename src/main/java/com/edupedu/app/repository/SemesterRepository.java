package com.edupedu.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edupedu.app.model.Semester;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {
}
