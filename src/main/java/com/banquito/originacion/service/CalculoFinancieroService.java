package com.banquito.originacion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
public class CalculoFinancieroService {

    
    public BigDecimal calcularCuotaMensual(BigDecimal montoSolicitado, BigDecimal tasaInteresAnual, Integer plazoMeses) {
        if (montoSolicitado.compareTo(BigDecimal.ZERO) <= 0 || 
            tasaInteresAnual.compareTo(BigDecimal.ZERO) < 0 || 
            plazoMeses <= 0) {
            throw new IllegalArgumentException("Parámetros inválidos para el cálculo de cuota");
        }

        // Convertir tasa anual a mensual
        BigDecimal tasaMensual = tasaInteresAnual.divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP);
        
        
        BigDecimal unoMasTasa = BigDecimal.ONE.add(tasaMensual);
        BigDecimal unoMasTasaElevadoN = unoMasTasa.pow(plazoMeses);
        
        BigDecimal numerador = tasaMensual.multiply(unoMasTasaElevadoN);
        BigDecimal denominador = unoMasTasaElevadoN.subtract(BigDecimal.ONE);
        
        BigDecimal factor = numerador.divide(denominador, 8, RoundingMode.HALF_UP);
        BigDecimal cuotaMensual = montoSolicitado.multiply(factor);
        
        log.info("Cuota mensual calculada: {} para monto: {}, tasa: {}, plazo: {} meses", 
                cuotaMensual, montoSolicitado, tasaInteresAnual, plazoMeses);
        
        return cuotaMensual.setScale(2, RoundingMode.HALF_UP);
    }

    
    public BigDecimal calcularMontoTotal(BigDecimal cuotaMensual, Integer plazoMeses) {
        return cuotaMensual.multiply(BigDecimal.valueOf(plazoMeses)).setScale(2, RoundingMode.HALF_UP);
    }

    
    public BigDecimal calcularTotalIntereses(BigDecimal montoTotal, BigDecimal montoSolicitado) {
        return montoTotal.subtract(montoSolicitado).setScale(2, RoundingMode.HALF_UP);
    }

    
    public boolean validarTasaInteres(BigDecimal tasaInteresAnual, BigDecimal tasaBase, BigDecimal margenPermitido) {
        BigDecimal tasaMinima = tasaBase.subtract(margenPermitido);
        BigDecimal tasaMaxima = tasaBase.add(margenPermitido);
        
        return tasaInteresAnual.compareTo(tasaMinima) >= 0 && 
               tasaInteresAnual.compareTo(tasaMaxima) <= 0;
    }

    
    public BigDecimal calcularMontoSolicitado(BigDecimal valorVehiculo, BigDecimal valorEntrada) {
        if (valorVehiculo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor del vehículo debe ser mayor a cero");
        }
        
        if (valorEntrada.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El valor de entrada no puede ser negativo");
        }
        
        if (valorEntrada.compareTo(valorVehiculo) >= 0) {
            throw new IllegalArgumentException("El valor de entrada no puede ser mayor o igual al valor del vehículo");
        }
        
        return valorVehiculo.subtract(valorEntrada).setScale(2, RoundingMode.HALF_UP);
    }

    
    public boolean validarMontoSolicitado(BigDecimal montoSolicitado, BigDecimal montoMinimo, BigDecimal montoMaximo) {
        return montoSolicitado.compareTo(montoMinimo) >= 0 && 
               montoSolicitado.compareTo(montoMaximo) <= 0;
    }

    
    public boolean validarPlazo(Integer plazoMeses, Integer plazoMinimo, Integer plazoMaximo) {
        return plazoMeses >= plazoMinimo && plazoMeses <= plazoMaximo;
    }
} 