package com.university.consultations.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "consultation_templates")
@Data
public class ConsultationTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private String room;

    @Column(nullable = false)
    private Integer maxStudents;

    @Column(nullable = false)
    private Boolean isOneTime = false;

    @Column(name = "expiration_datetime")
    private LocalDateTime expirationDateTime; // Новое поле для даты и времени удаления

    @Column(name = "is_bi_weekly", nullable = false)
    private Boolean isBiWeekly = false;

    @Column(name = "start_next_week", nullable = false)
    private Boolean startNextWeek = false;
}