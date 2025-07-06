package com.banquito.originacion.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IdentificadorVehiculoResponseDTO {
    private String id;
    private String placa;
    private String chasis;
    private String motor;
} 