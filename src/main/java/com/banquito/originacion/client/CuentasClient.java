package com.banquito.originacion.client;

import com.banquito.originacion.controller.dto.external.CuentaExternalDTO;
import com.banquito.originacion.controller.dto.external.CuentaClienteExternalDTO;
import com.banquito.originacion.controller.dto.external.CuentaClienteSolicitudDTO;
import com.banquito.originacion.controller.dto.external.CuentaMinimaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "cuentas-transaccional", url = "${app.cuentas-transaccional.url:http://3.129.67.241:8085}")
public interface CuentasClient {

    @GetMapping("/api/v1/cuentas/{id}")
    CuentaExternalDTO obtenerCuentaPorId(@PathVariable Integer id);

    @GetMapping("/api/v1/cuentas-clientes/numero-cuenta/{numeroCuenta}")
    CuentaClienteExternalDTO obtenerCuentaPorNumero(@PathVariable String numeroCuenta);

    @PostMapping("/api/v1/cuentas-clientes")
    CuentaClienteExternalDTO crearCuentaCliente(@RequestBody CuentaClienteSolicitudDTO cuentaClienteDTO);
} 