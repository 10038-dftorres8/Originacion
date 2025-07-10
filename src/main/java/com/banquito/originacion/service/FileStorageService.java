package com.banquito.originacion.service;

import com.banquito.originacion.exception.CreateEntityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class FileStorageService {

    @Value("${app.file.storage.path:./uploads}")
    private String storagePath;

    private final Path fileStorageLocation;

    public FileStorageService() {
        this.fileStorageLocation = Paths.get("./uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo crear el directorio de almacenamiento", ex);
        }
    }

    public String storeFile(MultipartFile file, Long idSolicitud, String tipoDocumento) {
        try {
            if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                throw new CreateEntityException("FileStorage", "Solo se permiten archivos PDF");
            }

            if (file.getSize() > 20 * 1024 * 1024) {
                throw new CreateEntityException("FileStorage", "El archivo no debe superar los 20MB");
            }

            Path solicitudDir = this.fileStorageLocation.resolve("solicitud_" + idSolicitud);
            Files.createDirectories(solicitudDir);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = tipoDocumento + "_" + timestamp + ".pdf";
            
            Path targetLocation = solicitudDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("Archivo guardado exitosamente: {}", targetLocation.toString());
            return targetLocation.toString();

        } catch (IOException ex) {
            throw new CreateEntityException("FileStorage", "No se pudo almacenar el archivo: " + ex.getMessage());
        }
    }

    public Resource loadFileAsResource(String filePath) {
        try {
            Path file = Paths.get(filePath);
            Resource resource = new UrlResource(file.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new CreateEntityException("FileStorage", "Archivo no encontrado: " + filePath);
            }
        } catch (MalformedURLException ex) {
            throw new CreateEntityException("FileStorage", "Archivo no encontrado: " + filePath);
        }
    }

    public void deleteFile(String filePath) {
        try {
            Path file = Paths.get(filePath);
            Files.deleteIfExists(file);
            log.info("Archivo eliminado: {}", filePath);
        } catch (IOException ex) {
            log.error("Error al eliminar archivo: {}", filePath, ex);
        }
    }
} 