package com.banquito.originacion.controller;

import com.banquito.originacion.controller.dto.ClienteProspectoDTO;
import com.banquito.originacion.controller.dto.ClienteResponseDTO;
import com.banquito.originacion.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cliente", description = "API para gestión de clientes prospectos")
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping("/{cedula}")
    @Operation(summary = "Consultar cliente por cédula", 
               description = "Busca un cliente por cédula en la base local o Core Bancario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<ClienteResponseDTO> consultarClientePorCedula(
            @Parameter(description = "Cédula del cliente (10 dígitos)", example = "1234567890")
            @PathVariable String cedula) {
        
        log.info("Solicitud de consulta de cliente por cédula: {}", cedula);
        ClienteResponseDTO response = clienteService.consultarClientePorCedula(cedula);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Registrar cliente prospecto", 
               description = "Crea un nuevo cliente prospecto con validaciones")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cliente registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "409", description = "Cliente ya existe"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<ClienteResponseDTO> registrarClienteProspecto(
            @Parameter(description = "Datos del cliente prospecto")
            @Valid @RequestBody ClienteProspectoDTO clienteDTO) {
        
        log.info("Solicitud de registro de cliente prospecto con cédula: {}", clienteDTO.getCedula());
        ClienteResponseDTO response = clienteService.registrarClienteProspecto(clienteDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
} 