package com.edupedu.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edupedu.app.model.Curator;

@Repository
public interface CuratorRepository extends JpaRepository<Curator, Long> {
    Optional<Curator> findByTeacherId(Long teacherId);

    Optional<Curator> findByStudentGroupId(Long studentGroupId);
}
