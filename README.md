# TallerRepair

TallerRepair es una aplicación de escritorio profesional para gestionar talleres de reparación y servicios técnicos. La solución está diseñada con Java 17 + JavaFX, arquitectura por capas y enfoque multi-plataforma para Windows y Linux.

## Descripción

La aplicación está pensada para manejar:

- clientes
- equipos
- órdenes de servicio
- diagnósticos
- presupuestos
- pagos
- inventario
- ventas
- caja
- reportes
- usuarios y permisos

La implementación actual corresponde a la Fase 1 del proyecto: base del proyecto, estructura modular, navegación principal y shell general de la aplicación.

## Requisitos

- Java 17 LTS
- Maven 3.9+
- Git
- Sistema operativo: Windows 10/11 o Linux 64-bit

## Estructura principal

```text
src/
└── main/
    ├── java/
    │   └── com/tallerrepair/tallerrepair/
    │       ├── Main.java
    │       ├── Launcher.java
    │       ├── config/
    │       ├── controller/
    │       ├── util/
    │       └── ...
    └── resources/
        ├── css/
        ├── fxml/
        └── ...
```

## Configuración

La aplicación usa una carpeta de trabajo local del usuario dentro del directorio principal del sistema:

- Linux/macOS: `~/.tallerrepair`
- Windows: `%USERPROFILE%\.tallerrepair`

Dentro de esa carpeta se reservan subdirectorios para:

- configuración
- logs
- fotos
- documentos
- backups

## Ejecutar la aplicación

### Linux / macOS

```bash
./mvnw clean javafx:run
```

### Windows

```powershell
mvnw.cmd clean javafx:run
```

## Compilar

```bash
./mvnw clean package
```

## Fase actual

La aplicación incluye en esta etapa:

- proyecto Maven con Java 17
- JavaFX configurado
- estructura por paquetes
- navegación lateral con módulos principales
- header con título del módulo activo
- área central de contenido
- CSS base profesional
- configuración inicial de rutas locales

## Siguientes fases

La siguiente etapa será la capa de persistencia con:

- SQLite
- JPA/Hibernate
- entidades base
- configuración de base de datos
- repositorios
- migración inicial

## Nota

Este proyecto sigue una arquitectura preparada para continuar con más módulos, capas de servicios, autenticación, base de datos y expansión futura hacia APIs y sincronización.
