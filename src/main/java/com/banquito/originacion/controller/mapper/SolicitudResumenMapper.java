package com.banquito.originacion.controller.mapper;

import com.banquito.originacion.controller.dto.SolicitudResumenDTO;
import com.banquito.originacion.controller.dto.VehiculoResponseDTO;
import com.banquito.originacion.controller.dto.VendedorResponseDTO;
import com.banquito.originacion.model.SolicitudCredito;
import com.banquito.originacion.model.ClienteProspecto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SolicitudResumenMapper {
    
    @Mapping(target = "idSolicitud", source = "solicitud.id")
    @Mapping(target = "numeroSolicitud", source = "solicitud.numeroSolicitud")
    @Mapping(target = "estado", source = "solicitud.estado")
    @Mapping(target = "fechaSolicitud", source = "solicitud.fechaSolicitud")
    @Mapping(target = "montoSolicitado", source = "solicitud.montoSolicitado")
    @Mapping(target = "valorEntrada", source = "solicitud.valorEntrada")
    @Mapping(target = "plazoMeses", source = "solicitud.plazoMeses")
    @Mapping(target = "tasaInteresAplicada", source = "solicitud.tasaInteresAplicada")
    @Mapping(target = "cuotaMensual", source = "solicitud.cuotaMensualCalculada")
    @Mapping(target = "montoTotal", source = "solicitud.montoTotalCalculado")
    @Mapping(target = "totalIntereses", source = "solicitud.totalInteresesCalculado")
    @Mapping(target = "placaVehiculo", source = "vehiculo.placa")
    @Mapping(target = "marcaVehiculo", source = "vehiculo.marca")
    @Mapping(target = "modeloVehiculo", source = "vehiculo.modelo")
    @Mapping(target = "valorVehiculo", source = "vehiculo.valor")
    @Mapping(target = "rucConcesionario", source = "solicitud.rucConcesionario")
    @Mapping(target = "cedulaVendedor", source = "vendedor.cedula")
    @Mapping(target = "nombreVendedor", source = "vendedor.nombre")
    @Mapping(target = "idPrestamo", source = "solicitud.idPrestamo")
    @Mapping(target = "cedulaCliente", source = "clienteProspecto.cedula")
    @Mapping(target = "nombresCliente", source = "clienteProspecto.nombres")
    @Mapping(target = "correoCliente", source = "clienteProspecto.correoTransaccional")
    @Mapping(target = "telefonoCliente", source = "clienteProspecto.telefonoTransaccional")
    SolicitudResumenDTO toSolicitudResumenDTO(SolicitudCredito solicitud, ClienteProspecto clienteProspecto, 
                                            VehiculoResponseDTO vehiculo, VendedorResponseDTO vendedor);
} 