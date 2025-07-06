package com.banquito.originacion.service;

import com.banquito.originacion.client.GestionVehiculosClient;
import com.banquito.originacion.controller.dto.VehiculoResponseDTO;
import com.banquito.originacion.controller.dto.VendedorResponseDTO;
import com.banquito.originacion.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestionVehiculosService {

    private final GestionVehiculosClient gestionVehiculosClient;

    /**
     * Obtiene información de un vehículo por placa y RUC del concesionario
     */
    public VehiculoResponseDTO obtenerVehiculo(String ruc, String placa) {
        try {
            log.info("Consultando vehículo con placa: {} en concesionario RUC: {}", placa, ruc);
            VehiculoResponseDTO vehiculo = gestionVehiculosClient.getVehiculoByPlaca(ruc, placa);
            if (vehiculo == null) {
                throw new ResourceNotFoundException("Vehículo no encontrado con placa: " + placa);
            }
            log.info("Vehículo encontrado: {} - {} {}", vehiculo.getMarca(), vehiculo.getModelo(), vehiculo.getValor());
            return vehiculo;
        } catch (RestClientException e) {
            log.error("Error al consultar vehículo con placa: {} en concesionario RUC: {}", placa, ruc, e);
            throw new ResourceNotFoundException("Error al consultar vehículo: " + e.getMessage());
        }
    }

    /**
     * Obtiene información de un vendedor por cédula y RUC del concesionario
     */
    public VendedorResponseDTO obtenerVendedor(String ruc, String cedula) {
        try {
            log.info("Consultando vendedor con cédula: {} en concesionario RUC: {}", cedula, ruc);
            VendedorResponseDTO vendedor = gestionVehiculosClient.getVendedorByCedula(ruc, cedula);
            if (vendedor == null) {
                throw new ResourceNotFoundException("Vendedor no encontrado con cédula: " + cedula);
            }
            log.info("Vendedor encontrado: {} - {}", vendedor.getNombre(), vendedor.getEmail());
            return vendedor;
        } catch (RestClientException e) {
            log.error("Error al consultar vendedor con cédula: {} en concesionario RUC: {}", cedula, ruc, e);
            throw new ResourceNotFoundException("Error al consultar vendedor: " + e.getMessage());
        }
    }

    /**
     * Valida que un vehículo esté disponible para financiamiento
     */
    public boolean validarVehiculoDisponible(VehiculoResponseDTO vehiculo) {
        if (vehiculo == null) {
            return false;
        }
        
        // Verificar que el vehículo esté en estado DISPONIBLE
        boolean disponible = "DISPONIBLE".equalsIgnoreCase(vehiculo.getEstado());
        
        if (!disponible) {
            log.warn("Vehículo {} no está disponible. Estado actual: {}", 
                    vehiculo.getIdentificadorVehiculo() != null ? vehiculo.getIdentificadorVehiculo().getPlaca() : "N/A", 
                    vehiculo.getEstado());
        }
        
        return disponible;
    }

    /**
     * Valida que un vendedor esté activo
     */
    public boolean validarVendedorActivo(VendedorResponseDTO vendedor) {
        if (vendedor == null) {
            return false;
        }
        
        // Verificar que el vendedor esté en estado ACTIVO
        boolean activo = "ACTIVO".equalsIgnoreCase(vendedor.getEstado());
        
        if (!activo) {
            log.warn("Vendedor {} no está activo. Estado actual: {}", vendedor.getCedula(), vendedor.getEstado());
        }
        
        return activo;
    }
} 