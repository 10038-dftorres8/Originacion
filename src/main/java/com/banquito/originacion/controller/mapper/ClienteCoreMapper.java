package com.banquito.originacion.controller.mapper;

import com.banquito.originacion.controller.dto.ClienteCoreResponseDTO;
import com.banquito.originacion.controller.dto.ClienteResponseDTO;
import com.banquito.originacion.controller.dto.DireccionDTO;
import com.banquito.originacion.controller.dto.TelefonoDTO;
import com.banquito.originacion.enums.EstadoClienteEnum;
import com.banquito.originacion.model.ClienteProspecto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ClienteCoreMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mensaje", ignore = true)
    @Mapping(source = "numeroIdentificacion", target = "cedula")
    @Mapping(source = "nombre", target = "nombres")
    @Mapping(source = "scoreInterno", target = "scoreInterno")
    @Mapping(source = "estado", target = "estado", qualifiedByName = "stringToEstadoCliente")
    @Mapping(source = "telefonos", target = "telefonoTransaccional", qualifiedByName = "extractTelefonoTransaccional")
    @Mapping(source = "telefonos", target = "telefonoTipo", qualifiedByName = "extractTelefonoTipo")
    @Mapping(source = "telefonos", target = "telefonoNumero", qualifiedByName = "extractTelefonoNumero")
    @Mapping(source = "direcciones", target = "direccionTipo", qualifiedByName = "extractDireccionTipo")
    @Mapping(source = "direcciones", target = "direccionLinea1", qualifiedByName = "extractDireccionLinea1")
    @Mapping(source = "direcciones", target = "direccionLinea2", qualifiedByName = "extractDireccionLinea2")
    @Mapping(source = "direcciones", target = "direccionCodigoPostal", qualifiedByName = "extractDireccionCodigoPostal")
    @Mapping(source = "direcciones", target = "direccionGeoCodigo", qualifiedByName = "extractDireccionGeoCodigo")
    @Mapping(target = "genero", constant = "MASCULINO")
    @Mapping(target = "fechaNacimiento", expression = "java(java.time.LocalDateTime.now().minusYears(30))")
    @Mapping(target = "nivelEstudio", constant = "UNIVERSITARIO")
    @Mapping(target = "estadoCivil", constant = "SOLTERO")
    @Mapping(target = "ingresos", constant = "1000.00")
    @Mapping(target = "egresos", constant = "500.00")
    @Mapping(target = "actividadEconomica", constant = "EMPLEADO")
    @Mapping(target = "correoTransaccional", constant = "cliente@email.com")
    ClienteResponseDTO toResponseDTO(ClienteCoreResponseDTO clienteCore);

    @Mapping(source = "id", target = "idClienteCore")
    @Mapping(source = "numeroIdentificacion", target = "cedula")
    @Mapping(source = "nombre", target = "nombres")
    @Mapping(source = "scoreInterno", target = "scoreInterno")
    @Mapping(source = "telefonos", target = "telefonoTransaccional", qualifiedByName = "extractTelefonoTransaccional")
    @Mapping(source = "telefonos", target = "telefonoTipo", qualifiedByName = "extractTelefonoTipo")
    @Mapping(source = "telefonos", target = "telefonoNumero", qualifiedByName = "extractTelefonoNumero")
    @Mapping(source = "direcciones", target = "direccionTipo", qualifiedByName = "extractDireccionTipo")
    @Mapping(source = "direcciones", target = "direccionLinea1", qualifiedByName = "extractDireccionLinea1")
    @Mapping(source = "direcciones", target = "direccionLinea2", qualifiedByName = "extractDireccionLinea2")
    @Mapping(source = "direcciones", target = "direccionCodigoPostal", qualifiedByName = "extractDireccionCodigoPostal")
    @Mapping(source = "direcciones", target = "direccionGeoCodigo", qualifiedByName = "extractDireccionGeoCodigo")
    @Mapping(target = "genero", constant = "MASCULINO")
    @Mapping(target = "fechaNacimiento", expression = "java(java.time.LocalDateTime.now().minusYears(30))")
    @Mapping(target = "nivelEstudio", constant = "UNIVERSITARIO")
    @Mapping(target = "estadoCivil", constant = "SOLTERO")
    @Mapping(target = "ingresos", constant = "1000.00")
    @Mapping(target = "egresos", constant = "500.00")
    @Mapping(target = "actividadEconomica", constant = "EMPLEADO")
    @Mapping(target = "correoTransaccional", constant = "cliente@email.com")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    ClienteProspecto toEntity(ClienteCoreResponseDTO clienteCore);

    @Named("stringToEstadoCliente")
    default EstadoClienteEnum stringToEstadoCliente(String estado) {
        if (estado == null) {
            return EstadoClienteEnum.PROSPECTO;
        }
        try {
            return EstadoClienteEnum.valueOf(estado);
        } catch (IllegalArgumentException e) {
            return EstadoClienteEnum.PROSPECTO;
        }
    }

    @Named("extractTelefonoTransaccional")
    default String extractTelefonoTransaccional(List<TelefonoDTO> telefonos) {
        if (telefonos == null || telefonos.isEmpty()) return "0999999999";
        return telefonos.stream()
                .filter(t -> "ACTIVO".equals(t.getEstado()))
                .findFirst()
                .map(TelefonoDTO::getNumero)
                .orElse("0999999999");
    }

    @Named("extractTelefonoTipo")
    default String extractTelefonoTipo(List<TelefonoDTO> telefonos) {
        if (telefonos == null || telefonos.isEmpty()) return "MOVIL";
        return telefonos.stream()
                .filter(t -> "ACTIVO".equals(t.getEstado()))
                .findFirst()
                .map(TelefonoDTO::getTipo)
                .orElse("MOVIL");
    }

    @Named("extractTelefonoNumero")
    default String extractTelefonoNumero(List<TelefonoDTO> telefonos) {
        if (telefonos == null || telefonos.isEmpty()) return "0999999999";
        return telefonos.stream()
                .filter(t -> "ACTIVO".equals(t.getEstado()))
                .findFirst()
                .map(TelefonoDTO::getNumero)
                .orElse("0999999999");
    }

    @Named("extractDireccionTipo")
    default String extractDireccionTipo(List<DireccionDTO> direcciones) {
        if (direcciones == null || direcciones.isEmpty()) return "RESIDENCIAL";
        return direcciones.stream()
                .filter(d -> "ACTIVO".equals(d.getEstado()))
                .findFirst()
                .map(DireccionDTO::getTipo)
                .orElse("RESIDENCIAL");
    }

    @Named("extractDireccionLinea1")
    default String extractDireccionLinea1(List<DireccionDTO> direcciones) {
        if (direcciones == null || direcciones.isEmpty()) return "Dirección no especificada";
        return direcciones.stream()
                .filter(d -> "ACTIVO".equals(d.getEstado()))
                .findFirst()
                .map(DireccionDTO::getLinea1)
                .orElse("Dirección no especificada");
    }

    @Named("extractDireccionLinea2")
    default String extractDireccionLinea2(List<DireccionDTO> direcciones) {
        if (direcciones == null || direcciones.isEmpty()) return "";
        return direcciones.stream()
                .filter(d -> "ACTIVO".equals(d.getEstado()))
                .findFirst()
                .map(DireccionDTO::getLinea2)
                .orElse("");
    }

    @Named("extractDireccionCodigoPostal")
    default String extractDireccionCodigoPostal(List<DireccionDTO> direcciones) {
        if (direcciones == null || direcciones.isEmpty()) return "170515";
        return direcciones.stream()
                .filter(d -> "ACTIVO".equals(d.getEstado()))
                .findFirst()
                .map(DireccionDTO::getCodigoPostal)
                .orElse("170515");
    }

    @Named("extractDireccionGeoCodigo")
    default String extractDireccionGeoCodigo(List<DireccionDTO> direcciones) {
        if (direcciones == null || direcciones.isEmpty()) return "170101";
        return direcciones.stream()
                .filter(d -> "ACTIVO".equals(d.getEstado()))
                .findFirst()
                .map(DireccionDTO::getCodigoGeografico)
                .orElse("170101");
    }
} 