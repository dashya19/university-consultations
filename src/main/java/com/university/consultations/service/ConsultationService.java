package com.university.consultations.service;

import com.university.consultations.entity.*;
import com.university.consultations.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
public class ConsultationService {
    private final ConsultationRepository consultationRepository;
    private final ConsultationTemplateRepository templateRepository;
    private final ConsultationArchiveRepository consultationArchiveRepository;
    private final ConsultationSignupRepository consultationSignupRepository;
    private final NotificationRepository notificationRepository;

    public ConsultationService(ConsultationRepository consultationRepository,
                               ConsultationTemplateRepository templateRepository,
                               ConsultationArchiveRepository consultationArchiveRepository,
                               ConsultationSignupRepository consultationSignupRepository,
                               NotificationRepository notificationRepository) {
        this.consultationRepository = consultationRepository;
        this.templateRepository = templateRepository;
        this.consultationArchiveRepository = consultationArchiveRepository;
        this.consultationSignupRepository = consultationSignupRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void updateConsultationsForTeacher(Teacher teacher) {
        LocalDateTime now = LocalDateTime.now();

        // Проверяем и удаляем шаблоны с истекшим сроком действия
        List<ConsultationTemplate> expiredTemplates = templateRepository.findByTeacherId(teacher.getId())
                .stream()
                .filter(t -> t.getExpirationDateTime() != null && t.getExpirationDateTime().isBefore(now))
                .toList();

        expiredTemplates.forEach(this::deleteTemplateWithNotifications);

        // Удаляем прошедшие консультации
        List<Consultation> oldConsultations = consultationRepository.findAll()
                .stream()
                .filter(c -> LocalDateTime.of(c.getConsultationDate(), c.getEndTime()).isBefore(now))
                .filter(c -> c.getTeacher().getId().equals(teacher.getId()))
                .toList();

        oldConsultations.forEach(consultation -> {
            List<ConsultationArchive> archives = consultationArchiveRepository.findByConsultationId(consultation.getId());
            archives.forEach(archive -> {
                archive.setConsultation(null);
                consultationArchiveRepository.save(archive);
            });
        });

        consultationRepository.deleteAll(oldConsultations);

        // Получаем активные шаблоны преподавателя
        List<ConsultationTemplate> templates = templateRepository.findByTeacherId(teacher.getId());

        for (ConsultationTemplate template : templates) {
            // Получаем все активные консультации по этому шаблону
            List<Consultation> existingConsultations = consultationRepository.findByTemplateId(template.getId())
                    .stream()
                    .filter(t -> !t.getTemplate().getIsOneTime())
                    .toList();

            // Проверяем, есть ли уже будущие консультации по этому шаблону
            boolean hasFutureConsultations = existingConsultations.stream()
                    .anyMatch(c -> LocalDateTime.of(c.getConsultationDate(), c.getEndTime()).isAfter(now));

            // Если нет будущих консультаций, создаем новую
            if (!hasFutureConsultations) {
                LocalDate nextDate = getNextAvailableDate(template, now);

                // For bi-weekly consultations, skip a week if needed
                if (template.getIsBiWeekly()) {
                    LocalDate lastConsultationDate = existingConsultations.stream()
                            .map(Consultation::getConsultationDate)
                            .max(LocalDate::compareTo)
                            .orElse(null);

                    if (lastConsultationDate != null && !nextDate.isAfter(lastConsultationDate.plusWeeks(1))) {
                        nextDate = lastConsultationDate.plusWeeks(2);
                    }
                }

                Consultation newConsultation = new Consultation();
                newConsultation.setTemplate(template);
                newConsultation.setTeacher(teacher);
                newConsultation.setConsultationDate(nextDate);
                newConsultation.setStartTime(template.getStartTime());
                newConsultation.setEndTime(template.getEndTime());
                newConsultation.setRoom(template.getRoom());
                newConsultation.setMaxStudents(template.getMaxStudents());

                consultationRepository.save(newConsultation);
            }

            // Для консультаций с ограничением по студентам проверяем заполненность
            for (Consultation consultation : existingConsultations) {
                if (consultation.getMaxStudents() != null) {
                    long signupsCount = consultationSignupRepository.countByConsultationId(consultation.getId());

                    // Если консультация заполнена и еще не прошла по времени
                    if (signupsCount >= consultation.getMaxStudents() &&
                            LocalDateTime.of(consultation.getConsultationDate(), consultation.getEndTime()).isAfter(now)) {

                        // Проверяем, есть ли уже следующая консультация
                        LocalDate nextDate = consultation.getConsultationDate().plusWeeks(template.getIsBiWeekly() ? 2 : 1);
                        boolean nextConsultationExists = consultationRepository.existsByTeacherIdAndConsultationDateAndStartTime(
                                teacher.getId(),
                                nextDate,
                                consultation.getStartTime()
                        );

                        if (!nextConsultationExists) {
                            Consultation newConsultation = new Consultation();
                            newConsultation.setTemplate(template);
                            newConsultation.setTeacher(teacher);
                            newConsultation.setConsultationDate(nextDate);
                            newConsultation.setStartTime(consultation.getStartTime());
                            newConsultation.setEndTime(consultation.getEndTime());
                            newConsultation.setRoom(consultation.getRoom());
                            newConsultation.setMaxStudents(consultation.getMaxStudents());

                            consultationRepository.save(newConsultation);
                        }
                    }
                }
            }
        }
    }

    private LocalDate getNextAvailableDate(ConsultationTemplate template, LocalDateTime now) {
        LocalDate today = LocalDate.now();
        DayOfWeek templateDay = template.getDayOfWeek();

        // Start from today or next week based on startNextWeek flag
        LocalDate nextDate = template.getStartNextWeek() ? today.plusWeeks(1) : today;

        // Find the next matching day of week
        while (nextDate.getDayOfWeek() != templateDay) {
            nextDate = nextDate.plusDays(1);
        }

        // If we found today's date but time has passed, move to next occurrence
        if (nextDate.isEqual(today) && template.getEndTime().isBefore(now.toLocalTime())) {
            nextDate = nextDate.plusWeeks(template.getIsBiWeekly() ? 2 : 1);
        }

        return nextDate;
    }

    private void deleteTemplateWithNotifications(ConsultationTemplate template) {
        // Получаем все консультации по этому шаблону
        List<Consultation> consultations = consultationRepository.findByTemplateId(template.getId());

        // Отправляем уведомления студентам
        consultations.forEach(consultation -> {
            List<ConsultationSignup> signups = consultationSignupRepository.findByConsultationId(consultation.getId());
            signups.forEach(signup -> {
                Notification notification = new Notification();
                notification.setUser(signup.getStudent().getUser());
                notification.setMessage(String.format(
                        "Шаблон консультации с преподавателем %s %s %s был автоматически удален по истечении срока действия",
                        template.getTeacher().getLastName(),
                        template.getTeacher().getFirstName(),
                        template.getTeacher().getPatronymic()
                ));
                notificationRepository.save(notification);
            });
        });

        // Отправляем уведомление преподавателю
        Notification teacherNotification = new Notification();
        teacherNotification.setUser(template.getTeacher().getUser());
        teacherNotification.setMessage(String.format(
                "Ваш шаблон консультации (%s, %s-%s) был автоматически удален по истечении установленного срока",
                template.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("ru")),
                template.getStartTime(),
                template.getEndTime()
        ));
        notificationRepository.save(teacherNotification);

        // Удаляем шаблон (каскадно удалятся консультации и записи)
        templateRepository.delete(template);
    }
}