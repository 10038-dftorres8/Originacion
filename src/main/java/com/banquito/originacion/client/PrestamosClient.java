package com.banquito.originacion.client;

import com.banquito.originacion.controller.dto.external.PrestamosExternalDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service", url = "${app.catalog-service.url:http://3.129.67.241:8081}")
public interface PrestamosClient {

    @GetMapping("/api/v1/prestamos/{id}")
    PrestamosExternalDTO consultarPrestamoPorId(@PathVariable String id);
} 