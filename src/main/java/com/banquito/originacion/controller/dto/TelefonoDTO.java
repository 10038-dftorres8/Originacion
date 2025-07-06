package com.banquito.originacion.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class TelefonoDTO {
    private String tipo;
    private String numero;
    private LocalDate fechaCreacion;
    private LocalDate fechaActualizacion;
    private String estado;
} 