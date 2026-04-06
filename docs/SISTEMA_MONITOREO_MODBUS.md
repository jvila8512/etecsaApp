# Sistema de Monitoreo de Equipos Modbus

## Arquitectura General

```
┌─────────────────────────────────────────────────────────────────────┐
│                    SCHEDULERS (Tareas programadas)                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  Scheduler RÁPIDO (500ms)                                           │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ Solo equipos con ALARMAS BOOLEANAS                          │   │
│  │ • Detecta alarmas booleanas al instante                    │   │
│  │ • Envía a /topic/dashboard/actualizaciones                 │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  Scheduler LENTO (2 segundos)                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ TODOS los equipos                                            │   │
│  │ • Lee estado (CONECTADO/DESCONECTADO)                       │   │
│  │ • Lee variables numéricas                                   │   │
│  │ • Envía a /topic/dashboard/estado                           │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## Flujo de Lectura de Datos

### 1. PollingService (procesarEquipo)

```java
// 1. Conectar al PLC
boolean ok = modBusService.connectToGenerator(genId, ip);

// 2. Si falla → DESCONECTADO
if (!ok) {
    eqDto.setEstado("DESCONECTADO");
    return;
}

// 3. Si éxito → OPERATIVO
eqDto.setEstado("OPERATIVO");

// 4. Leer cada variable según su tipo:
switch (ev.getTipoRegistro()) {
    case BIT_LOGICO_M:     // %MX → Booleano
        Boolean val = modBusService.readM(genId, dir);
    case PALABRA_MW:       // %MW → 16 bits
        Integer raw = modBusService.readMW(genId, dir);
    case PALABRA_DOBLE_MD: // %MD → 32 bits
        Long raw = modBusService.readMD(genId, dir);
}

// 5. Aplicar transformaciones:
//    - TipoDato: INT, FLOAT, etc.
//    - ScalingFactor: multiplicador

// 6. Guardar en BD
// 7. Detectar alarmas
// 8. Enviar por WebSocket
```

### 2. Tipos de Variables Soportadas

| Tipo Registro    | Dirección    | Tipo Dato      | Descripción         |
| ---------------- | ------------ | -------------- | ------------------- |
| BIT_LOGICO_M     | %MX (0-9999) | BOOLEAN        | Variables booleanas |
| PALABRA_MW       | %MW (0-9999) | INT16, UINT16  | 16 bits             |
| PALABRA_DOBLE_MD | %MD (0-9999) | INT32, FLOAT32 | 32 bits             |

## Sistema de Alarmas

### Detección

```java
// En PollingService.detectarAlarma()

// Condiciones:
// 1. habilitarAlarma = true
// 2. Booleanas: valor = true → alarma
// 3. Numéricas: valor > umbral → alarma

// Throttle: mínimo 1 segundo entre detecciones
// para evitar duplicados

```

### Flujo de Alarmas

```
PollingService.detectarAlarma()
    │
    ▼
AlarmaService.procesarDeteccion()
    │
    ├── ¿Ya existe alarma ACTIVA/RECONOCIDA?
    │   └── Sí → No crear (evitar duplicados)
    │
    ├── No → Crear alarma en BD
    │
    └── Notificar por WebSocket
        └── /topic/alarmas/nueva
```

### Estados de Alarma

```
ACTIVA    → Detectada, no reconocida
RECONOCIDA → Usuario reconoció (ACK)
FINALIZADA → Usuario resolvió
```

### Acciones

- **Reconocer** (ACK): ACTIVA → RECONOCIDA
- **Resolver**: RECONOCIDA → FINALIZADA

## Intervalos de Lectura

| Tipo                 | Condición      | Intervalo      |
| -------------------- | -------------- | -------------- |
| Booleanas con alarma | umbral != null | 500ms (1 tick) |
| Numéricas con alarma | umbral != null | 2 segundos     |
| Equipos críticos     | critico = true | 3 segundos     |
| Por defecto          | -              | 10 segundos    |

## Configuraciones Importantes

### ModBusService.java

```java
private static final long MIN_CONNECT_INTERVAL_MS = 3000; // 3 segundos entre intentos

```

### WebSocket Topics

| Topic                            | Frecuencia  | Contenido                                            |
| -------------------------------- | ----------- | ---------------------------------------------------- |
| /topic/dashboard/actualizaciones | 500ms       | Alarmas + variables de equipos con alarmas booleanas |
| /topic/dashboard/estado          | 2s          | Estado de todos los equipos                          |
| /topic/dashboard/completo        | Manual      | Todos los equipos + todas las variables              |
| /topic/alarmas/nueva             | Instantáneo | Nueva alarma detectada                               |
| /topic/alarmas/reconoci          | Instantáneo | Alarma reconocida                                    |
| /topic/alarmas/resuelta          | Instantáneo | Alarma resuelta                                      |

## Límites del Sistema

- **Equipos máximos**: 200
- **Variables por equipo**: 20
- **Total variables**: 4,000

## Modelo de Datos

### Equipo

- id, nombre, direccionIp, critico, estado, intervaloBase

### EventoEquipo (Variable)

- id, nombreVariable, direccionModbus, tipoRegistro, tipoDato
- valorBooleano, valorNumerico, umbralAlerta, habilitarAlarma
- severidadAlerta, intervaloLectura, esEscribible

### Alarma

- id, descripcion, severidad, estado, evento_id, acknowledgedBy

---

_Documento generado para referencia del sistema de monitoreo Modbus_
