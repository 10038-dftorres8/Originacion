package com.banquito.originacion.controller;

import com.banquito.originacion.controller.dto.*;
import com.banquito.originacion.enums.EstadoSolicitudEnum;
import com.banquito.originacion.model.SolicitudCredito;
import com.banquito.originacion.model.DocumentoAdjunto;
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
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/solicitudes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Solicitudes de Crédito", description = "Operaciones para la gestión de solicitudes de crédito")
public class SolicitudController {

    private final SolicitudService solicitudService;

    @Operation(summary = "Crear solicitud con validación de vehículo y vendedor", 
               description = "Crea una nueva solicitud de crédito validando la existencia y estado del vehículo y vendedor")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Solicitud creada exitosamente", content = @Content(schema = @Schema(implementation = SolicitudCredito.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o validación fallida", content = @Content)
    })
    @PostMapping("/con-validacion")
    public ResponseEntity<SolicitudCreditoResponseDTO> crearSolicitudConValidacion(
            @Valid @RequestBody SolicitudCreditoExtendidaDTO solicitudDTO) {
        log.info("Creando solicitud con validación para cliente {} y vehículo {}", solicitudDTO.getIdClienteProspecto(), solicitudDTO.getPlacaVehiculo());
        SolicitudCreditoResponseDTO response = solicitudService.crearSolicitudConValidacion(solicitudDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Simular crédito con validación de vehículo", 
               description = "Simula un crédito validando la existencia y disponibilidad del vehículo - Genera 3 escenarios")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Simulación exitosa", content = @Content(schema = @Schema(implementation = AmortizacionDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o vehículo no disponible", content = @Content)
    })
    @PostMapping("/simular-con-validacion")
    public ResponseEntity<List<AmortizacionDTO>> simularCreditoConValidacion(
            @Parameter(description = "RUC del concesionario", required = true) @RequestParam String rucConcesionario,
            @Parameter(description = "Placa del vehículo", required = true) @RequestParam String placaVehiculo,
            @Parameter(description = "Monto solicitado", required = true) @RequestParam BigDecimal montoSolicitado,
            @Parameter(description = "Plazo en meses", required = true) @RequestParam Integer plazoMeses,
            @Parameter(description = "Tasa de interés anual (ej: 0.15 para 15%)", required = true) @RequestParam BigDecimal tasaInteres) {
        log.info("Simulando crédito con validación para vehículo {} en concesionario {} con tasa {}", placaVehiculo, rucConcesionario, tasaInteres);
        List<AmortizacionDTO> amortizacion = solicitudService.simularCreditoConValidacion(
                rucConcesionario, placaVehiculo, montoSolicitado, plazoMeses, tasaInteres);
        return ResponseEntity.ok(amortizacion);
    }

    @Operation(summary = "Cargar documento a solicitud", 
               description = "Sube y valida un documento PDF para la solicitud (máximo 5MB)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Documento cargado exitosamente", content = @Content(schema = @Schema(implementation = DocumentoAdjunto.class))),
        @ApiResponse(responseCode = "400", description = "Archivo inválido", content = @Content)
    })
    @PostMapping("/{idSolicitud}/documentos")
    public ResponseEntity<DocumentoAdjuntoResponseDTO> cargarDocumento(
            @Parameter(description = "ID de la solicitud", required = true) @PathVariable Long idSolicitud,
            @Parameter(description = "Archivo PDF a cargar", required = true) @RequestParam("archivo") MultipartFile archivo,
            @Parameter(description = "Tipo de documento", required = true) @RequestParam("tipoDocumento") String tipoDocumento) {
        log.info("Cargando documento '{}' para solicitud {}", tipoDocumento, idSolicitud);
        DocumentoAdjuntoResponseDTO response = solicitudService.cargarDocumento(idSolicitud, archivo, tipoDocumento);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Consultar estado e historial de solicitud", 
               description = "Retorna el estado actual y el historial de cambios de la solicitud")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consulta exitosa", content = @Content),
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

    @Operation(summary = "Obtener resumen de solicitud", 
               description = "Obtiene información resumida de la solicitud con datos del vehículo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resumen obtenido exitosamente", content = @Content(schema = @Schema(implementation = SolicitudResumenDTO.class))),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content)
    })
    @GetMapping("/{idSolicitud}/resumen")
    public ResponseEntity<SolicitudResumenDTO> obtenerResumenSolicitud(
            @Parameter(description = "ID de la solicitud", required = true) @PathVariable Long idSolicitud) {
        log.info("Obteniendo resumen de solicitud {}", idSolicitud);
        SolicitudResumenDTO resumen = solicitudService.obtenerResumenSolicitud(idSolicitud);
        return ResponseEntity.ok(resumen);
    }
} 