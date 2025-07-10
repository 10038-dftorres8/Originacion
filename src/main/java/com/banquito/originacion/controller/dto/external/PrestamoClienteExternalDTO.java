package com.banquito.originacion.controller.dto.external;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrestamoClienteExternalDTO {
    private Integer id;
    private String idCliente;
    private String idPrestamo;
    private BigDecimal montoSolicitado;
    private Integer plazoMeses;
    private BigDecimal tasaInteresAplicada;
    private String estado;
} 