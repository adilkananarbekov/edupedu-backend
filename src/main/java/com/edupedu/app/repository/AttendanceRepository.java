
package com.edupedu.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.edupedu.app.model.Attendance;
import com.edupedu.app.model.enums.AttendanceStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudentId(Long studentId);

    List<Attendance> findByScheduleId(Long scheduleId);

    List<Attendance> findByDate(LocalDate date);

    @Query("SELECT a FROM Attendance a WHERE a.student.id = :studentId AND a.date BETWEEN :startDate AND :endDate ORDER BY a.date DESC")
    List<Attendance> findByStudentAndDateRange(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    Optional<Attendance> findByStudentIdAndScheduleIdAndDate(Long studentId, Long scheduleId, LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId AND a.status = :status")
    Long countByStudentAndStatus(@Param("studentId") Long studentId, @Param("status") AttendanceStatus status);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.studentGroup.id = :studentGroupId AND a.status = :status")
    Long countByStudentGroupAndStatus(@Param("studentGroupId") Long studentGroupId, @Param("status") AttendanceStatus status);
}
