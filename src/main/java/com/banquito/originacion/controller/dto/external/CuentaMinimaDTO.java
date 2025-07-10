package com.banquito.originacion.controller.dto.external;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaMinimaDTO {
    private Integer id;
    private String codigoCuenta;
    private String nombre;
} 