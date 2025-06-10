package com.university.consultations.repository;

import com.university.consultations.entity.ConsultationArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface ConsultationArchiveRepository extends JpaRepository<ConsultationArchive, Integer> {
    List<ConsultationArchive> findByStudentId(Integer studentId);
    List<ConsultationArchive> findByConsultationId(Integer consultationId);
    List<ConsultationArchive> findByConsultationIdAndStudentId(Integer consultationId, Integer studentId);

    // Метод для получения полного архива студента
    @Query("SELECT ca FROM ConsultationArchive ca " +
            "LEFT JOIN FETCH ca.consultation c " +
            "LEFT JOIN FETCH c.teacher t " +
            "WHERE ca.student.id = :studentId " +
            "ORDER BY ca.consultationDate DESC, ca.startTime DESC")
    List<ConsultationArchive> findFullArchiveByStudentId(@Param("studentId") Integer studentId);

    // Метод для получения архива преподавателя
    @Query("SELECT ca FROM ConsultationArchive ca " +
            "LEFT JOIN FETCH ca.student s " +
            "LEFT JOIN FETCH ca.consultation c " +
            "LEFT JOIN FETCH c.teacher t " +
            "WHERE (c.teacher.id = :teacherId OR ca.teacherId = :teacherId) AND ca.isHidden = false " +
            "ORDER BY ca.consultationDate DESC, ca.startTime DESC")
    List<ConsultationArchive> findFullArchiveByTeacherId(@Param("teacherId") Integer teacherId);

    boolean existsByConsultationIdAndStudentId(Integer consultationId, Integer studentId);

    // Метод для поиска по ID консультации, студента и преподавателя
    @Query("SELECT ca FROM ConsultationArchive ca " +
            "JOIN ca.consultation c " +
            "WHERE ca.id = :id AND ca.student.id = :studentId AND c.teacher.id = :teacherId")
    ConsultationArchive findByIdAndStudentIdAndTeacherId(
            @Param("id") Integer id,
            @Param("studentId") Integer studentId,
            @Param("teacherId") Integer teacherId);

    @Query("SELECT ca FROM ConsultationArchive ca " +
            "JOIN FETCH ca.student s " +
            "WHERE ca.teacherId = :teacherId AND ca.isHidden = false " +
            "ORDER BY ca.consultationDate DESC, ca.startTime DESC")
    List<ConsultationArchive> findArchiveByTeacherIdIgnoringConsultation(@Param("teacherId") Integer teacherId);

    @Query("SELECT " +
            "COUNT(ca) AS total, " +
            "SUM(CASE WHEN ca.attended = true THEN 1 ELSE 0 END) AS attendedCount, " +
            "SUM(CASE WHEN ca.attended = false THEN 1 ELSE 0 END) AS notAttendedCount " +
            "FROM ConsultationArchive ca " +
            "WHERE ca.student.id = :studentId")
    Map<String, Long> findAttendanceStatisticsByStudentId(@Param("studentId") Integer studentId);

    @Query("SELECT " +
            "COUNT(ca) AS total, " +
            "SUM(CASE WHEN ca.attended = true THEN 1 ELSE 0 END) AS attendedCount, " +
            "SUM(CASE WHEN ca.attended = false THEN 1 ELSE 0 END) AS notAttendedCount " +
            "FROM ConsultationArchive ca " +
            "LEFT JOIN ca.consultation c " +
            "WHERE (c.teacher.id = :teacherId OR ca.teacherId = :teacherId) AND ca.attended IS NOT NULL")
    Map<String, Long> findAttendanceStatisticsByTeacherId(@Param("teacherId") Integer teacherId);

    @Query("SELECT COUNT(ca) as total, " +
            "SUM(CASE WHEN ca.debtStatus = 'Сдал задолженность' THEN 1 ELSE 0 END) as passedCount, " +
            "SUM(CASE WHEN ca.debtStatus = 'Не сдал задолженность' THEN 1 ELSE 0 END) as failedCount " +
            "FROM ConsultationArchive ca WHERE ca.student.id = :studentId AND ca.debtStatus IN ('Сдал задолженность', 'Не сдал задолженность')")
    Map<String, Long> findDebtStatisticsByStudentId(@Param("studentId") Integer studentId);

    @Query("SELECT COUNT(ca) as total, " +
            "SUM(CASE WHEN ca.debtStatus = 'Сдал задолженность' THEN 1 ELSE 0 END) as passedCount, " +
            "SUM(CASE WHEN ca.debtStatus = 'Не сдал задолженность' THEN 1 ELSE 0 END) as failedCount " +
            "FROM ConsultationArchive ca WHERE ca.teacher.id = :teacherId AND ca.debtStatus IN ('Сдал задолженность', 'Не сдал задолженность')")
    Map<String, Long> findDebtStatisticsByTeacherId(@Param("teacherId") Integer teacherId);
}