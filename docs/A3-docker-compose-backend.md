# A3 - Docker Compose para API, worker, PostgreSQL y Redis

## Alcance

Solo backend. Utiliza el Dockerfile, `application-docker.yaml` y `entrypoint.sh` de A2; no los modifica.
No configura autoheal (A4), backups (A5) ni frontend.

## Requisitos

- Docker Desktop con motor Linux y Compose v2.
- A1 + A2 aplicados; ejecutable `mvnw` y `Dockerfile` en la raiz.
- `.env.docker` preparado en A2 con `JWT_SECRET`, `DOCUMENTO_CLAVE_CIFRADO`,
  `DOCUMENTO_CLAVE_HMAC`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
  y configuraciones externas requeridas por tu instalacion.
- Mantener `.env.docker` fuera de Git. No copies secretos a `compose.yaml`.

## Preparacion desde CMD (raiz de marketplace)

```cmd
cd /d C:\Users\Anderson\Desktop\ProyectoISW2\marketplace
git switch -c feat/docker-compose-backend
git status -sb
docker compose version
docker compose --env-file .env.docker -p marketplace-a3 config --quiet
```

**Cuidado:** NO ejecutes `docker compose config` sin `--quiet`, ya que puede imprimir
credenciales. Revisa que `.env.docker` exista y sea ignorado:

```cmd
git check-ignore .env.docker
```

Debe mostrar `.env.docker`. Si no, no hagas commit de secretos.

## Levantar los cuatro servicios

```cmd
docker compose --env-file .env.docker -p marketplace-a3 up -d --build
docker compose --env-file .env.docker -p marketplace-a3 ps
docker compose --env-file .env.docker -p marketplace-a3 logs --tail 100 api worker
```

Se construye una imagen local `marketplace-backend:a3` que comparten API y worker.
El backend API queda accesible en `http://127.0.0.1:8082`. El worker NO expone
puerto al host. Los servidores Postgres y Redis tampoco publican puertos al host.

La API usa `SPRING_PROFILES_ACTIVE=docker` y el worker usa
`SPRING_PROFILES_ACTIVE=docker,worker`, **sin pasar `--worker`**.
Este detalle conserva el puerto HTTP interno para la sonda de liveness de A2.
Los trabajos `@Profile("worker")` solo se activan en ese segundo servicio.

**Importante:** se inicializa **otra base de datos, vacia**, distinta de la BD
local que usabas con A1 y A2. Flyway creara el esquema con las migraciones disponibles.
No se importan usuarios ni datos automaticamente. La primera inicializacion puede tardar.
La configuracion de PostgreSQL fija UTF-8 y `C.UTF-8` para evitar problemas de
normalizacion Unicode en bases nuevas.

La API y el worker leen los secretos de `.env.docker`, pero se sustituyen dentro
de Compose las direcciones locales por `postgres:5432` y `redis:6379`. Las
credenciales de esta base nueva provienen de `SPRING_DATASOURCE_USERNAME` y
`SPRING_DATASOURCE_PASSWORD`. Si cambias esas credenciales posteriormente,
no cambian las del volumen ya inicializado: requiere gestion SQL de usuarios,
no borrar el volumen para resolverlo.

## Validacion

```cmd
curl.exe -i http://127.0.0.1:8082/actuator/health
curl.exe -i http://127.0.0.1:8082/actuator/health/liveness
curl.exe -i http://127.0.0.1:8082/actuator/health/readiness
curl.exe -i http://127.0.0.1:8082/actuator/env
docker compose --env-file .env.docker -p marketplace-a3 ps
```

Esperado: las tres rutas de salud HTTP `200` y `UP`; `/actuator/env` HTTP `401`.
`api`, `worker`, `postgres` y `redis` deberian mostrarse `healthy`.
La sonda `liveness` del worker indica que responde; NO comprueba que los trabajos
hayan enviado un correo o procesado un reembolso. Para eso inspecciona logs
especificos y realiza pruebas funcionales aparte.

## Diagnostico

```cmd
docker compose --env-file .env.docker -p marketplace-a3 ps -a
docker compose --env-file .env.docker -p marketplace-a3 logs --tail 200 api
docker compose --env-file .env.docker -p marketplace-a3 logs --tail 200 worker
docker compose --env-file .env.docker -p marketplace-a3 logs --tail 100 postgres redis
```

No pegues logs que contengan claves o tokens sin ocultarlos antes.
Si el worker no arranca, comprueba su log y la linea que muestra perfiles activos.
Cualquier error de migracion Flyway debe corregirse ANTES de continuar a A4.
Los servicios solo se reinician automaticamente si termina su proceso; Compose
NO reinicia de forma automatica un proceso que este corriendo pero `unhealthy`.
Ese caso corresponde a A4.

## Parar sin borrar datos

```cmd
docker compose --env-file .env.docker -p marketplace-a3 down
```

`down` conserva los volumenes persistentes. **NO uses `down -v`**, que borra
PostgreSQL, Redis, uploads y reportes de esta instalacion. Estos volumenes
son nuevos e independientes de los directorios bind de A2.

Si dejaste el contenedor A2 funcionando, puede permanecer en `8081` sin conflicto.
Para apagarlo opcionalmente: `docker stop marketplace-api-a2`.

## Git (solo cuando todo funcione)

```cmd
git add -- compose.yaml docs/A3-docker-compose-backend.md
git diff --cached --check
git diff --cached --stat
git commit -m "feat(infra): orquestar backend con Docker Compose"
git push -u origin feat/docker-compose-backend
```

No incluyas los cambios previos de `ErroresApiHandler`, ZIPs, ni `.env.docker`.
