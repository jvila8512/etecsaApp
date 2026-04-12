package com.etecsa.web.rest;

import com.etecsa.web.rest.vm.UploadResponse;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Generic file upload controller.
 * Supports uploading files to configurable folders.
 */
@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private static final Logger LOG = LoggerFactory.getLogger(UploadController.class);

    // Allowed content types by folder
    private static final Map<String, String[]> ALLOWED_TYPES_MAP = Map.of(
        "profile",
        new String[] { "image/jpeg", "image/png", "image/gif", "image/webp" },
        "alarmas",
        new String[] { "image/jpeg", "image/png", "image/gif", "image/webp" },
        "equipos",
        new String[] { "image/jpeg", "image/png", "image/gif", "image/webp" },
        "eventos",
        new String[] { "image/jpeg", "image/png", "image/gif", "image/webp" },
        "documentos",
        new String[] { "application/pdf", "image/jpeg", "image/png" }
    );

    // Max file size in bytes by folder (default 5MB)
    private static final Map<String, Long> MAX_SIZE_MAP = Map.of(
        "profile",
        500_000L, // 500KB for profile photos
        "alarmas",
        2_000_000L, // 2MB for alarm captures
        "equipos",
        5_000_000L, // 5MB for equipment images
        "eventos",
        5_000_000L, // 5MB for event attachments
        "documentos",
        10_000_000L // 10MB for documents
    );

    @Value("${app.upload.root:uploads}")
    private String uploadRoot;

    /**
     * Upload a file to a specific folder.
     *
     * @param file   the file to upload
     * @param folder the target folder (profile, alarmas, equipos, eventos, documentos)
     * @return the file path
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("folder") String folder) {
        LOG.debug("REST request to upload file to folder: {}", folder);

        // Validate folder
        if (!ALLOWED_TYPES_MAP.containsKey(folder)) {
            return ResponseEntity.badRequest()
                .body(new UploadResponse(null, "Carpeta no válida: " + folder + ". Usar: " + ALLOWED_TYPES_MAP.keySet()));
        }

        // Validate file is not empty
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new UploadResponse(null, "El archivo está vacío"));
        }

        // Validate file size
        long maxSize = MAX_SIZE_MAP.getOrDefault(folder, 5_000_000L);
        if (file.getSize() > maxSize) {
            return ResponseEntity.badRequest()
                .body(new UploadResponse(null, "Archivo muy grande. Máximo: " + (maxSize / 1_000_000) + "MB"));
        }

        // Validate content type
        String contentType = file.getContentType();
        String[] allowedTypes = ALLOWED_TYPES_MAP.get(folder);
        boolean isValidType = false;
        for (String type : allowedTypes) {
            if (type.equals(contentType)) {
                isValidType = true;
                break;
            }
        }

        if (!isValidType) {
            return ResponseEntity.badRequest()
                .body(new UploadResponse(null, "Tipo de archivo no válido. Permitidos: " + String.join(", ", allowedTypes)));
        }

        try {
            // Create folder if not exists
            Path folderPath = Paths.get(uploadRoot, folder);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            // Generate unique filename: folder + timestamp + random
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // Use folder name + timestamp + random for uniqueness
            String randomPart = UUID.randomUUID().toString().substring(0, 6);
            String newFilename = folder + "_" + System.currentTimeMillis() + "_" + randomPart + extension;

            // Save file
            Path filePath = folderPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/api/uploads/" + folder + "/" + newFilename;

            LOG.info("File uploaded successfully: {}", fileUrl);
            return ResponseEntity.ok(new UploadResponse(fileUrl, null));
        } catch (IOException e) {
            LOG.error("Error uploading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new UploadResponse(null, "Error al guardar el archivo: " + e.getMessage())
            );
        }
    }

    /**
     * Download a file.
     *
     * @param folder   the folder name
     * @param filename the filename
     * @return the file
     */
    @GetMapping("/{folder}/{filename}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String folder, @PathVariable String filename) {
        Path filePath = Paths.get(uploadRoot, folder, filename);

        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] fileContent = Files.readAllBytes(filePath);

            // Determine content type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok().headers(headers).body(fileContent);
        } catch (IOException e) {
            LOG.error("Error reading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a file.
     *
     * @param folder   the folder name
     * @param filename the filename
     * @return success message
     */
    @DeleteMapping("/{folder}/{filename}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable String folder, @PathVariable String filename) {
        Path filePath = Paths.get(uploadRoot, folder, filename);

        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            Files.delete(filePath);
            return ResponseEntity.ok(Map.of("message", "Archivo eliminado: " + filename));
        } catch (IOException e) {
            LOG.error("Error deleting file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error al eliminar: " + e.getMessage()));
        }
    }

    /**
     * Get upload configuration.
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        return ResponseEntity.ok(Map.of("folders", ALLOWED_TYPES_MAP.keySet(), "maxSizePerFolder", MAX_SIZE_MAP, "root", uploadRoot));
    }
}
