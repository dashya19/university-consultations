package com.university.consultations.controller;

import com.university.consultations.entity.Student;
import com.university.consultations.entity.Teacher;
import com.university.consultations.entity.Management;
import com.university.consultations.repository.StudentRepository;
import com.university.consultations.repository.TeacherRepository;
import com.university.consultations.repository.ManagementRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class FileUploadController {
    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    @Value("${upload.path}")
    private String uploadPath;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ManagementRepository managementRepository;
    public FileUploadController(StudentRepository studentRepository,
                                TeacherRepository teacherRepository,
                                ManagementRepository managementRepository) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.managementRepository = managementRepository;
    }
    @PostMapping("/uploadPhoto")
    public String uploadPhoto(@RequestParam("file") MultipartFile file,
                              @RequestParam("role") String role,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            redirectAttributes.addFlashAttribute("error", "Допустимы только JPG, JPEG и PNG файлы");
            return getRedirectUrl(role);
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null ||
                !(originalFilename.toLowerCase().endsWith(".jpg") ||
                        originalFilename.toLowerCase().endsWith(".jpeg") ||
                        originalFilename.toLowerCase().endsWith(".png"))) {
            redirectAttributes.addFlashAttribute("error", "Недопустимое расширение файла");
            return getRedirectUrl(role);
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            redirectAttributes.addFlashAttribute("error", "Файл слишком большой (макс. 2MB)");
            return getRedirectUrl(role);
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID() + extension;
        Path newFilePath = Paths.get(uploadPath, filename);
        Files.createDirectories(newFilePath.getParent());
        Files.write(newFilePath, file.getBytes());
        switch (role.toLowerCase()) {
            case "student":
                Student student = studentRepository.findByUser_Login(authentication.getName());
                deleteOldFileIfExists(student.getPhotoPath());
                student.setPhotoPath(filename);
                studentRepository.save(student);
                break;
            case "teacher":
                Teacher teacher = teacherRepository.findByUser_Login(authentication.getName());
                deleteOldFileIfExists(teacher.getPhotoPath());
                teacher.setPhotoPath(filename);
                teacherRepository.save(teacher);
                break;
            case "management":
                Management management = managementRepository.findByUser_Login(authentication.getName());
                deleteOldFileIfExists(management.getPhotoPath());
                management.setPhotoPath(filename);
                managementRepository.save(management);
                break;
            default:
                redirectAttributes.addFlashAttribute("error", "Неизвестная роль пользователя");
                return getRedirectUrl(role);
        }
        redirectAttributes.addFlashAttribute("success", "Фото успешно обновлено!");
        return getRedirectUrl(role);
    }
    private void deleteOldFileIfExists(String oldFilePath) {
        if (oldFilePath != null && !oldFilePath.isEmpty()) {
            Path path = Paths.get(uploadPath, oldFilePath);
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                logger.error("Не удалось удалить старый файл: " + path, e);
            }
        }
    }
    private String getRedirectUrl(String role) {
        return "redirect:/" + role.toLowerCase() + "/dashboard";
    }
}