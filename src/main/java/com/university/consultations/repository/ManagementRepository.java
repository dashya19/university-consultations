package com.university.consultations.repository;

import com.university.consultations.entity.Management;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagementRepository extends JpaRepository<Management, Integer> {
    Management findByUser_Login(String login);
}
