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
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

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
               description = "Crea una nueva solicitud de crédito validando la existencia y estado del vehículo y vendedor. Valida automáticamente que el vendedor pertenezca a la concesionaria del vehículo.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Solicitud creada exitosamente", content = @Content(schema = @Schema(implementation = SolicitudCreditoResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o validación fallida", content = @Content)
    })
    @PostMapping
    public ResponseEntity<SolicitudCreditoResponseDTO> crearSolicitud(
            @Valid @RequestBody SolicitudCreditoExtendidaDTO solicitudDTO) {
        log.info("Creando solicitud para cliente {} y vehículo placa {}", solicitudDTO.getIdClienteProspecto(), solicitudDTO.getPlacaVehiculo());
        SolicitudCreditoResponseDTO response = solicitudService.crearSolicitudConValidacion(solicitudDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Simular crédito", 
               description = "Simula un crédito validando la existencia y disponibilidad del vehículo. Genera 3 escenarios: con entrada 20%, sin entrada, y plazo máximo para menor cuota.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Simulación exitosa", content = @Content(schema = @Schema(implementation = SimulacionCreditoResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o vehículo no disponible", content = @Content)
    })
    @PostMapping("/simular")
    public ResponseEntity<SimulacionCreditoResponseDTO> simularCredito(
            @Valid @RequestBody SimulacionCreditoRequestDTO requestDTO) {
        log.info("Simulando crédito para vehículo {} en concesionario {} con tasa {}", 
                requestDTO.getPlacaVehiculo(), requestDTO.getRucConcesionario(), requestDTO.getTasaInteres());
        SimulacionCreditoResponseDTO simulacion = solicitudService.simularCreditoConValidacion(
                requestDTO.getRucConcesionario(), requestDTO.getPlacaVehiculo(), 
                requestDTO.getMontoSolicitado(), requestDTO.getPlazoMeses(), requestDTO.getTasaInteres());
        return ResponseEntity.ok(simulacion);
    }

    @Operation(summary = "Cargar documento a solicitud", 
               description = "Sube y valida un documento PDF para la solicitud (máximo 20MB). Tipos válidos: CEDULA_IDENTIDAD, ROL_PAGOS, ESTADO_CUENTA_BANCARIA")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Documento cargado exitosamente", content = @Content(schema = @Schema(implementation = DocumentoAdjuntoResponseDTO.class))),
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

    @Operation(summary = "Descargar documento", 
               description = "Descarga un documento específico de la solicitud")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Documento descargado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Documento no encontrado", content = @Content)
    })
    @GetMapping("/{idSolicitud}/documentos/{idDocumento}/descargar")
    public ResponseEntity<Resource> descargarDocumento(
            @Parameter(description = "ID de la solicitud", required = true) @PathVariable Long idSolicitud,
            @Parameter(description = "ID del documento", required = true) @PathVariable Long idDocumento) {
        log.info("Descargando documento {} de solicitud {}", idDocumento, idSolicitud);
        Resource resource = solicitudService.descargarDocumento(idSolicitud, idDocumento);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @Operation(summary = "Ver documento", 
               description = "Visualiza un documento específico de la solicitud en el navegador")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Documento visualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Documento no encontrado", content = @Content)
    })
    @GetMapping("/{idSolicitud}/documentos/{idDocumento}/ver")
    public ResponseEntity<Resource> verDocumento(
            @Parameter(description = "ID de la solicitud", required = true) @PathVariable Long idSolicitud,
            @Parameter(description = "ID del documento", required = true) @PathVariable Long idDocumento) {
        log.info("Visualizando documento {} de solicitud {}", idDocumento, idSolicitud);
        Resource resource = solicitudService.descargarDocumento(idSolicitud, idDocumento);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @Operation(summary = "Listar documentos de solicitud", 
               description = "Obtiene la lista de documentos cargados para una solicitud")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content)
    })
    @GetMapping("/{idSolicitud}/documentos")
    public ResponseEntity<List<DocumentoAdjuntoResponseDTO>> listarDocumentos(
            @Parameter(description = "ID de la solicitud", required = true) @PathVariable Long idSolicitud) {
        log.info("Listando documentos de solicitud {}", idSolicitud);
        List<DocumentoAdjuntoResponseDTO> documentos = solicitudService.listarDocumentos(idSolicitud);
        return ResponseEntity.ok(documentos);
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

    @Operation(summary = "Consultar solicitudes por rango de fechas", 
               description = "Obtiene las solicitudes en un rango de fechas específico, opcionalmente filtradas por estado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consulta exitosa", content = @Content(schema = @Schema(implementation = SolicitudConsultaResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos", content = @Content)
    })
    @PostMapping("/consultar-por-fechas")
    public ResponseEntity<List<SolicitudConsultaResponseDTO>> consultarSolicitudesPorFechas(
            @Valid @RequestBody SolicitudConsultaRequestDTO requestDTO) {
        log.info("Consultando solicitudes entre {} y {} con estado: {}", 
                requestDTO.getFechaInicio(), requestDTO.getFechaFin(), requestDTO.getEstado());
        List<SolicitudConsultaResponseDTO> solicitudes = solicitudService.consultarSolicitudesPorRangoFechas(
                requestDTO.getFechaInicio(), requestDTO.getFechaFin(), requestDTO.getEstado());
        return ResponseEntity.ok(solicitudes);
    }
} 