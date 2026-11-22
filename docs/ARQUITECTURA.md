# Arquitectura del backend

El backend es un **monolito modular en capas**: una sola aplicación Spring Boot (Java 22, Spring Boot 3.4.5, empaquetado WAR)
dividida en módulos por funcionalidad, y cada módulo organizado en capas. Dos herramientas vigilan la estructura en cada
ejecución de las pruebas, de modo que no depende de la disciplina de quien programa.

## 1. Módulos

Cada paquete directo de `co.edu.unbosque.marketplace` es un módulo:

`alojamiento`, `auditoria`, `cancelacion`, `cotizacion`, `disponibilidad`, `notificacion`, `pago`, `publicacion`, `reporte`,
`resena`, `reserva`, `servicio`, `tarifa`, `trabajo`, `ubicacion` y `usuario`.

Además hay dos paquetes transversales:

- `compartido`: las excepciones de dominio y la interfaz base de los repositorios (`RepositorioBasico`).
- `config`: configuración común, entre ella `ErroresApiHandler`, que traduce las excepciones de dominio a HTTP.

**Regla entre módulos:** un módulo solo usa a otro a través de su paquete `contracts` (interfaces de consulta o de comando,
eventos y objetos de intercambio). Spring Modulith lo verifica en `ArquitecturaModularTest`.

## 2. Capas dentro de cada módulo

| Capa | Paquete | Qué contiene |
|------|---------|--------------|
| Presentación | `api` | Controladores REST y sus solicitudes/respuestas. |
| Aplicación | `application` | Casos de uso (servicios), DTO y **puertos** (`application/port`). |
| Dominio | `domain` | Entidades y reglas del negocio. |
| Infraestructura | `infrastructure` | Adaptadores: persistencia, Stripe, Backblaze, Cloudinary, correo, caché. |
| Contratos | `contracts` | Lo único que otros módulos pueden usar. |

Las dependencias van hacia adentro: `api → application → domain`, y la infraestructura implementa los puertos de la aplicación.

### Reglas que se verifican automáticamente (`ArquitecturaCapasTest`)

1. El dominio no depende de `api`, `application` ni `infrastructure`.
2. La aplicación no depende de la presentación.
3. La aplicación no depende de la infraestructura (usa puertos).
4. La infraestructura no depende de la presentación.
5. La presentación no depende de la infraestructura.
6. La aplicación y la presentación no lanzan `IllegalArgumentException` ni `IllegalStateException`: lanzan excepciones de dominio.
7. La aplicación no depende de Spring Data (`org.springframework.data.jpa` ni `.repository`).
8. La aplicación no usa JDBC directamente.
9. La aplicación no usa clientes HTTP (`java.net.http`): las integraciones externas son adaptadores de infraestructura.
10. La aplicación no depende de Redis (`org.springframework.data.redis`) ni de JPA (`jakarta.persistence`): se usan detrás de un puerto.

## 3. Repositorios: puertos y adaptadores

- El puerto es una interfaz pura en `application/port`, por ejemplo `PublicacionRepository`. Extiende `RepositorioBasico<E, ID>`
  (`save`, `findById`, `findAll`, ...) y declara solo las consultas propias del módulo.
- La implementación es una interfaz Spring Data en `infrastructure/persistence`, por ejemplo `PublicacionRepositoryJpa`,
  que extiende el puerto y conserva las anotaciones `@Query`, `@Lock` y `@Modifying`.
- Las consultas SQL directas (candado de idempotencia, reclamo de reembolsos, ocupación de noches) viven en adaptadores JDBC
  de infraestructura detrás de un puerto.
- Los datos temporales de `usuario` (códigos de verificación y recuperación, contadores de intentos, esperas de reenvío) pasan por
  el puerto `AlmacenTemporal`, implementado con Redis en `usuario/infrastructure/redis`.
- Releer una reserva tras la llamada a Stripe pasa por el puerto `RefrescoDeReserva`, implementado con JPA en `reserva/infrastructure/persistence`.

### Estados y transiciones

- `EstadoReserva` (`reserva/contracts`) y `EstadoPublicacion` (`publicacion/contracts`) son los únicos lugares donde se definen los estados y
  qué transiciones están permitidas. El nombre de cada constante es el texto guardado en la base de datos; la columna sigue siendo
  texto y una prueba de integración compara los enums con las restricciones `chk_reserva_estado` y `chk_publicacion_estado`.
- Las entidades `Reserva` y `Publicacion` cambian de estado con `cambiarEstado(...)`, que consulta la tabla y lanza
  `ConflictoNegocioException` (409) si la transición no existe. Los servicios consultan con `estaEn(...)`, no con textos.
- Los conjuntos de negocio (por ejemplo `EstadoReserva.CANCELABLES` o `QUE_OCUPAN_NOCHES`) también viven en el enum; el SQL escrito a mano
  los arma con `EstadoReserva.enSql(...)`. Los textos que quedan dentro de anotaciones `@Query` (constantes de compilación) los revisa
  `EstadosCoincidenConLaBaseDeDatosIntegracionTest`.
- `reserva` lee el estado de la publicación como texto (`"ACTIVA"`) en dos comparaciones: importar `EstadoPublicacion` crearía un
  ciclo entre módulos (`publicacion` ya depende de `reserva`), que Spring Modulith no permite.

## 4. Errores y códigos HTTP

Los servicios lanzan excepciones de `compartido.domain` y `ErroresApiHandler` las convierte en `{"mensaje": "..."}`:

| Excepción | HTTP |
|-----------|------|
| `DatosInvalidosException` | 400 |
| `SesionRequeridaException` | 401 |
| `AccesoNoPermitidoException` | 403 |
| `RecursoNoEncontradoException` | 404 |
| `ConflictoNegocioException` | 409 |
| `PlazoVencidoException` | 410 |
| `FallaInternaException` | 500 |
| `ServicioNoDisponibleException` | 503 |

## 5. Procesos en segundo plano y consistencia

- Los procesos periódicos corren solo con el perfil `worker` (conciliación de pagos, expiración de retenciones, reembolsos,
  envío de correos, copia de auditoría, exportación de reportes). **En producción hay que activarlo**; sin él esos procesos no se ejecutan.
- Correo de verificación (registro y reenvío del código): patrón *transactional outbox* (`correo_pendiente`), con el código cifrado, reintentos y reclamo por lease.
  Los correos de recuperación de contraseña y de cambio de correo se siguen enviando directo, porque la persona espera la respuesta en la misma petición.
- Reembolsos de Stripe: tabla `reembolso_pago` con clave de idempotencia, reintentos y recuperación de trabajos atascados.
- Reservas: las noches se retienen en `noche_reservada` bajo un candado por alojamiento; una retención vencida se libera al
  retener de nuevo, sin esperar al proceso automático.
- La base de datos se versiona con Flyway (`src/main/resources/db/migration`).
- Búsqueda por ciudad sin tildes: la columna generada `alojamiento.ciudad_normalizada` tiene un índice de trigramas (`pg_trgm`, migración V11);
  con 100 000 alojamientos la consulta pasó de 111 ms (recorrido completo) a 4 ms (uso del índice).

### Pasarela de pago simulada y versiones en memoria

- `PagoSandboxGateway` tiene dos implementaciones: Stripe (`PagoSandboxAdapter`, perfil por defecto) y `PagoSimuladoGateway`, que vive en
  memoria y se activa con el perfil `pago-simulado` (por ejemplo `SPRING_PROFILES_ACTIVE=worker,pago-simulado`). Es idempotente por
  clave, aprueba los pagos y rechaza los de monto con `.13` de decimales para poder probar el rechazo. No usa red ni claves.
- `CacheBusquedaPublicaciones`, `ColaTrabajos` y `AlmacenTemporal` tienen versiones en memoria en `src/test` (paquete `doble`). Una prueba de
  contrato por puerto se ejecuta contra la versión en memoria y contra la real con Redis, para que no se parezcan solo en apariencia.
  Son dobles de prueba: no se despliegan.

## 6. Concesiones conocidas

Estas dependencias técnicas siguen dentro de las capas y están aceptadas por ahora:

- `MultipartFile` en la capa de aplicación (3 clases de `publicacion`): las imágenes llegan así desde el formulario.
- Las entidades del dominio llevan anotaciones JPA (29 clases) y `Usuario` implementa `UserDetails` de Spring Security.
- Algunos servicios usan Jackson (`ObjectMapper`) para serializar el detalle de auditoría, y los puertos de dos repositorios
  usan los tipos de valor `Sort`/`Pageable` de Spring Data.
- `JwtUtil` (infraestructura de `usuario`) usa Redis directamente para invalidar sesiones.
- Los adaptadores de infraestructura de Backblaze, Cloudinary, JWT y cifrado lanzan `IllegalStateException` para fallas
  internas (siempre 500); la regla 6 no los cubre.

## 7. Pruebas, cobertura y CI

- `./mvnw clean verify` ejecuta las pruebas y genera el informe JaCoCo en `target/site/jacoco/jacoco.xml`.
  Las pruebas de integración necesitan PostgreSQL 18 y Redis 7 (la CI los levanta como servicios).
- Estado medido tras la última integración: **1844 pruebas**, cobertura total de **85,6 % de líneas** y **73,8 % de ramas**.
  Los módulos con menor cobertura de líneas son `servicio` (24 %), `reporte` (47 %) y `config` (62 %).
- Pruebas de carga: `BusquedaConcurrenciaIntegracionTest` (en la suite) lanza búsquedas simultáneas contra la aplicación completa, y
  `pruebas-carga/busqueda.js` (k6, se ejecuta a mano contra un servidor) mide tiempos con umbrales; ver `pruebas-carga/README.md`.
- GitHub Actions (`.github/workflows/backend-ci.yml`) compila con Java 22, corre las pruebas, verifica que exista el informe
  JaCoCo, lo envía a SonarCloud y publica los informes como artefacto.
- SonarCloud exige en código nuevo: cobertura mínima y duplicación máxima del 3 %. Por eso los PR agregan pruebas para el código que cambian.
