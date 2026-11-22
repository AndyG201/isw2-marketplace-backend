# A5 - Respaldos automaticos de PostgreSQL (ISW2 Marketplace)

## Alcance

Se agrega **solo** el servicio `postgres-backup`, sin modificar la API, el worker, Redis, Flyway, autoheal ni el volumen `marketplace_postgres`.

- Imagen de cliente `postgres:18.6`, compatible con el servidor del Compose.
- `pg_dump` diario en formato **custom** (`.dump`), utilizable con `pg_restore`.
- Al iniciarse, realiza una copia si no existe otra copia exitosa del mismo dia. Los dias siguientes, la realiza despues de las **03:00 hora Colombia** (si falla, reintenta cada 15 minutos). Si Docker Desktop estuvo apagado, crea la copia pendiente cuando vuelva a arrancar.
- **Retencion de 7 dias** por defecto. Solo borra respaldos con nombre `marketplace_*.dump` que superan la retencion y **solo despues** de crear y verificar preliminarmente uno nuevo.
- Archivo `*.sha256` para cada copia y validacion inicial mediante `pg_restore --list`. **Esa comprobacion no sustituye una restauracion de prueba.**
- Los archivos quedan en `marketplace\backups\postgres` (carpeta local del proyecto, fuera del volumen de la base). `backups/.gitignore` evita subir copias de datos a Git.
- El contenedor usa el usuario `postgres`; no recibe los secretos JWT, SMTP u otros que no necesita.
- Healthcheck: verifica que el ultimo respaldo exitoso no tenga mas de 25 horas. No esta controlado por `autoheal` porque un error de copia se debe diagnosticar, no reiniciarse a ciegas.

## 1. Instalar desde CMD en Windows

Situate en la raiz de `marketplace` y crea una nueva rama **solo una vez**:

```cmd
cd /d C:\Users\Anderson\Desktop\ProyectoISW2\marketplace
git switch -c feat/postgres-backups
```

Descarga `A5-backups-postgresql.zip` en tu carpeta `Descargas` y extraelo:

```cmd
powershell -NoProfile -Command "Expand-Archive -LiteralPath '$env:USERPROFILE\Downloads\A5-backups-postgresql.zip' -DestinationPath '.' -Force"
```

Se actualiza `compose.yaml` y se agregan los archivos `deploy/backup/Dockerfile`, `deploy/backup/backup-postgres.sh`, `backups/.gitignore` y este documento.

## 2. Activar SOLO el respaldo (sin reiniciar los otros servicios)

```cmd
if not exist backups\postgres mkdir backups\postgres
docker compose --env-file .env.docker -p marketplace-a3 config --quiet
docker compose --env-file .env.docker -p marketplace-a3 up -d --no-deps --build postgres-backup
```

No es necesario detener API, worker, PostgreSQL ni Redis; este nuevo contenedor se conecta por la red de Compose existente.

## 3. Confirmar el resultado (sin ejecutar pruebas destructivas)

```cmd
docker compose --env-file .env.docker -p marketplace-a3 ps
docker compose --env-file .env.docker -p marketplace-a3 logs --tail 40 postgres-backup
dir backups\postgres
```

El log debe mostrar `[backup] OK: marketplace_...dump`. En `backups\postgres` debe aparecer un archivo `.dump` y su `.sha256`. Si tarda en aparecer, consulta de nuevo los logs: el primer respaldo depende del tamano de la base. `health: starting` es normal al principio.

**Opcional (solo si quieres otra copia inmediatamente):**

```cmd
docker compose --env-file .env.docker -p marketplace-a3 run --rm --no-deps postgres-backup once
```

Esto crea un nuevo `.dump`, no restaura ni elimina datos.

## 4. Guardar solo A5 en GitHub

Antes del commit, comprueba que no haya otros cambios preparados en el area de staging:

```cmd
git add -- compose.yaml deploy/backup/Dockerfile deploy/backup/backup-postgres.sh backups/.gitignore docs/A5-backups-postgresql.md
git diff --cached --check
git diff --cached --stat
```

Si el `stat` muestra exclusivamente esos cinco archivos:

```cmd
git commit -m "feat(infra): respaldos diarios de PostgreSQL con retencion"
git push -u origin feat/postgres-backups
```

No subas `.env.docker`, ningun `.dump` ni ningun `.sha256`. Las modificaciones locales anteriores de `ErroresApiHandler` y sus pruebas no deben entrar en este commit.

## Importante antes de produccion

- Los respaldos estan en **la misma computadora** que Docker Desktop. No protegen de perdida o robo de ese equipo: copia periodicamente el directorio a otro dispositivo o almacenamiento cifrado con control de acceso.
- Los `.dump` contienen datos reales potencialmente personales. No se cifran automaticamente; controla acceso, distribucion y almacenamiento.
- Este dump respalda la **base de datos `marketplace`**, no los archivos de `uploads`, `reportes`, ni roles globales de todo el servidor. Esos requieren una politica de backup adicional.
- La retencion borra copias viejas despues de un respaldo exitoso. Puedes cambiar `BACKUP_RETENTION_DAYS` (1..365) y `BACKUP_HOUR` (0..23) en `.env.docker` y recrear solo el respaldo. Sin configuracion, usa 7 dias y 03:00.
- Si Docker Desktop o la computadora estan apagados a las 03:00, no puede hacer copias mientras estan apagados. Al volver a iniciar, se realiza una copia si no existe una del dia.
- Probar restauraciones de forma periodica **solo contra una base vacia de ensayo**, nunca contra la base actual. `pg_restore --list` comprueba que el archivo sea reconocible, no garantiza que todos los datos se restauren.
- Si el log indica `Permission denied` al escribir `backups/postgres`, revisa los permisos de esa carpeta para el usuario del contenedor `postgres`. No ejecutes `docker compose down -v`: elimina los volúmenes de datos.
