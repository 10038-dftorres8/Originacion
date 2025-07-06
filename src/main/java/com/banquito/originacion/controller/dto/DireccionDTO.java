package com.banquito.originacion.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class DireccionDTO {
    private String tipo;
    private String linea1;
    private String linea2;
    private String codigoPostal;
    private String codigoGeografico;
    private String codigoProvincia;
    private String codigoCanton;
    private LocalDate fechaCreacion;
    private LocalDate fechaActualizacion;
    private String estado;
} 