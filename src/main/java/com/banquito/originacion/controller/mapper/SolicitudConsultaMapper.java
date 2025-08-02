package com.banquito.originacion.controller.mapper;

import com.banquito.originacion.controller.dto.SolicitudConsultaResponseDTO;
import com.banquito.originacion.model.SolicitudCredito;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SolicitudConsultaMapper {
    
    @Mapping(target = "idSolicitud", source = "solicitud.id")
    @Mapping(target = "numeroSolicitud", source = "solicitud.numeroSolicitud")
    @Mapping(target = "estado", source = "solicitud.estado")
    @Mapping(target = "fechaSolicitud", source = "solicitud.fechaSolicitud")
    @Mapping(target = "montoSolicitado", source = "solicitud.montoSolicitado")
    @Mapping(target = "plazoMeses", source = "solicitud.plazoMeses")
    @Mapping(target = "placaVehiculo", source = "solicitud.placaVehiculo")
    @Mapping(target = "rucConcesionario", source = "solicitud.rucConcesionario")
    @Mapping(target = "cedulaVendedor", source = "solicitud.cedulaVendedor")
    @Mapping(target = "idPrestamo", source = "solicitud.idPrestamo")
    @Mapping(target = "cedulaSolicitante", source = "solicitud.cedulaSolicitante")
    SolicitudConsultaResponseDTO toSolicitudConsultaResponseDTO(SolicitudCredito solicitud);
} 