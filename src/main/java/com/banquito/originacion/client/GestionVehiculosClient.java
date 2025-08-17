package com.banquito.originacion.client;

import com.banquito.originacion.controller.dto.VehiculoResponseDTO;
import com.banquito.originacion.controller.dto.VendedorResponseDTO;
import com.banquito.originacion.controller.dto.external.ConcesionarioResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "gestion-vehiculos", url = "${app.gestion-vehiculos.url}")
public interface GestionVehiculosClient {

    @GetMapping("/api/concesionarios/v1/ruc/{ruc}/vehiculos/placa/{placa}")
    VehiculoResponseDTO getVehiculoByPlaca(@PathVariable("ruc") String ruc, @PathVariable("placa") String placa);

    @GetMapping("/api/concesionarios/v1/ruc/{ruc}/vendedores/cedula/{cedula}")
    VendedorResponseDTO getVendedorByCedula(@PathVariable("ruc") String ruc, @PathVariable("cedula") String cedula);
    
    @GetMapping("/api/concesionarios/v1/ruc/{ruc}")
    ConcesionarioResponseDTO getConcesionarioByRuc(@PathVariable("ruc") String ruc);
} 
