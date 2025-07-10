package com.banquito.originacion.client;

import com.banquito.originacion.controller.dto.ClienteCoreResponseDTO;
import com.banquito.originacion.controller.dto.PersonaCoreResponseDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "core-bancario", url = "${app.core-bancario.url:http://3.129.67.241:8083}")
public interface CoreBancarioClient {

    @GetMapping("/api/v1/clientes/personas/{tipoIdentificacion}/{numeroIdentificacion}")
    PersonaCoreResponseDTO consultarPersonaPorIdentificacion(@PathVariable("tipoIdentificacion") String tipoIdentificacion, @PathVariable("numeroIdentificacion") String numeroIdentificacion);

    @GetMapping("/api/v1/clientes/clientes")
    List<ClienteCoreResponseDTO> consultarClientePorIdentificacion(@RequestParam("tipoIdentificacion") String tipoIdentificacion, @RequestParam("numeroIdentificacion") String numeroIdentificacion);

    @PostMapping("/api/v1/clientes/personas")
    PersonaCoreResponseDTO crearPersona(@RequestBody PersonaCoreResponseDTO personaDTO);

    @PostMapping("/api/v1/clientes/personas/{tipoIdentificacion}/{numeroIdentificacion}/clientes")
    ClienteCoreResponseDTO crearClienteDesdePersona(
            @PathVariable("tipoIdentificacion") String tipoIdentificacion,
            @PathVariable("numeroIdentificacion") String numeroIdentificacion,
            @RequestBody ClienteCoreResponseDTO clienteDTO);
} 
