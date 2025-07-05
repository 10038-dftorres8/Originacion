# 📘 API REST - Sistema de Gestión Vehicular

## 🚗 Identificadores de Vehículos

### 🔹 Listar todos los identificadores

```
GET http://localhost:8080/v1/identificadores-vehiculos
```

### 🔹 Buscar por ID

```
GET http://localhost:8080/v1/identificadores-vehiculos/{id}
```

### 🔹 Crear un identificador

```
POST http://localhost:8080/v1/identificadores-vehiculos
```

```json
{
  "vin": "1HGCM82633A004352",
  "numeroMotor": "MTR123456789",
  "placa": "ABC1234"
}
```

---

## 🏢 Concesionarios

### 🔹 Listar todos

```
GET http://localhost:8080/v1/concesionarios
```

### 🔹 Buscar por ID

```
GET http://localhost:8080/v1/concesionarios/{id}
```

### 🔹 Buscar por estado

```
GET http://localhost:8080/v1/concesionarios/estado/{estado}
```

### 🔹 Buscar por razón social

```
GET http://localhost:8080/v1/concesionarios/razon-social/{razon}
```

### 🔹 Buscar por email

```
GET http://localhost:8080/v1/concesionarios/email/{email}
```

### 🔹 Crear concesionario

```
POST http://localhost:8080/v1/concesionarios
```

```json
{
  "razonSocial": "Concesionaria Ejemplo",
  "direccion": "Av. Principal 123",
  "telefono": "0999999999",
  "emailContacto": "contacto@ejemplo.com",
  "estado": "ACTIVO"
}
```

### 🔹 Actualizar concesionario

```
PUT http://localhost:8080/v1/concesionarios/{id}
```

```json
{
  "razonSocial": "Concesionaria Ejemplo",
  "direccion": "Av. Principal 123",
  "telefono": "0999999999",
  "emailContacto": "contacto@ejemplo.com",
  "estado": "ACTIVO"
}
```

> 📌 **Nota:** Cambiar el campo que se desea actualizar.

### 🔹 Desactivar concesionario

```
PATCH http://localhost:8080/v1/concesionarios/{id}/desactivar
```

---

## 🚘 Vehículos

### 🔹 Listar todos

```
GET http://localhost:8080/v1/vehiculos
```

### 🔹 Buscar por ID

```
GET http://localhost:8080/v1/vehiculos/{id}
```

### 🔹 Buscar por concesionario

```
GET http://localhost:8080/v1/vehiculos/concesionario/{idConcesionario}
```

### 🔹 Buscar por estado

```
GET http://localhost:8080/v1/vehiculos/estado/{estado}
```

### 🔹 Buscar por marca

```
GET http://localhost:8080/v1/vehiculos/marca/{marca}
```

### 🔹 Buscar por modelo

```
GET http://localhost:8080/v1/vehiculos/modelo/{modelo}
```

### 🔹 Buscar por identificador

```
GET http://localhost:8080/v1/vehiculos/identificador/{idIdentificador}
```

### 🔹 Crear vehículo

```
POST http://localhost:8080/v1/vehiculos
```

```json
{
  "idConcesionario": 2,
  "idIdentificadorVehiculo": 2,
  "marca": "Toyota",
  "modelo": "Corolla",
  "anio": 2022,
  "valor": 18000.00,
  "color": "Rojo",
  "extras": "Aire acondicionado, Bluetooth",
  "estado": "NUEVO"
}
```

### 🔹 Actualizar vehículo

```
PUT http://localhost:8080/v1/vehiculos/{id}
```

```json
{
  "idConcesionario": 2,
  "idIdentificadorVehiculo": 2,
  "marca": "Toyota",
  "modelo": "Corolla",
  "anio": 2022,
  "valor": 18000.00,
  "color": "Rojo",
  "extras": "Aire acondicionado, Bluetooth",
  "estado": "NUEVO"
}
```

> 📌 **Nota:** Cambiar el campo que se desea actualizar.

### 🔹 Eliminar vehículo

```
DELETE http://localhost:8080/v1/vehiculos/{id}
```

---

## 🧑‍💼 Vendedores

### 🔹 Listar todos

```
GET http://localhost:8080/v1/vendedores
```

### 🔹 Buscar por ID

```
GET http://localhost:8080/v1/vendedores/{id}
```

### 🔹 Buscar por email

```
GET http://localhost:8080/v1/vendedores/email/{email}
```

### 🔹 Buscar por concesionario

```
GET http://localhost:8080/v1/vendedores/concesionario/{idConcesionario}
```

### 🔹 Buscar por estado

```
GET http://localhost:8080/v1/vendedores/estado/{estado}
```

### 🔹 Crear vendedor

```
POST http://localhost:8080/v1/vendedores
```

```json
{
  "idConcesionario": 2,
  "nombre": "Juan Pérez",
  "telefono": "0988888888",
  "email": "juan.perez@ejemplo.com",
  "estado": "ACTIVO"
}
```

### 🔹 Actualizar vendedor

```
PUT http://localhost:8080/v1/vendedores/{id}
```

```json
{
  "idConcesionario": 2,
  "nombre": "Juan Pérez",
  "telefono": "0988888888",
  "email": "juan.perez@ejemplo.com",
  "estado": "ACTIVO"
}
```

> 📌 **Nota:** Cambiar el campo que se desea actualizar.

### 🔹 Desactivar vendedor

```
PATCH http://localhost:8080/v1/vendedores/{id}/desactivar
```

---

## 📋 Auditorías

### 🔹 Listar todas

```
GET http://localhost:8080/v1/auditorias
```

### 🔹 Buscar por ID

```
GET http://localhost:8080/v1/auditorias/{id}
```

### 🔹 Buscar por tabla

```
GET http://localhost:8080/v1/auditorias/tabla/{tabla}
```

### 🔹 Buscar por acción

```
GET http://localhost:8080/v1/auditorias/accion/{accion}
```

### 🔹 Buscar por rango de fechas

```
GET http://localhost:8080/v1/auditorias/fechas?desde=YYYY-MM-DDTHH:MM:SS&hasta=YYYY-MM-DDTHH:MM:SS
```

#### 📌 Ejemplo:

```
GET http://localhost:8080/v1/auditorias/fechas?desde=2025-06-22T21:40:00&hasta=2025-06-22T22:00:00
```

### 🔹 Crear auditoría

```
POST http://localhost:8080/v1/auditorias
```

```json
{
  "tabla": "vehiculos",
  "accion": "INSERT",
  "fechaHora": "2024-06-01T12:00:00"
}
```
## 1. ClienteProspectoController

### Registrar un Cliente Prospecto (POST)
- **URL:** `POST /clientes-prospectos`
- **Body (JSON):**
```json
{
  "cedula": "0123456789",
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan.perez@email.com",
  "telefono": "0999999999",
  "direccion": "Av. Siempre Viva 123",
  "actividadEconomica": "Empleado",
  "ingresos": 1200.00,
  "egresos": 400.00,
  "estado": "PROSPECTO"
}
```

### Obtener todos los clientes prospecto (GET)
- **URL:** `GET /clientes-prospectos`


### Obtener cliente prospecto por cédula (GET)
- **URL:** `GET /clientes-prospectos/cedula/{cedula}`

### Validar información de un cliente (GET)
- **URL:** `GET /clientes-prospectos/validar/{cedula}`

### Actualizar información financiera de un cliente prospecto (PUT)
- **URL:** `PUT /clientes-prospectos/{id}/financiera?ingresos=1500&egresos=500`

### Clasificar cliente (GET)
- **URL:** `GET /clientes-prospectos/{cedula}/clasificar`

### Ver historial del cliente (GET)
- **URL:** `GET /clientes-prospectos/{cedula}/historial`

### Validar capacidad financiera (GET)
- **URL:** `GET /clientes-prospectos/{cedula}/capacidad-financiera?cuotaProyectada=300`

---

## 2. SolicitudCreditoController

### Listar todas las solicitudes de crédito (GET)
- **URL:** `GET /solicitudes-credito`

### Crear solicitud de crédito (POST)
- **URL:** `POST /solicitudes-credito`
- **Body (JSON):**
```json
{
  "idClienteProspecto": 1,
  "idVehiculo": 1,
  "entrada": 2000.00,
  "plazoMeses": 36,
  "scoreInterno": 750.00,
  "scoreExterno": 700.00,
  "idVendedor": 1
}
```

### Obtener solicitud por número (GET)
- **URL:** `GET /solicitudes-credito/numero/{numeroSolicitud}`

### Listar solicitudes por cliente (GET)
- **URL:** `GET /solicitudes-credito/cliente/{cedula}`

### Listar solicitudes por estado (GET)
- **URL:** `GET /solicitudes-credito/estado/{estado}`

### Simular crédito (GET)
- **URL:** `GET /solicitudes-credito/simular?idVehiculo=1&idClienteProspecto=1&plazoMaximo=36`

### Actualizar solicitud de crédito (PUT)
- **URL:** `PUT /solicitudes-credito/{id}`
- **Body (JSON):**
```json
{
  "idClienteProspecto": 1,
  "idVehiculo": 1,
  "entrada": 2500.00,
  "plazoMeses": 48,
  "scoreInterno": 750.00,
  "scoreExterno": 720.00,
  "idVendedor": 1
}
```

### Evaluar crédito automáticamente (POST)
- **URL:** `POST /solicitudes-credito/{id}/evaluar`

### Instrumentar crédito (POST)
- **URL:** `POST /solicitudes-credito/{id}/instrumentar`

---

## 3. DocumentacionController

### 1. Configurar y gestionar tipos de documento

#### Listar todos los tipos de documento (GET)
- **URL:** `GET /tipos-documentos`

#### Crear tipo de documento (POST)
- **URL:** `POST /tipos-documentos`
- **Body (JSON):**
```json
{
  "nombre": "Contrato",
  "descripcion": "Contrato obligatorio de crédito",
  "estado": "ACTIVO",
  "version": 0
}
```

#### Obtener tipos de documentos obligatorios (GET)
- **URL:** `GET /tipos-documentos/obligatorios`

#### Obtener tipos de documentos por categoría (GET)
- **URL:** `GET /tipos-documentos/categoria/{categoria}`

#### Activar tipo de documento (PATCH)
- **URL:** `PATCH /tipos-documentos/{id}/activar`

#### Desactivar tipo de documento (PATCH)
- **URL:** `PATCH /tipos-documentos/{id}/desactivar`

---

### 2. Gestionar documentos adjuntos

#### Listar todos los documentos adjuntos (GET)
- **URL:** `GET /documentos`

#### Cargar documento (POST)
- **URL:** `POST /documentos`
- **form-data:**
  - idSolicitud: 1
  - archivo: (adjuntar archivo PDF)
  - idTipoDocumento: 1

#### Registrar documentos firmados (POST)
- **URL:** `POST /documentos/firmados`
- **form-data:**
  - idSolicitud: 1
  - documentos: (adjuntar uno o varios archivos PDF)

#### Listar documentos por solicitud (GET)
- **URL:** `GET /documentos/solicitud/{idSolicitud}`

#### Obtener documento por ID (GET)
- **URL:** `GET /documentos/{idDocumento}`

#### Eliminar documento (DELETE)
- **URL:** `DELETE /documentos/{idDocumento}`

#### Verificar completitud documental (GET)
- **URL:** `GET /documentos/solicitud/{idSolicitud}/completitud`

---

