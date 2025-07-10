package com.banquito.originacion.controller.dto.external;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaExternalDTO {
    private Integer id;
    private String codigoCuenta;
    private String nombre;
    private String descripcion;
    private String estado;
    private Instant fechaCreacion;
    private Instant fechaModificacion;
    private Long version;
} 