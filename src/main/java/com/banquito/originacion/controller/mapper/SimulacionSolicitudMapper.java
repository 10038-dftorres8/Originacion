package com.banquito.originacion.controller.mapper;

import com.banquito.originacion.controller.dto.SimulacionSolicitudResponseDTO;
import com.banquito.originacion.controller.dto.external.PrestamosExternalDTO;
import com.banquito.originacion.controller.dto.VehiculoResponseDTO;
import com.banquito.originacion.model.SolicitudCredito;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface SimulacionSolicitudMapper {

    @Mapping(target = "numeroSolicitud", source = "solicitud.numeroSolicitud")
    @Mapping(target = "cedulaSolicitante", source = "solicitud.cedulaSolicitante")
    @Mapping(target = "placaVehiculo", source = "solicitud.placaVehiculo")
    @Mapping(target = "rucConcesionario", source = "solicitud.rucConcesionario")
    @Mapping(target = "idPrestamo", source = "solicitud.idPrestamo")
    @Mapping(target = "valorVehiculo", source = "vehiculo.valor")
    @Mapping(target = "valorEntrada", source = "solicitud.valorEntrada")
    @Mapping(target = "montoSolicitado", source = "solicitud.montoSolicitado")
    @Mapping(target = "plazoMeses", source = "solicitud.plazoMeses")
    @Mapping(target = "tasaInteres", source = "prestamo.tasaInteres", qualifiedByName = "convertirTasaInteres")
    @Mapping(target = "fechaSimulacion", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "cuotaMensual", ignore = true)
    @Mapping(target = "montoTotal", ignore = true)
    @Mapping(target = "totalIntereses", ignore = true)
    @Mapping(target = "totalAPagar", ignore = true)
    @Mapping(target = "tablaAmortizacion", ignore = true)
    @Mapping(target = "nombrePrestamo", source = "prestamo.nombre")
    @Mapping(target = "descripcionPrestamo", source = "prestamo.descripcion")
    @Mapping(target = "capacidadPagoCliente", source = "solicitud.capacidadPagoSolicitante")
    @Mapping(target = "esAprobable", ignore = true)
    @Mapping(target = "motivoRechazo", ignore = true)
    SimulacionSolicitudResponseDTO toSimulacionSolicitudResponseDTO(
            SolicitudCredito solicitud, 
            PrestamosExternalDTO prestamo, 
            VehiculoResponseDTO vehiculo);

    @Named("convertirTasaInteres")
    default BigDecimal convertirTasaInteres(BigDecimal tasaInteres) {
        if (tasaInteres == null) {
            return BigDecimal.ZERO;
        }
        return tasaInteres.divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
    }
} 