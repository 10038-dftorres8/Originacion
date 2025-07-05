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