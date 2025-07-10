package com.banquito.originacion.controller.dto.external;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PrestamosExternalDTO {
    private String id;
    private String idTipoPrestamo;
    private String idMoneda;
    private String nombre;
    private String descripcion;
    private LocalDateTime fechaModificacion;
    private String baseCalculo;
    private BigDecimal tasaInteres;
    private BigDecimal montoMinimo;
    private BigDecimal montoMaximo;
    private Integer plazoMinimoMeses;
    private Integer plazoMaximoMeses;
    private String tipoAmortizacion;
    private String idSeguro;
    private String idTipoComision;
    private String estado;
    private Long version;
} 