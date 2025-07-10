package com.banquito.originacion.service;

import com.banquito.originacion.client.CuentasClient;
import com.banquito.originacion.controller.dto.external.CuentaExternalDTO;
import com.banquito.originacion.controller.dto.external.CuentaClienteExternalDTO;
import com.banquito.originacion.controller.dto.external.CuentaClienteSolicitudDTO;
import com.banquito.originacion.controller.dto.external.CuentaMinimaDTO;
import com.banquito.originacion.exception.CreateEntityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class CuentasClientesService {

    private final CuentasClient cuentasClient;
    private final Random random = new Random();

    public CuentaClienteExternalDTO crearCuentaClienteParaCliente(String idCliente) {
        log.info("Creando cuenta cliente para cliente: {}", idCliente);
        

        String numeroCuenta = generarNumeroCuentaUnico();
        

        CuentaExternalDTO cuenta = obtenerCuentaPorId(5);
        if (cuenta == null) {
            throw new CreateEntityException("CuentaCliente", "No se pudo obtener la cuenta con ID 5");
        }
        

        CuentaClienteSolicitudDTO cuentaClienteDTO = CuentaClienteSolicitudDTO.builder()
                .idCuenta(cuenta.getId())
                .idCliente(idCliente)
                .numeroCuenta(numeroCuenta)
                .saldoDisponible(new BigDecimal("10.00"))
                .saldoContable(new BigDecimal("10.00"))
                .build();
        
        try {
            log.info("=== DATOS PARA CREAR CUENTA CLIENTE ===");
            log.info("ID Cuenta: {}", cuentaClienteDTO.getIdCuenta());
            log.info("ID Cliente: {}", cuentaClienteDTO.getIdCliente());
            log.info("Número Cuenta: {}", cuentaClienteDTO.getNumeroCuenta());
            log.info("Saldo Disponible: {}", cuentaClienteDTO.getSaldoDisponible());
            log.info("Saldo Contable: {}", cuentaClienteDTO.getSaldoContable());
            log.info("URL del servicio: {}", "http://3.129.67.241:8085/api/v1/cuentas-clientes");
            log.info("========================================");
            
            CuentaClienteExternalDTO cuentaCreada = cuentasClient.crearCuentaCliente(cuentaClienteDTO);
            log.info("Cuenta cliente creada exitosamente:");
            log.info("  - ID: {}", cuentaCreada.getId());
            log.info("  - Número Cuenta: {}", cuentaCreada.getNumeroCuenta());
            log.info("  - Cliente: {}", cuentaCreada.getIdCliente());
            log.info("  - Estado: {}", cuentaCreada.getEstado());
            return cuentaCreada;
        } catch (RestClientException e) {
            log.error("Error RestClient al crear cuenta cliente para cliente {}: {}", idCliente, e.getMessage());
            throw new CreateEntityException("CuentaCliente", "Error al crear cuenta cliente: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al crear cuenta cliente para cliente {}: {}", idCliente, e.getMessage());
            log.error("Stack trace completo:", e);
            throw new CreateEntityException("CuentaCliente", "Error inesperado al crear cuenta cliente: " + e.getMessage());
        }
    }
    
    private CuentaExternalDTO obtenerCuentaPorId(Integer idCuenta) {
        try {
            log.info("Intentando obtener cuenta con ID: {}", idCuenta);
            CuentaExternalDTO cuenta = cuentasClient.obtenerCuentaPorId(idCuenta);
            log.info("Cuenta obtenida exitosamente: ID={}, Nombre={}, Código={}", cuenta.getId(), cuenta.getNombre(), cuenta.getCodigoCuenta());
            return cuenta;
        } catch (RestClientException e) {
            log.error("Error al obtener cuenta con ID {}: {}", idCuenta, e.getMessage());
            throw new CreateEntityException("CuentaCliente", "Error al obtener cuenta con ID " + idCuenta + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al obtener cuenta con ID {}: {}", idCuenta, e.getMessage());
            throw new CreateEntityException("CuentaCliente", "Error inesperado al obtener cuenta con ID " + idCuenta + ": " + e.getMessage());
        }
    }
    
    private String generarNumeroCuentaUnico() {

        for (int intento = 0; intento < 5; intento++) {
            String numeroCuenta = generarNumeroCuentaAleatorio();
            log.info("Intentando número de cuenta: {} (intento {})", numeroCuenta, intento + 1);
            
            try {
                cuentasClient.obtenerCuentaPorNumero(numeroCuenta);
                log.info("Número de cuenta {} ya existe, intentando otro", numeroCuenta);
            } catch (RestClientException e) {
                log.info("Número de cuenta {} está disponible", numeroCuenta);
                return numeroCuenta;
            } catch (Exception e) {
                log.warn("Error al verificar número de cuenta {}: {}", numeroCuenta, e.getMessage());
                return numeroCuenta; // En caso de error, usar este número
            }
        }
        
        String numeroCuenta = generarNumeroCuentaConTimestamp();
        log.info("Usando número de cuenta con timestamp: {}", numeroCuenta);
        return numeroCuenta;
    }
    
    private String generarNumeroCuentaAleatorio() {
        StringBuilder numero = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            numero.append(random.nextInt(10));
        }
        String numeroGenerado = numero.toString();
        log.info("Número de cuenta aleatorio generado: {} (longitud: {})", numeroGenerado, numeroGenerado.length());
        return numeroGenerado;
    }
    
    private String generarNumeroCuentaConTimestamp() {
        long timestamp = System.currentTimeMillis();
        String timestampStr = String.valueOf(timestamp);
        if (timestampStr.length() > 10) {
            timestampStr = timestampStr.substring(timestampStr.length() - 10);
        } else {
            while (timestampStr.length() < 10) {
                timestampStr = "0" + timestampStr;
            }
        }
        return timestampStr;
    }
} 