package com.banquito.originacion.controller.mapper;

import com.banquito.originacion.controller.dto.SolicitudDetalladaResponseDTO;
import com.banquito.originacion.controller.dto.VehiculoResponseDTO;
import com.banquito.originacion.controller.dto.VendedorResponseDTO;
import com.banquito.originacion.controller.dto.external.PersonaResponseDTO;
import com.banquito.originacion.controller.dto.external.ConcesionarioResponseDTO;
import com.banquito.originacion.controller.dto.external.PrestamosExternalDTO;
import com.banquito.originacion.model.SolicitudCredito;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SolicitudDetalladaMapper {
    
    @Mapping(target = "idSolicitud", source = "solicitud.id")
    @Mapping(target = "numeroSolicitud", source = "solicitud.numeroSolicitud")
    @Mapping(target = "estado", source = "solicitud.estado")
    @Mapping(target = "fechaSolicitud", source = "solicitud.fechaSolicitud")
    @Mapping(target = "cedulaSolicitante", source = "solicitud.cedulaSolicitante")
    @Mapping(target = "nombresSolicitante", source = "persona.nombre")
    @Mapping(target = "calificacionSolicitante", source = "solicitud.calificacionSolicitante")
    @Mapping(target = "capacidadPagoSolicitante", source = "solicitud.capacidadPagoSolicitante")
    @Mapping(target = "placaVehiculo", source = "solicitud.placaVehiculo")
    @Mapping(target = "marcaVehiculo", source = "vehiculo.marca")
    @Mapping(target = "modeloVehiculo", source = "vehiculo.modelo")
    @Mapping(target = "anioVehiculo", source = "vehiculo.anio")
    @Mapping(target = "valorVehiculo", source = "vehiculo.valor")
    @Mapping(target = "rucConcesionario", source = "solicitud.rucConcesionario")
    @Mapping(target = "razonSocialConcesionario", source = "concesionario.razonSocial")
    @Mapping(target = "direccionConcesionario", source = "concesionario.direccion")
    @Mapping(target = "cedulaVendedor", source = "solicitud.cedulaVendedor")
    @Mapping(target = "nombreVendedor", source = "vendedor.nombre")
    @Mapping(target = "telefonoVendedor", source = "vendedor.telefono")
    @Mapping(target = "emailVendedor", source = "vendedor.email")
    @Mapping(target = "idPrestamo", source = "solicitud.idPrestamo")
    @Mapping(target = "nombrePrestamo", source = "prestamo.nombre")
    @Mapping(target = "descripcionPrestamo", source = "prestamo.descripcion")
    @Mapping(target = "valorEntrada", source = "solicitud.valorEntrada")
    @Mapping(target = "montoSolicitado", source = "solicitud.montoSolicitado")
    @Mapping(target = "plazoMeses", source = "solicitud.plazoMeses")
    SolicitudDetalladaResponseDTO toSolicitudDetalladaResponseDTO(
            SolicitudCredito solicitud,
            PersonaResponseDTO persona,
            VehiculoResponseDTO vehiculo,
            ConcesionarioResponseDTO concesionario,
            VendedorResponseDTO vendedor,
            PrestamosExternalDTO prestamo);
} 