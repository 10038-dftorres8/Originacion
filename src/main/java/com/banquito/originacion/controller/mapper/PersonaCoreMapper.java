package com.banquito.originacion.controller.mapper;

import com.banquito.originacion.controller.dto.PersonaCoreResponseDTO;
import com.banquito.originacion.enums.GeneroClienteEnum;
import com.banquito.originacion.model.ClienteProspecto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface PersonaCoreMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "idClienteCore", ignore = true)
    @Mapping(source = "numeroIdentificacion", target = "cedula")
    @Mapping(source = "nombre", target = "nombres")
    @Mapping(source = "genero", target = "genero", qualifiedByName = "convertirGenero")
    @Mapping(source = "fechaNacimiento", target = "fechaNacimiento", qualifiedByName = "convertirFecha")
    @Mapping(source = "nivelEstudio", target = "nivelEstudio")
    @Mapping(source = "estadoCivil", target = "estadoCivil")
    @Mapping(source = "correoElectronico", target = "correoTransaccional")
    @Mapping(target = "ingresos", ignore = true)
    @Mapping(target = "egresos", ignore = true)
    @Mapping(target = "actividadEconomica", ignore = true)
    @Mapping(target = "estado", constant = "PROSPECTO")
    @Mapping(target = "telefonoTransaccional", ignore = true)
    @Mapping(target = "telefonoTipo", ignore = true)
    @Mapping(target = "telefonoNumero", ignore = true)
    @Mapping(target = "direccionTipo", ignore = true)
    @Mapping(target = "direccionLinea1", ignore = true)
    @Mapping(target = "direccionLinea2", ignore = true)
    @Mapping(target = "direccionCodigoPostal", ignore = true)
    @Mapping(target = "direccionGeoCodigo", ignore = true)
    @Mapping(target = "scoreInterno", ignore = true)
    ClienteProspecto toEntity(PersonaCoreResponseDTO personaCore);

    @Named("convertirGenero")
    default String convertirGenero(String generoCore) {
        if (generoCore == null) return null;
        
        switch (generoCore.toUpperCase()) {
            case "M":
            case "MASCULINO":
                return GeneroClienteEnum.MASCULINO.name();
            case "F":
            case "FEMENINO":
                return GeneroClienteEnum.FEMENINO.name();
            default:
                return null;
        }
    }

    @Named("convertirFecha")
    default LocalDateTime convertirFecha(LocalDate fecha) {
        if (fecha == null) return null;
        return fecha.atStartOfDay();
    }
} 