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
public class CuentaClienteExternalDTO {
    private Integer id;
    private CuentaMinimaDTO idCuenta;
    private String idCliente;
    private String numeroCuenta;
    private BigDecimal saldoDisponible;
    private BigDecimal saldoContable;
    private String fechaApertura;
    private String estado;
    private Long version;
} 