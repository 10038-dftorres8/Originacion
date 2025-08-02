package com.banquito.originacion.controller.dto.external;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConcesionarioResponseDTO {
    private String id;
    private String razonSocial;
    private String direccion;
    private String telefono;
    private String emailContacto;
    private String estado;
    private Integer version;
    private String ruc;
} 