package com.university.consultations.repository;

import com.university.consultations.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Integer> {
    Student findByUser_Login(String login);

    @Query("SELECT t FROM Student t WHERE " +
            "LOWER(CONCAT(t.lastName, ' ', t.firstName, ' ', t.patronymic)) LIKE LOWER(CONCAT('%', :fullName, '%'))")
    List<Student> searchByFullName(@Param("fullName") String fullName);
}
