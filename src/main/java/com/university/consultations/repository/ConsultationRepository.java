package com.university.consultations.repository;

import com.university.consultations.entity.Consultation;
import com.university.consultations.entity.ConsultationTemplate;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Integer> {
    List<Consultation> findByTeacherId(Integer teacherId);
    List<Consultation> findByTemplateId(Integer templateId); // Добавляем этот метод

    boolean existsByTeacherIdAndConsultationDateAndStartTime(
            Integer teacherId,
            LocalDate consultationDate,
            LocalTime startTime
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM Consultation c WHERE c.id = :id AND c.template IS NULL AND c.teacher.id = :teacherId")
    int deleteOneTimeConsultation(Integer id, Integer teacherId);
}