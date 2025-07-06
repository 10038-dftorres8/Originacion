package com.banquito.originacion.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VendedorResponseDTO {
    private String id;
    private String nombre;
    private String telefono;
    private String email;
    private String estado;
    private Long version;
    private String cedula;
} 