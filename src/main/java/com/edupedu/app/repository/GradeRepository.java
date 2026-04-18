package com.edupedu.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.edupedu.app.model.Grade;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    @Query("SELECT g FROM Grade g WHERE g.takenClass.semester.student.id = :studentId")
    List<Grade> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT g FROM Grade g WHERE g.takenClass.semester.student.id = :studentId AND g.takenClass.clazz.subject.id = :subjectId")
    List<Grade> findByStudentIdAndSubjectId(@Param("studentId") Long studentId, @Param("subjectId") Long subjectId);

    @Query("SELECT g FROM Grade g WHERE g.takenClass.clazz.teacher.id = :teacherId")
    List<Grade> findByTeacherId(@Param("teacherId") Long teacherId);

    @Query("SELECT g FROM Grade g WHERE g.takenClass.semester.student.studentGroup.id = :studentGroupId")
    List<Grade> findByStudentGroupId(@Param("studentGroupId") Long studentGroupId);

    @Query("SELECT g FROM Grade g WHERE g.takenClass.semester.student.id = :studentId ORDER BY g.date DESC")
    List<Grade> findByStudentIdOrderByDateDesc(@Param("studentId") Long studentId);

    @Query("SELECT AVG(g.value) FROM Grade g WHERE g.takenClass.semester.student.id = :studentId AND g.takenClass.clazz.subject.id = :subjectId")
    Double findAverageByStudentAndSubject(@Param("studentId") Long studentId, @Param("subjectId") Long subjectId);
}
