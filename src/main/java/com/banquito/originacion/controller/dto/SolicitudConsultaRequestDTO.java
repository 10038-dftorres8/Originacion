package com.banquito.originacion.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SolicitudConsultaRequestDTO {
    @NotNull(message = "La fecha de inicio es requerida")
    private LocalDateTime fechaInicio;
    
    @NotNull(message = "La fecha de fin es requerida")
    private LocalDateTime fechaFin;
    
    private String estado;
} 