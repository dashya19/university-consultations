package com.university.consultations.repository;

import com.university.consultations.entity.ConsultationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsultationTemplateRepository extends JpaRepository<ConsultationTemplate, Integer> {
    List<ConsultationTemplate> findByTeacherId(Integer teacherId);

    @Query("SELECT t FROM ConsultationTemplate t WHERE t.expirationDateTime IS NOT NULL AND t.expirationDateTime <= :now")
    List<ConsultationTemplate> findExpiredTemplates(@Param("now") LocalDateTime now);
}