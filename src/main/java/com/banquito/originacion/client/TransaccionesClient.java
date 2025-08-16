package com.banquito.originacion.client;

import com.banquito.originacion.controller.dto.external.PrestamoClienteExternalDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "transacciones", url = "${app.transacciones.url}")
public interface TransaccionesClient {

    @PostMapping("/api/prestamos/v1/prestamos-clientes")
    PrestamoClienteExternalDTO crearPrestamoCliente(@RequestBody PrestamoClienteExternalDTO prestamoClienteDTO);
} 