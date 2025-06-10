package com.university.consultations.controller;

import com.university.consultations.entity.*;
import com.university.consultations.repository.*;
import com.university.consultations.service.ConsultationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class StudentController {
    private final StudentRepository studentRepository;
    private final ConsultationSignupRepository consultationSignupRepository;
    private final ConsultationArchiveRepository consultationArchiveRepository;
    private final NotificationRepository notificationRepository;
    public final TeacherRepository teacherRepository;
    public final ConsultationService consultationService;
    public final ConsultationRepository consultationRepository;

    public StudentController(StudentRepository studentRepository,
                             ConsultationSignupRepository consultationSignupRepository,
                             ConsultationArchiveRepository consultationArchiveRepository,
                             NotificationRepository notificationRepository,
                             TeacherRepository teacherRepository,
                             ConsultationService consultationService,
                             ConsultationRepository consultationRepository) {
        this.studentRepository = studentRepository;
        this.consultationSignupRepository = consultationSignupRepository;
        this.consultationArchiveRepository = consultationArchiveRepository;
        this.notificationRepository = notificationRepository;
        this.teacherRepository = teacherRepository;
        this.consultationService = consultationService;
        this.consultationRepository = consultationRepository;
    }
    @GetMapping("/student/dashboard")
    public String showStudentDashboard(Authentication authentication, Model model) {
        Student student = studentRepository.findByUser_Login(authentication.getName());

        List<ConsultationSignup> studentSignups = consultationSignupRepository.findByStudentId(student.getId());
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(student.getUser().getId());
        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(student.getUser().getId());

        model.addAttribute("username", authentication.getName());
        model.addAttribute("student", student);
        model.addAttribute("hasPhoto", student.getPhotoPath() != null && !student.getPhotoPath().isEmpty());
        model.addAttribute("studentSignups", studentSignups);
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);

        return "student";
    }
    @PostMapping("/student/updateFields")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateFields(
            @RequestParam(name = "fieldType") String fieldType,
            @RequestParam(name = "fieldInput") String fieldInput,
            Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        try {
            Student student = studentRepository.findByUser_Login(authentication.getName());
            if (fieldType.equals("email")) {
                if (!fieldInput.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$") && !fieldInput.isEmpty()) {
                    response.put("error", "Введите корректный email адрес");
                    return ResponseEntity.badRequest().body(response);
                }
                student.setEmail(fieldInput.isEmpty() ? null : fieldInput);
            } else if (fieldType.equals("phone")) {
                String cleanedPhone = fieldInput.replaceAll("\\D", "");
                if (!cleanedPhone.isEmpty() && !cleanedPhone.startsWith("8")) {
                    cleanedPhone = "8" + cleanedPhone;
                }
                if (!cleanedPhone.isEmpty() && cleanedPhone.length() != 11) {
                    response.put("error", "Номер телефона должен содержать 11 цифр");
                    return ResponseEntity.badRequest().body(response);
                }
                student.setPhone(cleanedPhone.isEmpty() ? null : cleanedPhone);
            }
            studentRepository.save(student);
            response.put("success", "Данные успешно обновлены");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Ошибка при обновлении данных");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/student/archive")
    @ResponseBody
    public ResponseEntity<List<ConsultationArchive>> getStudentArchive(Authentication authentication) {
        Student student = studentRepository.findByUser_Login(authentication.getName());
        List<ConsultationArchive> archive = consultationArchiveRepository.findFullArchiveByStudentId(student.getId());
        return ResponseEntity.ok(archive);
    }
    @PostMapping("/student/notifications/mark-as-read")
    @ResponseBody
    public ResponseEntity<?> markNotificationsAsRead(@RequestBody List<Integer> notificationIds, Authentication authentication) {
        Student student = studentRepository.findByUser_Login(authentication.getName());
        List<Notification> notifications = notificationRepository.findAllById(notificationIds);
        notifications.forEach(notification -> {
            if (notification.getUser().getId().equals(student.getUser().getId())) {
                notification.setIsRead(true);
                notificationRepository.save(notification);
            }
        });
        return ResponseEntity.ok().build();
    }
    @GetMapping("/student/notifications/unread-count")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        Student student = studentRepository.findByUser_Login(authentication.getName());
        long count = notificationRepository.countByUserIdAndIsReadFalse(student.getUser().getId());
        return ResponseEntity.ok(Collections.singletonMap("count", count));
    }
    @GetMapping("/api/students/search")
    @ResponseBody
    public ResponseEntity<List<Student>> searchStudents(@RequestParam("query") String query) {
        List<Student> results = studentRepository.searchByFullName(query);
        return ResponseEntity.ok(results);
    }
    @PostMapping("/student/consultation/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, String>> cancelConsultation(
            @RequestParam("signupId") Integer signupId,
            Authentication authentication) {

        Map<String, String> response = new HashMap<>();
        Student student = studentRepository.findByUser_Login(authentication.getName());

        try {
            // Проверяем, что запись принадлежит текущему студенту
            ConsultationSignup signup = consultationSignupRepository.findById(signupId)
                    .orElseThrow(() -> new RuntimeException("Запись не найдена"));

            if (!signup.getStudent().getId().equals(student.getId())) {
                response.put("error", "Вы не можете отменить чужую запись");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Создаем уведомление для преподавателя
            Notification notification = new Notification();
            notification.setUser(signup.getConsultation().getTeacher().getUser());
            notification.setMessage(String.format(
                    "Студент %s %s %s отменил запись на вашу консультацию %s в %s",
                    student.getLastName(),
                    student.getFirstName(),
                    student.getPatronymic(),
                    signup.getConsultation().getConsultationDate(),
                    signup.getConsultation().getStartTime()
            ));
            notification.setRelatedEntityType("CONSULTATION");
            notification.setRelatedEntityId(signup.getConsultation().getId());
            notificationRepository.save(notification);

            // Удаляем запись из основной таблицы
            consultationSignupRepository.delete(signup);

            // Удаляем архивную запись, если она есть
            List<ConsultationArchive> archives = consultationArchiveRepository.findByConsultationIdAndStudentId(
                    signup.getConsultation().getId(),
                    student.getId()
            );

            if (!archives.isEmpty()) {
                consultationArchiveRepository.deleteAll(archives);
            }

            response.put("success", "Запись успешно отменена");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Ошибка при отмене записи: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}