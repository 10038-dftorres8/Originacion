package com.banquito.originacion.controller;

import com.banquito.originacion.controller.dto.*;
import com.banquito.originacion.enums.EstadoSolicitudEnum;
import com.banquito.originacion.model.SolicitudCredito;
import com.banquito.originacion.service.SolicitudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.banquito.originacion.controller.dto.SolicitudConsultaRequestDTO;
import com.banquito.originacion.controller.dto.SolicitudConsultaResponseDTO;

@RestController
@RequestMapping("/api/v1/solicitudes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Solicitudes de Crédito", description = "Operaciones para la gestión de solicitudes de crédito")
public class SolicitudController {

    private final SolicitudService solicitudService;

    @Operation(summary = "Crear solicitud de crédito", 
               description = "Crea una nueva solicitud de crédito validando la existencia del cliente en Core Bancario, del préstamo y del vehículo. Calcula automáticamente el monto solicitado y valida la capacidad de pago.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Solicitud creada exitosamente", content = @Content(schema = @Schema(implementation = SolicitudCreditoResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o validación fallida", content = @Content)
    })
    @PostMapping
    public ResponseEntity<SolicitudCreditoResponseDTO> crearSolicitud(
            @Valid @RequestBody SolicitudCreditoDTO solicitudDTO) {
        log.info("Creando solicitud para cédula {} y vehículo placa {}", solicitudDTO.getCedulaSolicitante(), solicitudDTO.getPlacaVehiculo());
        SolicitudCreditoResponseDTO response = solicitudService.crearSolicitud(solicitudDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Simular solicitud", 
               description = "Simula una solicitud de crédito ya creada para mostrar la tabla de amortización específica del cliente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Simulación exitosa", content = @Content(schema = @Schema(implementation = SimulacionSolicitudResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content)
    })
    @GetMapping("/{numeroSolicitud}/simular")
    public ResponseEntity<SimulacionSolicitudResponseDTO> simularSolicitud(
            @Parameter(description = "Número de la solicitud", required = true) @PathVariable String numeroSolicitud) {
        log.info("Simulando solicitud con número: {}", numeroSolicitud);
        SimulacionSolicitudResponseDTO simulacion = solicitudService.simularSolicitud(numeroSolicitud);
        return ResponseEntity.ok(simulacion);
    }



    @Operation(summary = "Consultar estado e historial de solicitud", 
               description = "Retorna el estado actual y el historial de cambios de la solicitud")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consulta exitosa", content = @Content(schema = @Schema(implementation = EstadoSolicitudResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content)
    })
    @GetMapping("/{idSolicitud}/estado")
    public ResponseEntity<EstadoSolicitudResponseDTO> consultarEstadoSolicitud(
            @Parameter(description = "ID de la solicitud", required = true) @PathVariable Long idSolicitud) {
        log.info("Consultando estado e historial de solicitud {}", idSolicitud);
        EstadoSolicitudResponseDTO response = solicitudService.consultarEstadoSolicitud(idSolicitud);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cambiar estado de solicitud", 
               description = "Cambia el estado de la solicitud y registra la trazabilidad")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado cambiado exitosamente", content = @Content),
        @ApiResponse(responseCode = "400", description = "Transición no permitida", content = @Content)
    })
    @PostMapping("/{idSolicitud}/cambiar-estado")
    public ResponseEntity<Void> cambiarEstadoSolicitud(
            @Parameter(description = "ID de la solicitud", required = true) @PathVariable Long idSolicitud,
            @Parameter(description = "Nuevo estado", required = true, schema = @Schema(implementation = EstadoSolicitudEnum.class)) @RequestParam EstadoSolicitudEnum nuevoEstado,
            @Parameter(description = "Motivo del cambio", required = true) @RequestParam String motivo,
            @Parameter(description = "Usuario que realiza el cambio", required = true) @RequestParam String usuario) {
        log.info("Cambiando estado de solicitud {} a {}", idSolicitud, nuevoEstado);
        solicitudService.cambiarEstadoSolicitud(idSolicitud, nuevoEstado.name(), motivo, usuario);
        return ResponseEntity.ok().build();
    }



    @Operation(summary = "Consultar solicitudes por rango de fechas", 
               description = "Obtiene las solicitudes en un rango de fechas específico (máximo 31 días), opcionalmente filtradas por estado, cédula del vendedor y RUC del concesionario, con paginación. Los campos opcionales que vengan como null se ignoran (no se aplica filtro).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consulta exitosa", content = @Content(schema = @Schema(implementation = SolicitudConsultaPaginadaResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos o rango de fechas excede 31 días", content = @Content)
    })
    @PostMapping("/consultar-por-fechas")
    public ResponseEntity<SolicitudConsultaPaginadaResponseDTO> consultarSolicitudesPorFechas(
            @Valid @RequestBody SolicitudConsultaRequestDTO requestDTO) {
        log.info("Consultando solicitudes entre {} y {} con estado: {}, vendedor: {}, concesionario: {}, página: {}, tamaño: {}", 
                requestDTO.getFechaInicio(), requestDTO.getFechaFin(), requestDTO.getEstado(), 
                requestDTO.getCedulaVendedor(), requestDTO.getRucConcesionario(),
                requestDTO.getPagina(), requestDTO.getTamanoPagina());
        SolicitudConsultaPaginadaResponseDTO solicitudes = solicitudService.consultarSolicitudesPorRangoFechas(
                requestDTO.getFechaInicio(), requestDTO.getFechaFin(), requestDTO.getEstado(),
                requestDTO.getCedulaVendedor(), requestDTO.getRucConcesionario(),
                requestDTO.getPagina(), requestDTO.getTamanoPagina());
        return ResponseEntity.ok(solicitudes);
    }

    @Operation(summary = "Obtener información detallada de solicitud", 
               description = "Obtiene toda la información detallada de una solicitud incluyendo datos del solicitante, vehículo, concesionario, vendedor y préstamo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Información obtenida exitosamente", content = @Content(schema = @Schema(implementation = SolicitudDetalladaResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content),
        @ApiResponse(responseCode = "400", description = "Error al obtener información externa", content = @Content)
    })
    @GetMapping("/{numeroSolicitud}/detalle")
    public ResponseEntity<SolicitudDetalladaResponseDTO> obtenerSolicitudDetallada(
            @Parameter(description = "Número de la solicitud", required = true) @PathVariable String numeroSolicitud) {
        log.info("Obteniendo información detallada de la solicitud: {}", numeroSolicitud);
        SolicitudDetalladaResponseDTO solicitudDetallada = solicitudService.obtenerSolicitudDetallada(numeroSolicitud);
        return ResponseEntity.ok(solicitudDetallada);
    }

    @Operation(summary = "Editar solicitud de crédito", 
               description = "Edita una solicitud de crédito existente en estado BORRADOR, aplicando las mismas validaciones que la creación")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Solicitud editada exitosamente", content = @Content(schema = @Schema(implementation = SolicitudCreditoResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos, validación fallida o solicitud no en estado BORRADOR", content = @Content),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content)
    })
    @PutMapping("/{numeroSolicitud}")
    public ResponseEntity<SolicitudCreditoResponseDTO> editarSolicitud(
            @Parameter(description = "Número de la solicitud", required = true) @PathVariable String numeroSolicitud,
            @Valid @RequestBody SolicitudCreditoEdicionDTO solicitudEdicionDTO) {
        log.info("Editando solicitud: {} para cédula: {}", numeroSolicitud, solicitudEdicionDTO.getCedulaSolicitante());
        SolicitudCreditoResponseDTO response = solicitudService.editarSolicitud(numeroSolicitud, solicitudEdicionDTO);
        return ResponseEntity.ok(response);
    }
} 