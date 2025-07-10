package com.banquito.originacion.controller.mapper;

import com.banquito.originacion.controller.dto.ClienteResponseDTO;
import com.banquito.originacion.controller.dto.ClienteProspectoRegistroDTO;
import com.banquito.originacion.model.ClienteProspecto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClienteProspectoMapper {

    @Mapping(target = "mensaje", ignore = true)
    @Mapping(target = "existeEnCore", ignore = true)
    @Mapping(target = "esCliente", ignore = true)
    ClienteResponseDTO toResponseDTO(ClienteProspecto clienteProspecto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "idClienteCore", ignore = true)
    @Mapping(target = "genero", expression = "java(dto.getGenero() != null ? dto.getGenero().name() : \"MASCULINO\")")
    @Mapping(target = "fechaNacimiento", expression = "java(dto.getFechaNacimiento() != null ? dto.getFechaNacimiento() : java.time.LocalDateTime.now().minusYears(30))")
    @Mapping(target = "nivelEstudio", expression = "java(dto.getNivelEstudio() != null ? dto.getNivelEstudio() : \"UNIVERSITARIO\")")
    @Mapping(target = "estadoCivil", expression = "java(dto.getEstadoCivil() != null ? dto.getEstadoCivil() : \"SOLTERO\")")
    @Mapping(target = "ingresos", expression = "java(dto.getIngresos() != null ? dto.getIngresos() : new java.math.BigDecimal(\"1000.00\"))")
    @Mapping(target = "egresos", expression = "java(dto.getEgresos() != null ? dto.getEgresos() : new java.math.BigDecimal(\"500.00\"))")
    @Mapping(target = "actividadEconomica", expression = "java(dto.getActividadEconomica() != null ? dto.getActividadEconomica() : \"EMPLEADO\")")
    @Mapping(target = "correoTransaccional", expression = "java(dto.getCorreoTransaccional() != null ? dto.getCorreoTransaccional() : \"cliente@email.com\")")
    @Mapping(target = "telefonoTransaccional", expression = "java(dto.getTelefonoTransaccional() != null ? dto.getTelefonoTransaccional() : \"0999999999\")")
    @Mapping(target = "telefonoTipo", expression = "java(dto.getTelefonoTipo() != null ? dto.getTelefonoTipo().name() : \"MOVIL\")")
    @Mapping(target = "telefonoNumero", expression = "java(dto.getTelefonoNumero() != null ? dto.getTelefonoNumero() : \"0999999999\")")
    @Mapping(target = "direccionTipo", expression = "java(dto.getDireccionTipo() != null ? dto.getDireccionTipo().name() : \"RESIDENCIAL\")")
    @Mapping(target = "direccionLinea1", expression = "java(dto.getDireccionLinea1() != null ? dto.getDireccionLinea1() : \"Dirección no especificada\")")
    @Mapping(target = "direccionLinea2", expression = "java(dto.getDireccionLinea2() != null ? dto.getDireccionLinea2() : \"\")")
    @Mapping(target = "direccionCodigoPostal", expression = "java(dto.getDireccionCodigoPostal() != null ? dto.getDireccionCodigoPostal() : \"170515\")")
    @Mapping(target = "direccionGeoCodigo", expression = "java(dto.getDireccionGeoCodigo() != null ? dto.getDireccionGeoCodigo() : \"170101\")")
    @Mapping(target = "estado", constant = "PROSPECTO")
    @Mapping(target = "scoreInterno", constant = "0.00")
    ClienteProspecto toEntity(ClienteProspectoRegistroDTO dto);
} 