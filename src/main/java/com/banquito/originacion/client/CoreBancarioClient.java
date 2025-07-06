package com.banquito.originacion.client;

import com.banquito.originacion.controller.dto.ClienteCoreResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "core-bancario", url = "${app.core-bancario.url:http://localhost:8082}")
public interface CoreBancarioClient {

    @GetMapping("/api/clientes/{tipo}/{numero}")
    ClienteCoreResponseDTO consultarClientePorIdentificacion(@PathVariable("tipo") String tipo, @PathVariable("numero") String numero);
} 