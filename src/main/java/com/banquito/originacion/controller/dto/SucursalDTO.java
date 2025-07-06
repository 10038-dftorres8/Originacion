package com.banquito.originacion.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class SucursalDTO {
    private String codigoSucursal;
    private String estado;
    private LocalDate fechaCreacion;
    private LocalDate fechaUltimaActualizacion;
} 