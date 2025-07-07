package com.banquito.originacion.client;

import com.banquito.originacion.controller.dto.VehiculoResponseDTO;
import com.banquito.originacion.controller.dto.VendedorResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "gestion-vehiculos", url = "${app.gestion-vehiculos.url:http://18.223.158.69:8082}")
public interface GestionVehiculosClient {

    @GetMapping("/api/concesionarios/ruc/{ruc}/vehiculos/placa/{placa}")
    VehiculoResponseDTO getVehiculoByPlaca(@PathVariable("ruc") String ruc, @PathVariable("placa") String placa);

    @GetMapping("/api/concesionarios/ruc/{ruc}/vendedores/cedula/{cedula}")
    VendedorResponseDTO getVendedorByCedula(@PathVariable("ruc") String ruc, @PathVariable("cedula") String cedula);
} 
