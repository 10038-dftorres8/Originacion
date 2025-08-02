package com.banquito.originacion.controller.mapper;

import com.banquito.originacion.controller.dto.SolicitudCreditoResponseDTO;
import com.banquito.originacion.model.SolicitudCredito;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SolicitudCreditoMapper {
    
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "numeroSolicitud", source = "entity.numeroSolicitud")
    @Mapping(target = "fechaSolicitud", source = "entity.fechaSolicitud")
    @Mapping(target = "cedulaSolicitante", source = "entity.cedulaSolicitante")
    @Mapping(target = "calificacionSolicitante", source = "entity.calificacionSolicitante")
    @Mapping(target = "capacidadPagoSolicitante", source = "entity.capacidadPagoSolicitante")
    @Mapping(target = "placaVehiculo", source = "entity.placaVehiculo")
    @Mapping(target = "rucConcesionario", source = "entity.rucConcesionario")
    @Mapping(target = "cedulaVendedor", source = "entity.cedulaVendedor")
    @Mapping(target = "idPrestamo", source = "entity.idPrestamo")
    @Mapping(target = "valorEntrada", source = "entity.valorEntrada")
    @Mapping(target = "montoSolicitado", source = "entity.montoSolicitado")
    @Mapping(target = "plazoMeses", source = "entity.plazoMeses")
    @Mapping(target = "estado", source = "entity.estado")
    @Mapping(target = "version", source = "entity.version")
    SolicitudCreditoResponseDTO toResponseDTO(SolicitudCredito entity);
} 