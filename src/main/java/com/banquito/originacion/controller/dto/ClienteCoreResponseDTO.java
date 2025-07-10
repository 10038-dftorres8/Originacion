package com.banquito.originacion.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteCoreResponseDTO {
    private String id;
    private String tipoEntidad;
    private String idEntidad;
    private String nombre;
    private BigDecimal scoreInterno;
    private String nacionalidad;
    private String tipoIdentificacion;
    private String numeroIdentificacion;
    private String tipoCliente;
    private String segmento;
    private String canalAfiliacion;
    private String comentarios;
    private String estado;
    private LocalDate fechaCreacion;
    private List<TelefonoDTO> telefonos;
    private List<DireccionDTO> direcciones;
    // Campos del contacto transaccional
    private String correoTransaccional;
    private String telefonoTransaccional;
} 