package com.banquito.originacion.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaCoreResponseDTO {
    private String id;
    private String tipoIdentificacion;
    private String numeroIdentificacion;
    private String nombre;
    private String genero;
    private LocalDate fechaNacimiento;
    private String estadoCivil;
    private String nivelEstudio;
    private String correoElectronico;
    private String estado;
} 