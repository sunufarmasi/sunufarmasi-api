package sn.sunufarmasi.shared.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/**
 * Controller pour l'upload de fichiers (pièces jointes tickets, etc.)
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/uploads")
@Slf4j
@CrossOrigin(origins = "*")
public class FileUploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Upload d'un fichier joint pour un ticket
     * POST /api/v1/uploads/ticket
     */
    @PostMapping("/ticket")
    @PreAuthorize("hasRole('SYNDICAT')")
    public ResponseEntity<Map<String, String>> uploadTicketFile(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Fichier vide"));
        }

        // Limiter la taille (5 MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("error", "Fichier trop volumineux (max 5 MB)"));
        }

        // Créer le répertoire si nécessaire
        Path uploadPath = Paths.get(uploadDir, "tickets");
        Files.createDirectories(uploadPath);

        // Nom unique pour éviter les collisions
        String originalName = file.getOriginalFilename();
        String extension = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : "";
        String uniqueName = UUID.randomUUID() + extension;

        Path filePath = uploadPath.resolve(uniqueName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String fileUrl = baseUrl + "/uploads/tickets/" + uniqueName;
        log.info("Fichier uploadé: {} → {}", originalName, fileUrl);

        return ResponseEntity.ok(Map.of(
                "nom", originalName != null ? originalName : uniqueName,
                "url", fileUrl,
                "taille", String.valueOf(file.getSize())
        ));
    }
}
