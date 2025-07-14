package com.banquito.originacion.controller.mapper;

import com.banquito.originacion.controller.dto.EstadoSolicitudResponseDTO;
import com.banquito.originacion.model.SolicitudCredito;
import com.banquito.originacion.model.ClienteProspecto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {HistorialEstadoMapper.class})
public interface SolicitudEstadoMapper {
    
    @Mapping(target = "idSolicitud", source = "solicitud.id")
    @Mapping(target = "numeroSolicitud", source = "solicitud.numeroSolicitud")
    @Mapping(target = "estadoActual", source = "solicitud.estado")
    @Mapping(target = "fechaSolicitud", source = "solicitud.fechaSolicitud")
    @Mapping(target = "montoSolicitado", source = "solicitud.montoSolicitado")
    @Mapping(target = "plazoMeses", source = "solicitud.plazoMeses")
    @Mapping(target = "cuotaMensual", source = "solicitud.cuotaMensualCalculada")
    @Mapping(target = "placaVehiculo", source = "solicitud.placaVehiculo")
    @Mapping(target = "rucConcesionario", source = "solicitud.rucConcesionario")
    @Mapping(target = "cedulaVendedor", source = "solicitud.cedulaVendedor")
    @Mapping(target = "idPrestamo", source = "solicitud.idPrestamo")
    @Mapping(target = "cedulaCliente", source = "clienteProspecto.cedula")
    @Mapping(target = "nombresCliente", source = "clienteProspecto.nombres")
    @Mapping(target = "historial", ignore = true)
    EstadoSolicitudResponseDTO toEstadoSolicitudResponseDTO(SolicitudCredito solicitud, ClienteProspecto clienteProspecto);
} 