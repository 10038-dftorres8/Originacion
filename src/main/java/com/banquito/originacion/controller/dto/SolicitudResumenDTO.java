package com.banquito.originacion.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudResumenDTO {
    private Long id_solicitud;
    private BigDecimal precio_final_vehiculo;
    private BigDecimal monto_aprobado;
    private Integer plazo_final_meses;
    private BigDecimal tasa_efectiva_anual;
} 