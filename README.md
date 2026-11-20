# Autogest

Aplicación web para gestionar un taller automotriz: vehículos, reparaciones, repuestos y consulta pública por placa.

## Integrantes

- Sarita Toloza Buitrago
- Sandra Carolina Espinosa Rojas

## Problema

Los talleres manejan sus datos de forma manual y dispersa. Autogest los centraliza en una sola aplicación con acceso por rol.

## Funcionalidades obligatorias asignadas

| Tipo | Funcionalidad | Dónde está en Autogest |
|---|---|---|
| Implementación | Subida de archivos | Imagen de cada repuesto (JPG, PNG o WEBP, máximo 2 MB), guardada en `uploads/spare-parts/` |
| Implementación | Mapas geográficos | Ubicación del cliente en órdenes a domicilio, con Leaflet y OpenStreetMap |
| UX | Notificaciones toast | Aviso de éxito o error en cada acción, en todos los módulos |
| UX | Tabla editable | Edición de una fila directamente en repuestos y modelos de automóvil |

## Otras funcionalidades

- Inicio de sesión con roles: Administrador, Operador y Mecánico.
- CRUD de modelos de automóvil, vehículos (placa colombiana con su color) y repuestos (imagen, EAN, stock, modelos compatibles).
- Órdenes de reparación con mecánico asignado y flujo de estados: Pendiente, En progreso, Completada, Entregada o Cancelada.
- Servicio a domicilio con ubicación marcada en un mapa.
- El mecánico registra los repuestos usados; se valida compatibilidad y se descuenta el stock.
- Portal público: el cliente consulta su reparación con la placa, sin iniciar sesión.
- Notificaciones Toast en cada acción y tablas editables en repuestos y modelos.

## Roles

| Rol | Acceso |
|---|---|
| Cliente (sin sesión) | Consulta por placa |
| Operador | Vehículos y órdenes de reparación |
| Mecánico | Solo sus órdenes asignadas y repuestos compatibles |
| Administrador | Todo, incluidos usuarios, modelos y repuestos |

## Tecnologías

- Java, Spring Boot 4.1.0 (MVC, Data JPA, Security)
- Thymeleaf + HTMX
- HTML, CSS y JavaScript
- Leaflet + OpenStreetMap
- H2 en memoria (solo desarrollo)
- Gradle (wrapper incluido)

## Arquitectura

Capas: Controller, Service, Repository, Entity. El servidor genera el HTML y HTMX actualiza partes de la página sin recargarla.

```
src/main/java/.../autogest/
  config/ controller/ service/ repository/ model/(entity, dto, constant)
src/main/resources/
  templates/ static/ application.properties data.sql
```

## Cómo ejecutar

**Desde IntelliJ IDEA:**

1. Abrir la carpeta del proyecto como proyecto Gradle.
2. Crear una configuración de tipo Gradle con el nombre `Spring Boot Run`, tarea `bootRun` y proyecto `autogest`.
3. Pulsar Run.

Abrir http://localhost:8080

## Usuario de prueba

Administrador: `admin` / `1234` (solo desarrollo). Desde ahí se crean operadores y mecánicos.
