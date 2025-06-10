package com.university.consultations.controller;

import com.university.consultations.entity.ConsultationArchive;
import com.university.consultations.entity.Management;
import com.university.consultations.entity.Student;
import com.university.consultations.entity.Teacher;
import com.university.consultations.repository.ConsultationArchiveRepository;
import com.university.consultations.repository.ManagementRepository;
import com.university.consultations.repository.TeacherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ManagementController {
    private final ManagementRepository managementRepository;
    private final ConsultationArchiveRepository consultationArchiveRepository;

    public ManagementController(ManagementRepository managementRepository,
                                ConsultationArchiveRepository consultationArchiveRepository) {
        this.managementRepository = managementRepository;
        this.consultationArchiveRepository = consultationArchiveRepository;
    }
    @GetMapping("/management/dashboard")
    public String showManagementDashboard(Authentication authentication, Model model) {
        Management management = managementRepository.findByUser_Login(authentication.getName());

        model.addAttribute("username", authentication.getName());
        model.addAttribute("management", management);
        model.addAttribute("hasPhoto", management.getPhotoPath() != null && !management.getPhotoPath().isEmpty());

        return "management";
    }
    @PostMapping("/management/updateFields")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateFields(
            @RequestParam(name = "fieldType") String fieldType,
            @RequestParam(name = "fieldInput") String fieldInput,
            Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        try {
            Management management = managementRepository.findByUser_Login(authentication.getName());
            if (fieldType.equals("email")) {
                if (!fieldInput.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$") && !fieldInput.isEmpty()) {
                    response.put("error", "Введите корректный email адрес");
                    return ResponseEntity.badRequest().body(response);
                }
                management.setEmail(fieldInput.isEmpty() ? null : fieldInput);
            } else if (fieldType.equals("phone")) {
                String cleanedPhone = fieldInput.replaceAll("\\D", "");
                if (!cleanedPhone.isEmpty() && !cleanedPhone.startsWith("8")) {
                    cleanedPhone = "8" + cleanedPhone;
                }
                if (!cleanedPhone.isEmpty() && cleanedPhone.length() != 11) {
                    response.put("error", "Номер телефона должен содержать 11 цифр");
                    return ResponseEntity.badRequest().body(response);
                }
                management.setPhone(cleanedPhone.isEmpty() ? null : cleanedPhone);
            }
            managementRepository.save(management);
            response.put("success", "Данные успешно обновлены");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Ошибка при обновлении данных");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/api/students/{id}/archive")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getStudentArchive(@PathVariable("id") Integer studentId) {
        List<ConsultationArchive> archive = consultationArchiveRepository.findFullArchiveByStudentId(studentId);

        List<Map<String, Object>> response = archive.stream().map(record -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("consultationDate", record.getConsultationDate());
            map.put("startTime", record.getStartTime().toString());
            map.put("endTime", record.getEndTime().toString());
            map.put("room", record.getRoom());
            map.put("reason", record.getReason());
            map.put("attended", record.getAttended());
            map.put("feedback", record.getFeedback());
            map.put("debtStatus", record.getDebtStatus());

            // Сначала проверяем прямое поле teacher
            if (record.getTeacher() != null) {
                map.put("teacherLastName", record.getTeacher().getLastName());
                map.put("teacherFirstName", record.getTeacher().getFirstName());
                map.put("teacherPatronymic", record.getTeacher().getPatronymic());
            }
            // Затем проверяем teacher через consultation
            else if (record.getConsultation() != null && record.getConsultation().getTeacher() != null) {
                Teacher teacher = record.getConsultation().getTeacher();
                map.put("teacherLastName", teacher.getLastName());
                map.put("teacherFirstName", teacher.getFirstName());
                map.put("teacherPatronymic", teacher.getPatronymic());
            }
            // Если ничего не найдено
            else {
                map.put("teacherLastName", "Неизвестно");
                map.put("teacherFirstName", "");
                map.put("teacherPatronymic", "");
            }

            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
    @GetMapping("/api/teachers/{id}/archive")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getTeacherArchive(@PathVariable("id") Integer teacherId) {
        List<ConsultationArchive> archive = consultationArchiveRepository.findFullArchiveByTeacherId(teacherId);

        List<Map<String, Object>> response = archive.stream().map(record -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("consultationDate", record.getConsultationDate());
            map.put("startTime", record.getStartTime().toString());
            map.put("endTime", record.getEndTime().toString());
            map.put("room", record.getRoom());
            map.put("reason", record.getReason());
            map.put("attended", record.getAttended());
            map.put("feedback", record.getFeedback());
            map.put("debtStatus", record.getDebtStatus());
            // Информация о студенте
            if (record.getStudent() != null) {
                map.put("studentLastName", record.getStudent().getLastName());
                map.put("studentFirstName", record.getStudent().getFirstName());
                map.put("studentPatronymic", record.getStudent().getPatronymic());
                map.put("studentGroupName", record.getStudent().getGroupName());
            }

            // Добавляем информацию о преподавателе (если есть в самой записи)
            if (record.getTeacher() != null) {
                map.put("teacherLastName", record.getTeacher().getLastName());
                map.put("teacherFirstName", record.getTeacher().getFirstName());
                map.put("teacherPatronymic", record.getTeacher().getPatronymic());
            }
            // Или из связанной консультации
            else if (record.getConsultation() != null && record.getConsultation().getTeacher() != null) {
                Teacher teacher = record.getConsultation().getTeacher();
                map.put("teacherLastName", teacher.getLastName());
                map.put("teacherFirstName", teacher.getFirstName());
                map.put("teacherPatronymic", teacher.getPatronymic());
            }

            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
    @GetMapping("/api/students/{id}/attendance-stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStudentAttendanceStats(@PathVariable("id") Integer studentId) {
        Map<String, Long> stats = consultationArchiveRepository.findAttendanceStatisticsByStudentId(studentId);

        long total = stats.get("total") != null ? stats.get("total") : 0;
        long attended = stats.get("attendedCount") != null ? stats.get("attendedCount") : 0;
        long notAttended = stats.get("notAttendedCount") != null ? stats.get("notAttendedCount") : 0;

        double attendancePercentage = total > 0 ? (double) attended / total * 100 : 0;

        Map<String, Object> response = new HashMap<>();
        response.put("total", total);
        response.put("attended", attended);
        response.put("notAttended", notAttended);
        response.put("attendancePercentage", Math.round(attendancePercentage * 100.0) / 100.0);

        return ResponseEntity.ok(response);
    }
    // ManagementController.java
    @GetMapping("/api/teachers/{id}/attendance-stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTeacherAttendanceStats(@PathVariable("id") Integer teacherId) {
        Map<String, Long> stats = consultationArchiveRepository.findAttendanceStatisticsByTeacherId(teacherId);

        long total = stats.get("total") != null ? stats.get("total") : 0;
        long attended = stats.get("attendedCount") != null ? stats.get("attendedCount") : 0;
        long notAttended = stats.get("notAttendedCount") != null ? stats.get("notAttendedCount") : 0;

        double attendancePercentage = total > 0 ? (double) attended / total * 100 : 0;

        Map<String, Object> response = new HashMap<>();
        response.put("total", total);
        response.put("attended", attended);
        response.put("notAttended", notAttended);
        response.put("attendancePercentage", Math.round(attendancePercentage * 100.0) / 100.0);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/api/students/{id}/debt-stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStudentDebtStats(@PathVariable("id") Integer studentId) {
        Map<String, Long> stats = consultationArchiveRepository.findDebtStatisticsByStudentId(studentId);

        long total = stats.get("total") != null ? stats.get("total") : 0;
        long passed = stats.get("passedCount") != null ? stats.get("passedCount") : 0;
        long failed = stats.get("failedCount") != null ? stats.get("failedCount") : 0;

        double passedPercentage = total > 0 ? (double) passed / total * 100 : 0;

        Map<String, Object> response = new HashMap<>();
        response.put("total", total);
        response.put("passed", passed);
        response.put("failed", failed);
        response.put("passedPercentage", Math.round(passedPercentage * 100.0) / 100.0);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/teachers/{id}/debt-stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTeacherDebtStats(@PathVariable("id") Integer teacherId) {
        Map<String, Long> stats = consultationArchiveRepository.findDebtStatisticsByTeacherId(teacherId);

        long total = stats.get("total") != null ? stats.get("total") : 0;
        long passed = stats.get("passedCount") != null ? stats.get("passedCount") : 0;
        long failed = stats.get("failedCount") != null ? stats.get("failedCount") : 0;

        double passedPercentage = total > 0 ? (double) passed / total * 100 : 0;

        Map<String, Object> response = new HashMap<>();
        response.put("total", total);
        response.put("passed", passed);
        response.put("failed", failed);
        response.put("passedPercentage", Math.round(passedPercentage * 100.0) / 100.0);

        return ResponseEntity.ok(response);
    }
}