package com.banquito.originacion.controller.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentoAdjuntoResponseDTO {
    private Long id;
    private Long idSolicitud;
    private Long idDocumentoRequerido;
    private String nombreArchivo;
    private String rutaStorage;
    private LocalDateTime fechaCarga;
    private Long version;
} 