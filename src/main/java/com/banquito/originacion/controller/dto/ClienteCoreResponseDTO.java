package com.banquito.originacion.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class ClienteCoreResponseDTO {
    private String id;
    private String tipoEntidad;
    private String idEntidad;
    private String nombre;
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
    private String contactoTransaccional;
    private List<SucursalDTO> sucursales;
} 