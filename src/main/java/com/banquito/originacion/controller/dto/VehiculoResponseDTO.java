package com.banquito.originacion.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class VehiculoResponseDTO {
    private String id;
    private String marca;
    private String modelo;
    private double cilindraje;
    private String anio;
    private BigDecimal valor;
    private String color;
    private String extras;
    private String estado;
    private String tipo;
    private String combustible;
    private String condicion;
    private Long version;
    private IdentificadorVehiculoResponseDTO identificadorVehiculo;
} 