package com.banquito.originacion;

import com.banquito.originacion.controller.dto.*;
import com.banquito.originacion.enums.EstadoSolicitudEnum;
import com.banquito.originacion.service.SolicitudService;
import com.banquito.originacion.service.VendedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/originacion/v1/solicitudes")
@CrossOrigin(origins = "*")
@Tag(name = "Solicitudes de Crédito", description = "Operaciones para la gestión de solicitudes de crédito")
public class SolicitudController {

    @Autowired
    private SolicitudService solicitudService;

    @Autowired
    private VendedorService vendedorService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        System.out.println("Endpoint de prueba llamado");
        return ResponseEntity.ok("Backend funcionando correctamente - " + System.currentTimeMillis());
    }

    @GetMapping("/test-vendedor/{email}")
    public ResponseEntity<String> testVendedor(@PathVariable String email) {
        try {
            System.out.println("Probando obtención de cédula para vendedor: " + email);
            String cedula = vendedorService.obtenerCedulaVendedorPorEmail(email);
            if (cedula != null) {
                return ResponseEntity.ok("Cédula encontrada: " + cedula);
            } else {
                return ResponseEntity.ok("No se encontró cédula para el email: " + email);
            }
        } catch (Exception e) {
            System.out.println("Error al probar vendedor: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }

    @GetMapping("/todas")
    public ResponseEntity<String> todasLasSolicitudes() {
        try {
            System.out.println("Consultando todas las solicitudes sin filtros");
            // Usar fechas muy amplias para obtener todas las solicitudes
            LocalDateTime fechaInicio = LocalDateTime.of(2020, 1, 1, 0, 0);
            LocalDateTime fechaFin = LocalDateTime.of(2030, 12, 31, 23, 59);
            
            SolicitudConsultaPaginadaResponseDTO solicitudes = solicitudService.consultarSolicitudesPorRangoFechas(
                    fechaInicio, fechaFin, null, null, null, 0, 100);
            
            return ResponseEntity.ok("Total solicitudes encontradas: " + solicitudes.getTotalElementos() + 
                    " - Solicitudes: " + solicitudes.getSolicitudes().size());
        } catch (Exception e) {
            System.out.println("Error al consultar todas las solicitudes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }

    @PostMapping("/consultar-por-fechas")
    public ResponseEntity<SolicitudConsultaPaginadaResponseDTO> consultarSolicitudesPorFechas(
            @Valid @RequestBody SolicitudConsultaRequestDTO requestDTO,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        try {
            System.out.println("Consultando solicitudes por fechas");
            System.out.println("Usuario logueado: " + userEmail);
            
            // Si es vendedor, forzar el filtro por su cédula
            String cedulaVendedorFiltro = requestDTO.getCedulaVendedor();
            System.out.println("Filtro inicial cedulaVendedor: " + cedulaVendedorFiltro);
            
            if (userEmail != null && !userEmail.isEmpty()) {
                System.out.println("Usuario logueado como vendedor: " + userEmail);
                String cedulaVendedor = obtenerCedulaVendedorPorEmail(userEmail);
                System.out.println("Cédula obtenida del servicio: " + cedulaVendedor);
                
                if (cedulaVendedor != null) {
                    cedulaVendedorFiltro = cedulaVendedor;
                    System.out.println("Aplicando filtro por vendedor: " + cedulaVendedor);
                } else {
                    System.out.println("No se pudo obtener la cédula del vendedor para email: " + userEmail);
                }
            } else {
                System.out.println("Usuario no identificado o admin, mostrando todas las solicitudes");
            }
            
            System.out.println("Filtro final cedulaVendedor: " + cedulaVendedorFiltro);
            
            SolicitudConsultaPaginadaResponseDTO solicitudes = solicitudService.consultarSolicitudesPorRangoFechas(
                    requestDTO.getFechaInicio(), requestDTO.getFechaFin(), requestDTO.getEstado(),
                    cedulaVendedorFiltro, requestDTO.getRucConcesionario(),
                    requestDTO.getPagina(), requestDTO.getTamanoPagina());
            
            return ResponseEntity.ok(solicitudes);
        } catch (Exception e) {
            System.out.println("Error al consultar solicitudes por fechas: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{numeroSolicitud}/detalle")
    public ResponseEntity<SolicitudDetalladaResponseDTO> obtenerSolicitudDetallada(
            @Parameter(description = "Número de la solicitud", required = true) @PathVariable String numeroSolicitud) {
        try {
            System.out.println("Obteniendo información detallada de la solicitud: " + numeroSolicitud);
            SolicitudDetalladaResponseDTO solicitudDetallada = solicitudService.obtenerSolicitudDetallada(numeroSolicitud);
            return ResponseEntity.ok(solicitudDetallada);
        } catch (Exception e) {
            System.out.println("Error al obtener detalle de solicitud: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{numeroSolicitud}/simular")
    public ResponseEntity<SimulacionSolicitudResponseDTO> simularSolicitud(
            @Parameter(description = "Número de la solicitud", required = true) @PathVariable String numeroSolicitud) {
        try {
            System.out.println("Simulando solicitud con número: " + numeroSolicitud);
            SimulacionSolicitudResponseDTO simulacion = solicitudService.simularSolicitud(numeroSolicitud);
            return ResponseEntity.ok(simulacion);
        } catch (Exception e) {
            System.out.println("Error al simular solicitud: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Método auxiliar para obtener la cédula del vendedor por su email
     */
    private String obtenerCedulaVendedorPorEmail(String email) {
        try {
            System.out.println("Obteniendo cédula del vendedor para email: " + email);
            return vendedorService.obtenerCedulaVendedorPorEmail(email);
        } catch (Exception e) {
            System.out.println("Error al obtener cédula del vendedor: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Operation(summary = "Consultar cliente por cédula", description = "Consulta cliente desde CoreBancario y devuelve al frontend")
    @GetMapping("/v1/clientes/personas/CEDULA/{cedula}")
    public ResponseEntity<Object> consultarClientePorCedula(@PathVariable String cedula) {
        System.out.println("Consultando cliente por cédula: " + cedula);
        try {
            // Llamar a CoreBancario desde Originacion
            String url = "http://banquito-alb-1166574131.us-east-2.elb.amazonaws.com/api/general/v1/clientes/personas/CEDULA/" + cedula;
            System.out.println("Llamando a: " + url);
            
            RestTemplate restTemplate = new RestTemplate();
            Object cliente = restTemplate.getForObject(url, Object.class);
            
            System.out.println("Cliente obtenido: " + cliente);
            return ResponseEntity.ok(cliente);
        } catch (Exception e) {
            System.out.println("Error al consultar cliente: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Error al consultar cliente: " + e.getMessage()));
        }
    }

    @Operation(summary = "Cambiar estado de solicitud", description = "Cambia el estado de una solicitud de crédito")
    @PostMapping("/{idSolicitud}/cambiar-estado")
    public ResponseEntity<String> cambiarEstado(
            @Parameter(description = "ID de la solicitud", required = true) @PathVariable Long idSolicitud,
            @Parameter(description = "Nuevo estado", required = true) @RequestParam String nuevoEstado,
            @Parameter(description = "Motivo del cambio", required = false) @RequestParam(required = false) String motivo,
            @Parameter(description = "Usuario que realiza el cambio", required = false) @RequestParam(required = false) String usuario) {
        try {
            System.out.println("Cambiando estado de solicitud " + idSolicitud + " a: " + nuevoEstado);
            solicitudService.cambiarEstadoSolicitud(idSolicitud, nuevoEstado, motivo, usuario);
            return ResponseEntity.ok("Estado cambiado exitosamente a: " + nuevoEstado);
        } catch (Exception e) {
            System.out.println("Error al cambiar estado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al cambiar estado: " + e.getMessage());
        }
    }
}
