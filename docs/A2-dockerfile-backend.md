# A2. Imagen Docker del backend

Esta entrega construye desde fuentes una imagen de Marketplace con Java 22.0.2 y `marketplace.war`. Parte de A1, que aporta Actuator y sus reglas de seguridad.

## Contenido y rutas

Descomprime desde la carpeta que contiene `pom.xml`. Las rutas dentro de este ZIP empiezan directamente en la raiz del proyecto.

| Archivo | Funcion |
| --- | --- |
| `Dockerfile` | Compila con Maven Wrapper y ejecuta `marketplace.war` con un usuario sin privilegios de administrador. |
| `.dockerignore` | Limita los archivos enviados a Docker. Excluye configuracion local, claves, ZIP y `target`. |
| `src/main/resources/application-docker.yaml` | Configura conexiones, correo y claves mediante variables de entorno, al activar `docker`. |
| `deploy/api/entrypoint.sh` | Comprueba variables requeridas y arranca Java con `exec`, para recibir SIGTERM. |
| `deploy/api/Preparar-EntornoDocker.ps1` | Crea `.env.docker` desde tu configuracion actual. Conserva contrasenas y claves existentes. |
| `docs/A2-dockerfile-backend.md` | Instrucciones de aplicacion, prueba y cierre. |

`Dockerfile` y `.dockerignore` van junto a `pom.xml`. La configuracion de Spring va dentro de `src/main/resources`. Los scripts van dentro de `deploy/api`.

## 1. Aplicar desde Windows CMD

Abre Docker Desktop con contenedores Linux. Manten encendidos PostgreSQL y Redis que ya usaste para A1. A2 conecta con esos servicios existentes.

Ejecuta desde tu proyecto:

```cmd
cd /d C:\Users\Anderson\Desktop\ProyectoISW2\marketplace
git switch -c feat/dockerfile-backend
copy "%USERPROFILE%\Downloads\A2-dockerfile-backend.zip" .
powershell -NoProfile -Command "Expand-Archive -LiteralPath '.\A2-dockerfile-backend.zip' -DestinationPath '.' -Force"
git diff --check
docker version
docker info --format "{{.OSType}}"
```

`docker version` debe mostrar Client y Server. La ultima orden debe responder `linux`. Si ya creaste `feat/dockerfile-backend`, continua en esa rama sin repetir `git switch -c`.

La nueva rama parte de tu estado actual, que incluye A1. Tus modificaciones pendientes en `ErroresApiHandler` conservan su estado.

## 2. Preparar configuracion local

```cmd
powershell -NoProfile -ExecutionPolicy Bypass -File .\deploy\api\Preparar-EntornoDocker.ps1
git check-ignore .env.docker
```

La segunda orden debe mostrar `.env.docker`. Tu `.gitignore` recibido ya contiene `.env.*`.

La preparacion lee `src/main/resources/application.properties` y las configuraciones locales existentes. Las variables del proceso tienen prioridad. Reutiliza claves JWT, cifrado y HMAC, junto con las credenciales de PostgreSQL, Redis, SMTP, Stripe, Cloudinary y Backblaze que esten configuradas. No genera claves nuevas.

Las direcciones locales de PostgreSQL y Redis se adaptan a `host.docker.internal`. Conserva tu puerto y nombre de base. Las direcciones remotas conservan su valor. Este nombre conecta un contenedor con servicios del equipo que ejecuta Docker Desktop.

`.env.docker` contiene credenciales. Conserva este archivo en tu equipo, fuera de commits y archivos compartidos. Su sintaxis corresponde a `docker run --env-file`: cada linea tiene `NOMBRE=valor`, sin comillas agregadas por herramientas de Compose.

Si `.env.docker` ya existe, la preparacion termina sin sobrescribirlo. Para regenerarlo tras cambiar tu configuracion, guarda primero tu copia local y vuelve a ejecutar la preparacion:

```cmd
copy .env.docker .env.docker.respaldo
del .env.docker
powershell -NoProfile -ExecutionPolicy Bypass -File .\deploy\api\Preparar-EntornoDocker.ps1
```

Mantiene las mismas claves de cifrado y HMAC cuando uses tu base existente. Cambiarlas impediria recuperar o buscar datos protegidos previamente.

## 3. Construir la imagen

```cmd
docker build --progress=plain -t marketplace-backend:a2 .
```

Docker descarga Java 22 y ejecuta Maven Wrapper dentro de la etapa de compilacion. La etapa final recibe `marketplace.war`. No necesita una copia previa en `target` ni Maven instalado en Windows.

La construccion omite pruebas mediante `-Dmaven.test.skip=true`. Las pruebas requieren sus servicios de prueba y se ejecutan antes de publicar cambios. La compilacion de una imagen no reemplaza tus pruebas.

## 4. Arrancar la API

Esta comprobacion publica la API en `127.0.0.1:8081`. Tu proceso local en `8080` conserva su puerto. Dentro del contenedor Spring escucha en `8080`.

Las carpetas `uploads` y `reportes` se montan desde tu proyecto para conservar sus archivos al recrear este contenedor.

```cmd
if not exist uploads mkdir uploads
if not exist reportes mkdir reportes
docker run -d --name marketplace-api-a2 --env-file .env.docker -p 127.0.0.1:8081:8080 --mount "type=bind,source=%cd%\uploads,target=/app/uploads" --mount "type=bind,source=%cd%\reportes,target=/app/reportes" marketplace-backend:a2
docker logs --tail 80 marketplace-api-a2
```

Espera al mensaje `Started MarketplaceApplication` antes de consultar salud. Usa esta orden para seguir los mensajes durante su arranque:

```cmd
docker logs -f marketplace-api-a2
```

`Ctrl+C` cierra la visualizacion de logs. La API sigue ejecutandose.

## 5. Comprobar A2

```cmd
curl.exe -i http://localhost:8081/actuator/health
curl.exe -i http://localhost:8081/actuator/health/liveness
curl.exe -i http://localhost:8081/actuator/health/readiness
curl.exe -i http://localhost:8081/actuator/env
docker inspect --format "{{.State.Health.Status}}" marketplace-api-a2
```

Resultados esperados:

| Comprobacion | Resultado |
| --- | --- |
| `health` | HTTP 200 y `status: UP`. |
| `liveness` | HTTP 200 y `status: UP`. |
| `readiness` | HTTP 200 y `status: UP`, con PostgreSQL y Redis disponibles. |
| `env`, sin sesion | HTTP 401. |
| Estado de Docker | `healthy` tras una comprobacion satisfactoria. Inicialmente aparece `starting`. |

La imagen consulta liveness cada 30 segundos. Readiness comprueba dependencias externas. La imagen admite tres fallos de liveness y concede 180 segundos de arranque antes de contar fallos.

La configuracion `docker` mantiene las sondas de A1. No ejecuta tareas del perfil `worker`. La misma imagen admite `SPRING_PROFILES_ACTIVE=docker,worker` para la siguiente etapa. No pases `--worker`: tu `MarketplaceApplication` desactiva HTTP con ese argumento y perderia su sonda.

## 6. Diagnostico de esta prueba

```cmd
docker ps -a --filter "name=marketplace-api-a2"
docker logs --tail 150 marketplace-api-a2
docker inspect --format "{{json .State.Health}}" marketplace-api-a2
```

Si PostgreSQL rechaza la conexion, comprueba que tu servicio siga activo y admita conexiones desde Docker Desktop. Conserva tu puerto original en `.env.docker`. Si Redis falla, comprueba su servicio y sus credenciales. Un `503` de readiness requiere revisar esas conexiones.

No pegues `.env.docker` ni una salida completa de `docker inspect` en chats: la inspeccion completa contiene variables de entorno. Las ordenes anteriores consultan estado y salud.

Para detener la comprobacion:

```cmd
docker stop -t 40 marketplace-api-a2
```

Para recrearla despues de cambiar fuentes o configuracion:

```cmd
docker stop -t 40 marketplace-api-a2
docker rm marketplace-api-a2
docker build --progress=plain -t marketplace-backend:a2 .
```

Luego repite `docker run` de la seccion 4. `docker rm` elimina este contenedor de prueba. Las carpetas montadas conservan sus archivos.

## 7. Guardar A2 despues de comprobarlo

```cmd
git add -- Dockerfile .dockerignore src/main/resources/application-docker.yaml deploy/api/entrypoint.sh deploy/api/Preparar-EntornoDocker.ps1 docs/A2-dockerfile-backend.md
git diff --cached --check
git diff --cached --stat
git commit -m "feat(infra): agregar imagen Docker del backend"
git push -u origin feat/dockerfile-backend
```

La lista explicita limita este commit a los seis archivos de A2. La integracion a `main` se revisa cuando terminen estas comprobaciones.

## Validacion de preparacion

Comprobaciones realizadas sobre las fuentes recibidas con A1 y esta entrega:

- Empaquetado con Java 22.0.2 y Maven 3.10.0: `BUILD SUCCESS`.
- Inspeccion del WAR: incluye configuracion Docker y observabilidad, sin `application.properties` local.
- Arranque directo de ese WAR con perfil `docker`, PostgreSQL 18.6 y Redis 7.4.6 temporales. Usa credenciales sinteticas y una base aislada.
- Respuestas HTTP verificadas: `health`, `liveness` y `readiness` devuelven 200 y `UP`. `env` devuelve 401 sin sesion.
- Nueve escenarios del script de configuracion aprobados con PowerShell 7.4.6 y datos sinteticos. Incluyen claves conservadas, puertos, IPv6, destinos remotos, caracteres especiales, referencias a variables, campos ausentes y rechazo de sobrescritura.
- Sintaxis YAML y shell validada. ZIP revisado para excluir credenciales originales y evitar una carpeta `marketplace/` adicional.

Este entorno de preparacion carece de Docker Engine y Windows PowerShell 5.1. Por eso quedan pendientes `docker build` y `docker run` en tu equipo, incluida su conexion mediante Docker Desktop. La validacion del WAR no equivale a una ejecucion del contenedor. No se repitio la suite completa para esta entrega de infraestructura.

## Referencias

- Docker, compilacion por etapas: https://docs.docker.com/build/building/multi-stage/
- Docker, `run` y `--env-file`: https://docs.docker.com/reference/cli/docker/container/run/
- Docker Desktop, conexiones desde contenedores al equipo: https://docs.docker.com/desktop/features/networking/
- Imagen Java 22: https://hub.docker.com/_/eclipse-temurin/tags?name=22.0.2_9-jdk-jammy
