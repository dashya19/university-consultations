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
import org.springframework.web.server.ResponseStatusException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class TeacherController {
    private final TeacherRepository teacherRepository;
    private final ConsultationTemplateRepository consultationTemplateRepository;
    private final ConsultationRepository consultationRepository;
    private final ConsultationService consultationService;
    private final ConsultationSignupRepository consultationSignupRepository;
    private final ConsultationArchiveRepository consultationArchiveRepository;
    private final NotificationRepository notificationRepository;

    public TeacherController(TeacherRepository teacherRepository,
                             ConsultationTemplateRepository consultationTemplateRepository,
                             ConsultationRepository consultationRepository,
                             ConsultationService consultationService,
                             ConsultationSignupRepository consultationSignupRepository,
                             ConsultationArchiveRepository consultationArchiveRepository,
                             NotificationRepository notificationRepository) {
        this.teacherRepository = teacherRepository;
        this.consultationTemplateRepository = consultationTemplateRepository;
        this.consultationRepository = consultationRepository;
        this.consultationService = consultationService;
        this.consultationSignupRepository = consultationSignupRepository;
        this.consultationArchiveRepository = consultationArchiveRepository;
        this.notificationRepository = notificationRepository;
    }
    @GetMapping("/teacher/dashboard")
    public String showTeacherDashboard(Authentication authentication, Model model) {
        Teacher teacher = teacherRepository.findByUser_Login(authentication.getName());

        consultationService.updateConsultationsForTeacher(teacher);

        // Получаем регулярные шаблоны (не разовые)
        List<ConsultationTemplate> regularConsultationTemplates = consultationTemplateRepository.findByTeacherId(teacher.getId())
                .stream()
                .filter(t -> !t.getIsOneTime())
                .collect(Collectors.toList());

        // Группируем консультации по шаблонам
        Map<ConsultationTemplate, List<Consultation>> consultationsByTemplate = new LinkedHashMap<>();
        for (ConsultationTemplate template : regularConsultationTemplates) {
            List<Consultation> templateConsultations = consultationRepository.findByTemplateId(template.getId())
                    .stream()
                    .collect(Collectors.toList());
            consultationsByTemplate.put(template, templateConsultations);
        }

        // Получаем все консультации и фильтруем разовые
        List<Consultation> allConsultations = consultationRepository.findByTeacherId(teacher.getId());
        List<Consultation> oneTimeConsultations = allConsultations.stream()
                .filter(c -> c.getTemplate() == null) // Разовые консультации не имеют шаблона
                .collect(Collectors.toList());

        // Для записей студентов используем все консультации
        Map<Integer, List<ConsultationSignup>> signupsByConsultation = new HashMap<>();
        for (Consultation consultation : allConsultations) {
            List<ConsultationSignup> signups = consultationSignupRepository.findByConsultationId(consultation.getId());
            signupsByConsultation.put(consultation.getId(), signups);
        }

        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(teacher.getUser().getId());
        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(teacher.getUser().getId());

        model.addAttribute("username", authentication.getName());
        model.addAttribute("teacher", teacher);
        model.addAttribute("hasPhoto", teacher.getPhotoPath() != null && !teacher.getPhotoPath().isEmpty());
        model.addAttribute("consultations", allConsultations); // конкретные консультации
        model.addAttribute("regularConsultationTemplates", regularConsultationTemplates); // только регулярные шаблоны
        model.addAttribute("consultationsByTemplate", consultationsByTemplate); // консультации по шаблонам
        model.addAttribute("oneTimeConsultations", oneTimeConsultations); // разовые консультации
        model.addAttribute("signupsByConsultation", signupsByConsultation);
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);

        return "teacher";
    }
    @PostMapping("/teacher/updateFields")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateFields(
            @RequestParam(name = "fieldType") String fieldType,
            @RequestParam(name = "fieldInput") String fieldInput,
            Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        try {
            Teacher teacher = teacherRepository.findByUser_Login(authentication.getName());
            if (fieldType.equals("email")) {
                if (!fieldInput.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$") && !fieldInput.isEmpty()) {
                    response.put("error", "Введите корректный email адрес");
                    return ResponseEntity.badRequest().body(response);
                }
                teacher.setEmail(fieldInput.isEmpty() ? null : fieldInput);
            } else if (fieldType.equals("phone")) {
                String cleanedPhone = fieldInput.replaceAll("\\D", "");
                if (!cleanedPhone.isEmpty() && !cleanedPhone.startsWith("8")) {
                    cleanedPhone = "8" + cleanedPhone;
                }
                if (!cleanedPhone.isEmpty() && cleanedPhone.length() != 11) {
                    response.put("error", "Номер телефона должен содержать 11 цифр");
                    return ResponseEntity.badRequest().body(response);
                }
                teacher.setPhone(cleanedPhone.isEmpty() ? null : cleanedPhone);
            }
            teacherRepository.save(teacher);
            response.put("success", "Данные успешно обновлены");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Ошибка при обновлении данных");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/teacher/consultation/create")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createConsultation(
            @RequestParam(value = "dayOfWeek", required = false) Integer dayOfWeek,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            @RequestParam("room") String room,
            @RequestParam("maxStudents") int maxStudents,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "isOneTime", defaultValue = "false") boolean isOneTime,
            @RequestParam(value = "expirationDate", required = false) String expirationDate,
            @RequestParam(value = "expirationTime", required = false) String expirationTime,
            @RequestParam(value = "isBiWeekly", defaultValue = "false") boolean isBiWeekly,
            @RequestParam(value = "startNextWeek", defaultValue = "false") boolean startNextWeek,
            Authentication authentication) {

        Map<String, String> response = new HashMap<>();
        try {
            Teacher teacher = teacherRepository.findByUser_Login(authentication.getName());
            LocalTime start = LocalTime.parse(startTime);
            LocalTime end = LocalTime.parse(endTime);

            if (start.isAfter(end)) {
                response.put("error", "Время начала должно быть раньше времени окончания");
                return ResponseEntity.badRequest().body(response);
            }

            if (isOneTime) {
                // Создаем разовую консультацию
                if (date == null) {
                    response.put("error", "Для разовой консультации необходимо указать дату");
                    return ResponseEntity.badRequest().body(response);
                }

                LocalDate consultationDate = LocalDate.parse(date);

                Consultation consultation = new Consultation();
                consultation.setTeacher(teacher);
                consultation.setConsultationDate(consultationDate);
                consultation.setStartTime(start);
                consultation.setEndTime(end);
                consultation.setRoom(room);
                consultation.setMaxStudents(maxStudents);
                consultationRepository.save(consultation);

                response.put("success", "Разовая консультация успешно создана");
            } else {
                // Создаем регулярный шаблон
                if (dayOfWeek == null) {
                    response.put("error", "Для регулярной консультации необходимо указать день недели");
                    return ResponseEntity.badRequest().body(response);
                }

                ConsultationTemplate template = new ConsultationTemplate();
                template.setTeacher(teacher);
                template.setDayOfWeek(DayOfWeek.of(dayOfWeek));
                template.setStartTime(start);
                template.setEndTime(end);
                template.setRoom(room);
                template.setMaxStudents(maxStudents);
                template.setIsOneTime(false);
                template.setIsBiWeekly(isBiWeekly);
                template.setStartNextWeek(startNextWeek);
                // Устанавливаем дату и время удаления, если они указаны
                if (expirationDate != null && !expirationDate.isEmpty() &&
                        expirationTime != null && !expirationTime.isEmpty()) {
                    LocalDate expDate = LocalDate.parse(expirationDate);
                    LocalTime expTime = LocalTime.parse(expirationTime);
                    template.setExpirationDateTime(LocalDateTime.of(expDate, expTime));
                }
                consultationTemplateRepository.save(template);

                // Отправляем уведомление преподавателю, если установлено время завершения
                if (template.getExpirationDateTime() != null) {
                    Notification notification = new Notification();
                    notification.setUser(teacher.getUser());
                    notification.setMessage(String.format(
                            "Шаблон регулярной консультации (%s, %s-%s) будет автоматически удален %s",
                            template.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("ru")),
                            template.getStartTime(),
                            template.getEndTime(),
                            template.getExpirationDateTime().format(DateTimeFormatter.ISO_DATE.ofPattern("dd.MM.yyyy HH:mm"))
                    ));
                    notificationRepository.save(notification);
                }

                response.put("success", "Шаблон регулярной консультации успешно создан");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Ошибка при создании консультации: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/api/teachers/search")
    @ResponseBody
    public ResponseEntity<List<Teacher>> searchTeachers(@RequestParam("query") String query) {
        List<Teacher> results = teacherRepository.searchByFullName(query);
        return ResponseEntity.ok(results);
    }
    @GetMapping("/api/teachers/{id}/consultations")
    @ResponseBody
    public ResponseEntity<List<Consultation>> getTeacherConsultations(
            @PathVariable("id") Integer teacherId,
            @RequestParam(value = "forceUpdate", required = false) Boolean forceUpdate) {

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Если запрос с флагом forceUpdate - обновляем консультации
        if (forceUpdate != null && forceUpdate) {
            consultationService.updateConsultationsForTeacher(teacher);
        }

        List<Consultation> consultations = consultationRepository.findByTeacherId(teacherId)
                .stream()
                .collect(Collectors.toList());
        return ResponseEntity.ok(consultations);
    }
    @GetMapping("/teacher/archive")
    @ResponseBody
    public ResponseEntity<List<ConsultationArchive>> getTeacherArchive(Authentication authentication) {
        Teacher teacher = teacherRepository.findByUser_Login(authentication.getName());
        List<ConsultationArchive> archive = consultationArchiveRepository.findArchiveByTeacherIdIgnoringConsultation(teacher.getId());
        return ResponseEntity.ok(archive);
    }
    @PostMapping("/teacher/archive/update")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateArchiveRecord(
            @RequestBody Map<String, Object> payload,
            Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        try {
            Integer recordId = (Integer) payload.get("id");
            Boolean attended = (Boolean) payload.get("attended");
            String feedback = (String) payload.get("feedback");
            String debtStatus = (String) payload.get("debtStatus");
            ConsultationArchive record = consultationArchiveRepository.findById(recordId)
                    .orElseThrow(() -> new RuntimeException("Запись не найдена"));
            Teacher teacher = teacherRepository.findByUser_Login(authentication.getName());
            if (!record.getTeacherId().equals(teacher.getId())) {
                response.put("error", "У вас нет прав для изменения этой записи");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            record.setAttended(attended);
            record.setFeedback(feedback);
            record.setDebtStatus(debtStatus);
            consultationArchiveRepository.save(record);
            response.put("success", "Данные обновлены");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Ошибка при обновлении: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/teacher/consultation/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteConsultationTemplate(
            @PathVariable("id") Integer templateId,
            Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        try {
            Teacher teacher = teacherRepository.findByUser_Login(authentication.getName());
            ConsultationTemplate template = consultationTemplateRepository.findById(templateId)
                    .orElseThrow(() -> new RuntimeException("Шаблон консультации не найден"));

            if (!template.getTeacher().getId().equals(teacher.getId())) {
                response.put("error", "У вас нет прав для удаления этого шаблона");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            List<Consultation> consultations = consultationRepository.findByTemplateId(templateId);

            // Обработка каждой консультации
            for (Consultation consultation : consultations) {
                // Отправка уведомлений для всех записавшихся студентов
                List<ConsultationSignup> signups = consultationSignupRepository.findByConsultationId(consultation.getId());
                for (ConsultationSignup signup : signups) {
                    Notification notification = new Notification();
                    notification.setUser(signup.getStudent().getUser());
                    notification.setMessage(String.format(
                            "Консультация с преподавателем %s %s %s на %s отменена, так как преподаватель удалил регулярную консультацию",
                            teacher.getLastName(),
                            teacher.getFirstName(),
                            teacher.getPatronymic(),
                            consultation.getConsultationDate()
                    ));
                    notification.setRelatedEntityType("CONSULTATION");
                    notification.setRelatedEntityId(consultation.getId());
                    notificationRepository.save(notification);
                }

                // Удаление архивных записей для будущих консультаций
                if (LocalDateTime.of(consultation.getConsultationDate(), consultation.getEndTime()).isAfter(LocalDateTime.now())) {
                    List<ConsultationArchive> archives = consultationArchiveRepository.findByConsultationId(consultation.getId());
                    consultationArchiveRepository.deleteAll(archives);
                }
            }

            consultationTemplateRepository.delete(template);
            response.put("success", "Шаблон консультации успешно удалён");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Ошибка при удалении шаблона: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/teacher/archive/hide/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> hideArchiveRecord(
            @PathVariable("id") Integer recordId,
            Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        try {
            Teacher teacher = teacherRepository.findByUser_Login(authentication.getName());
            ConsultationArchive record = consultationArchiveRepository.findById(recordId)
                    .orElseThrow(() -> new RuntimeException("Record not found"));
            if (!record.getTeacherId().equals(teacher.getId())) {
                response.put("error", "У вас нет прав для скрытия этой записи");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            record.setIsHidden(true);
            consultationArchiveRepository.save(record);
            response.put("success", "Запись успешно скрыта");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Ошибка при скрытии записи: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/api/consultations/{id}/signups-count")
    @ResponseBody
    public ResponseEntity<Map<String, Integer>> getConsultationSignupsCount(@PathVariable("id") Integer consultationId) {
        long count = consultationSignupRepository.countByConsultationId(consultationId);
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Map<String, Integer> response = new HashMap<>();
        response.put("current", (int) count);
        response.put("max", consultation.getMaxStudents());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/teacher/notifications/mark-as-read")
    @ResponseBody
    public ResponseEntity<?> markNotificationsAsRead(@RequestBody List<Integer> notificationIds, Authentication authentication) {
        Teacher teacher = teacherRepository.findByUser_Login(authentication.getName());
        List<Notification> notifications = notificationRepository.findAllById(notificationIds);
        notifications.forEach(notification -> {
            if (notification.getUser().getId().equals(teacher.getUser().getId())) {
                notification.setIsRead(true);
                notificationRepository.save(notification);
            }
        });
        return ResponseEntity.ok().build();
    }
    @GetMapping("/teacher/notifications/unread-count")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        Teacher teacher = teacherRepository.findByUser_Login(authentication.getName());
        long count = notificationRepository.countByUserIdAndIsReadFalse(teacher.getUser().getId());
        return ResponseEntity.ok(Collections.singletonMap("count", count));
    }
    @PostMapping("/teacher/update-consultations")
    @ResponseBody
    public ResponseEntity<?> updateConsultations(Authentication authentication) {
        Teacher teacher = teacherRepository.findByUser_Login(authentication.getName());
        consultationService.updateConsultationsForTeacher(teacher);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/teacher/consultation/delete-one-time/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteOneTimeConsultation(
            @PathVariable("id") Integer consultationId,
            Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        try {
            Teacher teacher = teacherRepository.findByUser_Login(authentication.getName());

            // Проверяем, что консультация существует и принадлежит преподавателю
            Consultation consultation = consultationRepository.findById(consultationId)
                    .orElseThrow(() -> new RuntimeException("Консультация не найдена"));

            if (!consultation.getTeacher().getId().equals(teacher.getId())) {
                response.put("error", "У вас нет прав для удаления этой консультации");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Проверяем, что это разовая консультация (без шаблона)
            if (consultation.getTemplate() != null) {
                response.put("error", "Это не разовая консультация");
                return ResponseEntity.badRequest().body(response);
            }

            // Удаляем связанные записи студентов
            List<ConsultationSignup> signups = consultationSignupRepository.findByConsultationId(consultationId);
            for (ConsultationSignup signup : signups) {
                // Создаем уведомления для студентов
                Notification notification = new Notification();
                notification.setUser(signup.getStudent().getUser());
                notification.setMessage(String.format(
                        "Разовая консультация с преподавателем %s %s %s на %s отменена",
                        teacher.getLastName(),
                        teacher.getFirstName(),
                        teacher.getPatronymic(),
                        consultation.getConsultationDate()
                ));
                notification.setRelatedEntityType("CONSULTATION");
                notification.setRelatedEntityId(consultation.getId());
                notificationRepository.save(notification);
            }

            // Удаляем саму консультацию
            consultationRepository.delete(consultation);

            response.put("success", "Разовая консультация успешно удалена");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Ошибка при удалении консультации: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/teacher/consultation/delete-consultation/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteConsultation(
            @PathVariable("id") Integer consultationId,
            Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            Teacher teacher = teacherRepository.findByUser_Login(authentication.getName());
            Consultation consultation = consultationRepository.findById(consultationId)
                    .orElseThrow(() -> new RuntimeException("Консультация не найдена"));

            if (!consultation.getTeacher().getId().equals(teacher.getId())) {
                response.put("error", "У вас нет прав для удаления этой консультации");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            ConsultationTemplate template = consultation.getTemplate();
            boolean isLastConsultation = false;

            // Проверяем, является ли это последней консультацией для шаблона
            if (template != null) {
                List<Consultation> templateConsultations = consultationRepository.findByTemplateId(template.getId())
                        .stream()
                        .filter(c -> !c.getId().equals(consultationId)) // Исключаем текущую консультацию
                        .collect(Collectors.toList());

                isLastConsultation = templateConsultations.isEmpty();

                // Удаляем связанные записи студентов
                List<ConsultationSignup> signups = consultationSignupRepository.findByConsultationId(consultationId);
                for (ConsultationSignup signup : signups) {
                    // Создаем уведомления для студентов
                    Notification notification = new Notification();
                    notification.setUser(signup.getStudent().getUser());
                    notification.setMessage(String.format(
                            "Консультация с преподавателем %s %s %s на %s отменена",
                            teacher.getLastName(),
                            teacher.getFirstName(),
                            teacher.getPatronymic(),
                            consultation.getConsultationDate()
                    ));
                    notification.setRelatedEntityType("CONSULTATION");
                    notification.setRelatedEntityId(consultation.getId());
                    notificationRepository.save(notification);
                }

                // Удаляем саму консультацию
                consultationRepository.delete(consultation);

                // Если это была последняя консультация, создаем новую на следующей неделе
                if (isLastConsultation) {
                    LocalDate nextDate = consultation.getConsultationDate().plusWeeks(1);

                    Consultation newConsultation = new Consultation();
                    newConsultation.setTemplate(template);
                    newConsultation.setTeacher(teacher);
                    newConsultation.setConsultationDate(nextDate);
                    newConsultation.setStartTime(consultation.getStartTime());
                    newConsultation.setEndTime(consultation.getEndTime());
                    newConsultation.setRoom(consultation.getRoom());
                    newConsultation.setMaxStudents(consultation.getMaxStudents());
                    consultationRepository.save(newConsultation);

                    response.put("reloadNeeded", true);
                }
            } else {
                // Для разовых консультаций просто удаляем
                List<ConsultationSignup> signups = consultationSignupRepository.findByConsultationId(consultationId);
                for (ConsultationSignup signup : signups) {
                    Notification notification = new Notification();
                    notification.setUser(signup.getStudent().getUser());
                    notification.setMessage(String.format(
                            "Разовая консультация с преподавателем %s %s %s на %s отменена",
                            teacher.getLastName(),
                            teacher.getFirstName(),
                            teacher.getPatronymic(),
                            consultation.getConsultationDate()
                    ));
                    notification.setRelatedEntityType("CONSULTATION");
                    notification.setRelatedEntityId(consultation.getId());
                    notificationRepository.save(notification);
                }
                consultationRepository.delete(consultation);
            }

            response.put("success", "Консультация успешно удалена");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Ошибка при удалении консультации: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/teacher/signup/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteStudentSignup(
            @PathVariable("id") Integer signupId,
            @RequestBody(required = false) Map<String, String> requestBody,
            Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        try {
            Teacher teacher = teacherRepository.findByUser_Login(authentication.getName());
            String cancelReason = requestBody != null ? requestBody.get("reason") : null;

            ConsultationSignup signup = consultationSignupRepository.findById(signupId)
                    .orElseThrow(() -> new RuntimeException("Запись не найдена"));

            // Проверяем, что консультация принадлежит текущему преподавателю
            if (!signup.getConsultation().getTeacher().getId().equals(teacher.getId())) {
                response.put("error", "У вас нет прав для удаления этой записи");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Создаем уведомление для студента
            Notification notification = new Notification();
            notification.setUser(signup.getStudent().getUser());

            String message = String.format(
                    "Ваша запись на консультацию с преподавателем %s %s %s на %s была отменена преподавателем",
                    teacher.getLastName(),
                    teacher.getFirstName(),
                    teacher.getPatronymic(),
                    signup.getConsultation().getConsultationDate()
            );

            if (cancelReason != null && !cancelReason.isEmpty()) {
                message += String.format(". Причина: %s", cancelReason);
            }

            notification.setMessage(message);
            notification.setRelatedEntityType("CONSULTATION");
            notification.setRelatedEntityId(signup.getConsultation().getId());
            notificationRepository.save(notification);

            // Удаляем запись
            consultationSignupRepository.delete(signup);

            response.put("success", "Запись студента успешно удалена");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Ошибка при удалении записи: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}