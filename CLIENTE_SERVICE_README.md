# ClienteService - Microservicio de Originación

## 📋 Descripción
El ClienteService es parte del microservicio de Originación y maneja la gestión de clientes prospectos según el requerimiento FR-01 - Registro y Validación de Clientes.

## 🏗️ Arquitectura

### Componentes Implementados

#### 1. **Enums** (`/enums/`)
- `GeneroClienteEnum`: MASCULINO, FEMENINO, OTRO
- `EstadoClienteEnum`: PROSPECTO, ACTIVO, INACTIVO
- `TipoTelefonoEnum`: CELULAR, RESIDENCIAL, LABORAL
- `TipoDireccionEnum`: DOMICILIO, LABORAL
- `ClasificacionClienteEnum`: CLIENTE_NUEVO, CLIENTE_ACTIVO, CLIENTE_MOROSO

#### 2. **DTOs** (`/controller/dto/`)
- `ClienteProspectoDTO`: DTO de entrada con validaciones completas
- `ClienteResponseDTO`: DTO de respuesta con clasificación del cliente

#### 3. **Mappers** (`/controller/mapper/`)
- `ClienteProspectoMapper`: Mapper de MapStruct para conversiones DTO ↔ Entity

#### 4. **Servicios** (`/service/`)
- `ClienteService`: Servicio principal con la lógica de negocio
- `CoreBancarioService`: Servicio simulado para integración con Core Bancario

#### 5. **Controlador** (`/controller/`)
- `ClienteController`: REST API con documentación OpenAPI

## 🔧 Funcionalidades Implementadas

### 1. **consultarClientePorCedula(String cedula)**
**Requerimiento:** FR-01 - Registro y Validación de Clientes

**Lógica:**
1. Buscar en tabla `clientes_prospectos` por cédula
2. Si no existe, consultar Core Bancario (simulado)
3. Si existe en Core, traer datos y clasificar cliente
4. Retornar datos del cliente + clasificación

**Endpoint:** `GET /api/v1/clientes/{cedula}`

### 2. **registrarClienteProspecto(ClienteProspectoDTO clienteDTO)**
**Requerimiento:** FR-01 - Registro y Validación de Clientes

**Lógica:**
1. Validar formato de cédula ecuatoriana (algoritmo oficial)
2. Verificar que no exista ya en la base
3. Validar contra lista negra (simulada)
4. Guardar en tabla `clientes_prospectos`
5. Clasificar automáticamente como "Cliente nuevo"

**Endpoint:** `POST /api/v1/clientes`

### 3. **clasificarCliente(ClienteProspecto cliente)**
**Requerimiento:** FR-01 - Registro y Validación de Clientes

**Lógica:**
1. Evaluar historial crediticio
2. Verificar productos activos
3. Revisar morosidad
4. Asignar clasificación: "Cliente nuevo", "Cliente activo", "Cliente moroso"

## 🛡️ Validaciones Implementadas

### Cédula Ecuatoriana
- Formato: exactamente 10 dígitos
- Algoritmo de validación del dígito verificador
- Validación contra lista negra

### Datos del Cliente
- Validaciones de longitud y formato
- Validaciones de email
- Validaciones de montos (BigDecimal)
- Validaciones de fechas

## 🔌 Integración con Otros Microservicios

### Core Bancario (Simulado)
- Consulta de clientes existentes
- Validación de lista negra
- Historial crediticio
- Productos activos
- Morosidad

**Nota:** Actualmente simulado. Para implementación real, usar OpenFeign para comunicarse con:
- Microservicio de Core Bancario
- Microservicio de Validaciones
- Microservicio de Créditos
- Microservicio de Productos

## 📊 Base de Datos

### Tabla Principal
- `originacion.clientes_prospectos`

### Relaciones
- Relación con `solicitudes_credito` (OneToMany)
- Relación con `documentos_adjuntos` (OneToMany)

## 🚀 Tecnologías Utilizadas

- **Spring Boot 3.5.3**
- **Spring Data JPA**
- **MapStruct 1.5.5.Final**
- **Lombok**
- **OpenAPI/Swagger**
- **PostgreSQL**
- **OpenFeign** (preparado para integración)

## 📝 Documentación API

La documentación está disponible en:
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

## 🔄 Flujo de Trabajo

1. **Consulta de Cliente:**
   ```
   Cliente → Controller → Service → Repository (Local)
   ↓
   Si no existe → CoreBancarioService (Simulado)
   ↓
   Clasificación → Response
   ```

2. **Registro de Cliente:**
   ```
   DTO → Controller → Service → Validaciones → Repository
   ↓
   Clasificación → Response
   ```

## 🧪 Próximos Pasos

1. **Implementar OpenFeign** para comunicación real con otros microservicios
2. **Agregar tests unitarios** y de integración
3. **Implementar cache** para consultas frecuentes
4. **Agregar métricas** con Micrometer
5. **Implementar circuit breaker** para llamadas externas
6. **Agregar validaciones adicionales** según requerimientos específicos

## 📋 Notas de Implementación

- Todos los IDs son de tipo `Long`
- Los cálculos monetarios usan `BigDecimal`
- La versión para control de concurrencia es `Long`
- Se usan métodos JPA estándar (sin `@Query` ni `@Modifying`)
- Las excepciones personalizadas están implementadas
- El logging está configurado con SLF4J

# Servicio de Clientes - Integración con Core Bancario

Este documento describe el servicio de clientes que integra con el Core Bancario para validar y registrar clientes prospectos.

## Configuración

### Dependencias
- `spring-cloud-starter-openfeign`: Cliente HTTP declarativo
- `feign-okhttp`: Cliente HTTP OkHttp para mejor rendimiento

### Configuración de URLs
En `application.properties`:
```properties
# Configuración del Core Bancario
app.core-bancario.url=http://localhost:8082
```

## Funcionalidades

### 1. Consultar Cliente por Cédula
**Endpoint:** `GET /api/v1/clientes/cedula/{cedula}`

**Funcionalidad:**
- Busca primero en la base de datos local
- Si no existe localmente, consulta en el Core Bancario
- Retorna información del cliente encontrado

**Respuesta del Core Bancario:**
```json
{
    "id": "6869ffb3ea7982e7de57e0ee",
    "tipoEntidad": "PERSONA",
    "idEntidad": "6869ff04ea7982e7de57e0ed",
    "nombre": "Juan Carlos Pérez García",
    "nacionalidad": null,
    "tipoIdentificacion": "CEDULA",
    "numeroIdentificacion": "1234567890",
    "tipoCliente": "NATURAL",
    "segmento": "PERSONAL",
    "canalAfiliacion": "OFICINA",
    "comentarios": "Cliente preferencial",
    "estado": "ACTIVO",
    "fechaCreacion": "2025-07-05",
    "telefonos": [
        {
            "tipo": "MOVIL",
            "numero": "0987002679",
            "fechaCreacion": "2025-07-06",
            "fechaActualizacion": "2025-07-06",
            "estado": "ACTIVO"
        }
    ],
    "direcciones": [
        {
            "tipo": "RESIDENCIAL",
            "linea1": "Av. Amazonas 123",
            "linea2": "Edificio Centro, Piso 5",
            "codigoPostal": "170515",
            "codigoGeografico": "170101",
            "codigoProvincia": "17",
            "codigoCanton": "01",
            "fechaCreacion": "2024-01-15",
            "fechaActualizacion": "2024-01-15",
            "estado": "ACTIVO"
        }
    ],
    "contactoTransaccional": null,
    "sucursales": [
        {
            "codigoSucursal": "001",
            "estado": "ACTIVO",
            "fechaCreacion": "2024-01-15",
            "fechaUltimaActualizacion": "2024-01-15"
        }
    ]
}
```

### 2. Registrar Cliente Prospecto
**Endpoint:** `POST /api/v1/clientes`

**Funcionalidad:**
- Valida que no exista un cliente con la misma cédula en la base local
- Consulta si existe en el Core Bancario
- Si existe en Core Bancario:
  - Mapea los datos del Core Bancario a la entidad local
  - Guarda el `idClienteCore` para referencia
  - Sobrescribe con datos del DTO de registro si están presentes
- Si no existe en Core Bancario:
  - Registra como nuevo prospecto

**Body de ejemplo:**
```json
{
    "cedula": "1234567890",
    "nombres": "Juan Pérez",
    "genero": "MASCULINO",
    "fechaNacimiento": "1990-01-01T00:00:00",
    "nivelEstudio": "UNIVERSITARIO",
    "estadoCivil": "SOLTERO",
    "ingresos": 1500.00,
    "egresos": 800.00,
    "actividadEconomica": "EMPLEADO",
    "correoTransaccional": "juan@email.com",
    "telefonoTransaccional": "0987654321",
    "telefonoTipo": "MOVIL",
    "telefonoNumero": "0987654321",
    "direccionTipo": "RESIDENCIAL",
    "direccionLinea1": "Av. Principal 123",
    "direccionLinea2": "Edificio Central",
    "direccionCodigoPostal": "170515",
    "direccionGeoCodigo": "170101"
}
```

## Mapeo de Datos

### Del Core Bancario a ClienteProspecto
- `id` → `idClienteCore` (convertido usando hashCode)
- `numeroIdentificacion` → `cedula`
- `nombre` → `nombres`
- `telefonos[0].numero` → `telefonoTransaccional` (primer teléfono activo)
- `telefonos[0].tipo` → `telefonoTipo`
- `direcciones[0].linea1` → `direccionLinea1` (primera dirección activa)
- `direcciones[0].linea2` → `direccionLinea2`
- `direcciones[0].codigoPostal` → `direccionCodigoPostal`
- `direcciones[0].codigoGeografico` → `direccionGeoCodigo`

### Valores por Defecto
Para campos no disponibles en el Core Bancario se asignan valores por defecto:
- `genero`: "MASCULINO"
- `fechaNacimiento`: 30 años atrás desde hoy
- `nivelEstudio`: "UNIVERSITARIO"
- `estadoCivil`: "SOLTERO"
- `ingresos`: 1000.00
- `egresos`: 500.00
- `actividadEconomica`: "EMPLEADO"
- `correoTransaccional`: "cliente@email.com"

## Validaciones

### Cédula Ecuatoriana
- Debe tener exactamente 10 dígitos
- Solo números
- Validación del dígito verificador según algoritmo ecuatoriano

### Duplicados
- Verifica que no exista otro cliente con la misma cédula en la base local

## Manejo de Errores

### Errores Comunes
- **400**: Cédula inválida o datos de validación incorrectos
- **404**: Cliente no encontrado
- **409**: Cliente ya existe (para registro)
- **500**: Error interno del Core Bancario

### Logging
- Registra todas las operaciones importantes
- Incluye información de trazabilidad
- Maneja errores de comunicación con el Core Bancario

## Consideraciones Técnicas

1. **Conversión de IDs**: El Core Bancario usa ObjectId de MongoDB (String), se convierte a Long usando hashCode
2. **Mapeo Inteligente**: Extrae información de arrays (teléfonos, direcciones) priorizando registros activos
3. **Sobrescritura de Datos**: Los datos del DTO de registro tienen prioridad sobre los del Core Bancario
4. **Fallback**: Si no hay datos en el Core Bancario, se usan valores por defecto sensatos

## Próximos Pasos

1. Implementar cache para consultas frecuentes
2. Agregar validaciones adicionales según reglas de negocio
3. Implementar sincronización bidireccional con Core Bancario
4. Agregar métricas de uso del servicio 