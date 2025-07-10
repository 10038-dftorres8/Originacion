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
    private String escenario;
    
    public AmortizacionDTO(Integer numeroCuota, BigDecimal saldoInicial, BigDecimal cuota,
                          BigDecimal abonoCapital, BigDecimal interes, BigDecimal saldoFinal) {
        this.numeroCuota = numeroCuota;
        this.saldoInicial = saldoInicial;
        this.cuota = cuota;
        this.abonoCapital = abonoCapital;
        this.interes = interes;
        this.saldoFinal = saldoFinal;
        this.escenario = "Escenario por defecto";
    }
} 