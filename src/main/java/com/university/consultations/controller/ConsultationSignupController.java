package com.university.consultations.controller;

import com.university.consultations.entity.Consultation;
import com.university.consultations.entity.ConsultationArchive;
import com.university.consultations.entity.ConsultationSignup;
import com.university.consultations.entity.Student;
import com.university.consultations.repository.ConsultationArchiveRepository;
import com.university.consultations.repository.ConsultationRepository;
import com.university.consultations.repository.ConsultationSignupRepository;
import com.university.consultations.repository.StudentRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationSignupController {
    private final ConsultationSignupRepository signupRepository;
    private final ConsultationRepository consultationRepository;
    private final StudentRepository studentRepository;
    private final ConsultationArchiveRepository archiveRepository;
    public ConsultationSignupController(ConsultationSignupRepository signupRepository,
                                        ConsultationRepository consultationRepository,
                                        StudentRepository studentRepository,
                                        ConsultationArchiveRepository archiveRepository) {
        this.signupRepository = signupRepository;
        this.consultationRepository = consultationRepository;
        this.studentRepository = studentRepository;
        this.archiveRepository = archiveRepository;
    }
    @PostMapping("/register")
    public ResponseEntity<?> registerForConsultation(
            @RequestBody RegisterRequest request,
            Authentication authentication) {
        try {
            Student student = studentRepository.findByUser_Login(authentication.getName());
            if (student == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Пользователь не авторизован"));
            }

            Consultation consultation = consultationRepository.findById(request.getConsultationId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Консультация не найдена"
                    ));

            // Проверка, не началась ли уже консультация
            LocalDateTime consultationStart = LocalDateTime.of(consultation.getConsultationDate(), consultation.getStartTime());
            if (LocalDateTime.now().isAfter(consultationStart)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Невозможно записаться на консультацию, так как она уже началась"));
            }

            if (signupRepository.existsByConsultationIdAndStudentId(consultation.getId(), student.getId())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Вы уже записаны на эту консультацию"));
            }

            long signupsCount = signupRepository.countByConsultationId(consultation.getId());
            if (signupsCount >= consultation.getMaxStudents()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Достигнуто максимальное количество записей"));
            }

            // Создаем запись на консультацию
            ConsultationSignup signup = new ConsultationSignup();
            signup.setConsultation(consultation);
            signup.setStudent(student);
            signup.setReason(request.getReason());
            signup.setSignupTime(LocalDateTime.now());
            signupRepository.save(signup);

            // Создаем архивную запись
            ConsultationArchive archive = new ConsultationArchive();
            archive.setConsultation(consultation);
            archive.setStudent(student);
            archive.setAttended(null);
            archive.setFeedback(null);
            archive.setArchivedAt(Timestamp.valueOf(LocalDateTime.now()));
            archive.setReason(request.getReason());
            archive.setConsultationDate(consultation.getConsultationDate());
            archive.setStartTime(consultation.getStartTime());
            archive.setEndTime(consultation.getEndTime());
            archive.setRoom(consultation.getRoom());
            archive.setTeacherId(consultation.getTeacher().getId());
            archiveRepository.save(archive);

            // Возвращаем успешный ответ с данными о записи
            return ResponseEntity.ok(Map.of(
                    "success", "Вы успешно записаны на консультацию",
                    "signupId", signup.getId(),
                    "consultationDate", consultation.getConsultationDate().toString(),
                    "teacherName", consultation.getTeacher().getLastName() + " " +
                            consultation.getTeacher().getFirstName() + " " +
                            consultation.getTeacher().getPatronymic()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка сервера: " + e.getMessage()));
        }
    }
    @PostMapping("/finish-consultation/{consultationId}")
    public ResponseEntity<?> finishConsultation(@PathVariable Integer consultationId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Консультация не найдена"));
        List<ConsultationSignup> signups = signupRepository.findByConsultationId(consultationId);
        for (ConsultationSignup signup : signups) {
            ConsultationArchive archive = archiveRepository.findByConsultationIdAndStudentId(consultationId, signup.getStudent().getId())
                    .stream()
                    .findFirst()
                    .orElse(new ConsultationArchive());
            archive.setConsultation(consultation);
            archive.setStudent(signup.getStudent());
            archive.setAttended(true);
            archive.setFeedback("Добавить отзыв");
            archive.setReason(signup.getReason());
            archive.setConsultationDate(consultation.getConsultationDate());
            archive.setStartTime(consultation.getStartTime());
            archive.setEndTime(consultation.getEndTime());
            archive.setRoom(consultation.getRoom());
            archive.setTeacherId(consultation.getTeacher().getId());
            archive.setTeacher(consultation.getTeacher());
            archive.setArchivedAt(Timestamp.valueOf(LocalDateTime.now()));
            archiveRepository.save(archive);
        }
        signupRepository.deleteAll(signups);
        return ResponseEntity.ok(Map.of("success", "Консультация завершена, записи архивированы"));
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        private Integer consultationId;
        private String reason;
    }
}