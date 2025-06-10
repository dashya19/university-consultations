package com.university.consultations.repository;

import com.university.consultations.entity.ConsultationSignup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultationSignupRepository extends JpaRepository<ConsultationSignup, Integer> {
    List<ConsultationSignup> findByConsultationId(Integer consultationId);
    boolean existsByConsultationIdAndStudentId(Integer consultationId, Integer studentId);

    List<ConsultationSignup> findByStudentId(Integer studentId);

    @Query("SELECT COUNT(cs) FROM ConsultationSignup cs WHERE cs.consultation.id = :consultationId")
    long countByConsultationId(@Param("consultationId") Integer consultationId);
}
