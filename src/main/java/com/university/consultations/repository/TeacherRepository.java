package com.university.consultations.repository;

import com.university.consultations.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeacherRepository extends JpaRepository<Teacher, Integer> {
    Teacher findByUser_Login(String login);

    @Query("SELECT t FROM Teacher t WHERE " +
            "LOWER(CONCAT(t.lastName, ' ', t.firstName, ' ', t.patronymic)) LIKE LOWER(CONCAT('%', :fullName, '%'))")
    List<Teacher> searchByFullName(@Param("fullName") String fullName);
}