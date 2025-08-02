package com.banquito.originacion.client;

import com.banquito.originacion.controller.dto.external.PersonaResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "core-bancario", url = "${app.core-bancario.url}")
public interface CoreBancarioClient {

    @GetMapping("/api/v1/clientes/personas/{tipoIdentificacion}/{numeroIdentificacion}")
    PersonaResponseDTO consultarPersonaPorIdentificacion(@PathVariable("tipoIdentificacion") String tipoIdentificacion, @PathVariable("numeroIdentificacion") String numeroIdentificacion);

    @GetMapping("/api/v1/clientes/clientes")
    Object consultarClientePorIdentificacion(@PathVariable("tipoIdentificacion") String tipoIdentificacion, @PathVariable("numeroIdentificacion") String numeroIdentificacion);
} 
