# Integración con Servicio de Gestión de Vehículos

Este documento describe la integración del servicio de originación con el servicio de gestión de vehículos usando OpenFeign.

## Configuración

### 1. Dependencias

El proyecto ya incluye las siguientes dependencias necesarias:
- `spring-cloud-starter-openfeign`: Cliente HTTP declarativo
- `feign-okhttp`: Cliente HTTP OkHttp para mejor rendimiento

### 2. Configuración de URLs

En `application.properties`:
```properties
# Configuración del servicio de gestión de vehículos
app.gestion-vehiculos.url=http://localhost:8081
```

## Componentes Implementados

### 1. Cliente Feign (`GestionVehiculosClient`)

```java
@FeignClient(name = "gestion-vehiculos", url = "${app.gestion-vehiculos.url:http://localhost:8081}")
public interface GestionVehiculosClient {
    
    @GetMapping("/api/concesionarios/ruc/{ruc}/vehiculos/placa/{placa}")
    VehiculoResponseDTO getVehiculoByPlaca(@PathVariable("ruc") String ruc, @PathVariable("placa") String placa);

    @GetMapping("/api/concesionarios/ruc/{ruc}/vendedores/cedula/{cedula}")
    VendedorResponseDTO getVendedorByCedula(@PathVariable("ruc") String ruc, @PathVariable("cedula") String cedula);
}
```

### 2. DTOs de Respuesta

- `VehiculoResponseDTO`: Información del vehículo
- `VendedorResponseDTO`: Información del vendedor
- `IdentificadorVehiculoResponseDTO`: Información de identificación del vehículo

### 3. Servicio de Integración (`GestionVehiculosService`)

Maneja la comunicación con el servicio de gestión de vehículos e incluye:
- Obtención de información de vehículos
- Obtención de información de vendedores
- Validaciones de disponibilidad y estado

### 4. Endpoints Disponibles

#### Crear Solicitud (con validación obligatoria de vehículo y vendedor)
```
POST /api/v1/solicitudes/con-validacion
```

Body:
```json
{
  "idClienteProspecto": 1,
  "idProductoCredito": 1,
  "plazoMeses": 36,
  "valorEntrada": 5000.00,
  "tasaInteres": 0.15,
  "rucConcesionario": "1234567890001",
  "placaVehiculo": "ABC1234",
  "cedulaVendedor": "0102030405"
}
```

**Nota:** El `montoSolicitado` se calcula automáticamente como: `valor del vehículo - entrada`

#### Simular Crédito (con validación obligatoria de vehículo) - 3 Escenarios
```
POST /api/v1/solicitudes/simular-con-validacion?rucConcesionario=1234567890001&placaVehiculo=ABC1234&montoSolicitado=20000.00&plazoMeses=36&tasaInteres=0.15
```

**Escenarios generados:**
1. **Con entrada 20%**: Monto financiado = 80% del monto solicitado
2. **Sin entrada**: Monto financiado = 100% del monto solicitado  
3. **Plazo máximo**: Plazo extendido para menor cuota (máximo 60 meses)

#### Otros Endpoints
- `POST /api/v1/solicitudes/{idSolicitud}/documentos` - Cargar documento
- `GET /api/v1/solicitudes/{idSolicitud}/estado` - Consultar estado
- `POST /api/v1/solicitudes/{idSolicitud}/cambiar-estado` - Cambiar estado

## Validaciones Implementadas

### Vehículo
- Existencia del vehículo en el concesionario
- Estado DISPONIBLE
- Validación de monto máximo (80% del valor del vehículo)

### Vendedor
- Existencia del vendedor en el concesionario
- Estado ACTIVO

## Manejo de Errores

### Configuración de Feign
- Timeouts configurados
- Logging completo para debugging
- Decodificador de errores personalizado

### Errores Comunes
- **404**: Vehículo o vendedor no encontrado
- **400**: Datos de validación incorrectos
- **500**: Error interno del servicio de gestión de vehículos

## Uso en el Código

### En SolicitudService

```java
// Obtener información del vehículo
VehiculoResponseDTO vehiculo = gestionVehiculosService.obtenerVehiculo(ruc, placa);

// Validar disponibilidad
if (!gestionVehiculosService.validarVehiculoDisponible(vehiculo)) {
    throw new CreateEntityException("El vehículo no está disponible");
}

// Obtener información del vendedor
VendedorResponseDTO vendedor = gestionVehiculosService.obtenerVendedor(ruc, cedula);

// Validar estado activo
if (!gestionVehiculosService.validarVendedorActivo(vendedor)) {
    throw new CreateEntityException("El vendedor no está activo");
}
```

## Consideraciones

1. **Disponibilidad del Servicio**: El servicio de gestión de vehículos debe estar ejecutándose en el puerto configurado
2. **Timeouts**: Configurar timeouts apropiados para evitar bloqueos
3. **Logging**: Usar logs para debugging de la comunicación entre servicios
4. **Fallbacks**: Considerar implementar circuit breakers para mayor robustez

## Próximos Pasos

1. Implementar circuit breakers con Resilience4j
2. Agregar métricas de comunicación entre servicios
3. Implementar cache para información frecuentemente consultada
4. Agregar validaciones adicionales según reglas de negocio 