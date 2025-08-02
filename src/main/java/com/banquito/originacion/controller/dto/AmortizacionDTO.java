package com.banquito.originacion.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AmortizacionDTO {
    private Integer numeroCuota;
    private BigDecimal saldoInicial;
    private BigDecimal cuota;
    private BigDecimal abonoCapital;
    private BigDecimal interes;
    private BigDecimal saldoFinal;
} 