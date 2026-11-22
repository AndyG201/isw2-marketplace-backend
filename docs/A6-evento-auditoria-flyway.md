# A6 — Migracion Flyway V12: indice de evento_auditoria

## Alcance

La migracion `src/main/resources/db/migration/V12__evento_auditoria.sql` crea
`public.evento_auditoria`, tabla de **indice de eventos archivados en Backblaze B2**.
No debe confundirse con `public.bandeja_salida_auditoria`, creada en V1: esa
es la bandeja de salida antes de almacenar los eventos.

Se contrastaron los 13 campos con `AuditoriaIndiceJdbcRepository` (INSERT/ON
CONFLICT) y `AuditoriaConsultaJdbcRepository` (SELECT, filtros, ordenamiento).
Los tipos y longitudes se basan en `BandejaSalidaAuditoria` y en
`ResultadoAlmacenamientoAuditoria`. El identificador de usuario es opcional y
no se agrega FK para no perder trazabilidad si se elimina un usuario.

Incluye tres indices: paginacion temporal, filtros por usuario/fecha y
filtros por accion/fecha. El hash SHA-256 se valida como 64 caracteres hex
minusculos, tal como lo genera `BackblazeAuditoriaAdapter`, y solo se acepta
retencion `COMPLIANCE`, como exige `AuditoriaIndiceJdbcRepository`.

## 1. Crear rama e instalar

En CMD de Windows, desde la raiz del proyecto:

```cmd
cd /d C:\Users\Anderson\Desktop\ProyectoISW2\marketplace
git switch -c feat/evento-auditoria-flyway
powershell -NoProfile -Command "Expand-Archive -LiteralPath (Join-Path $env:USERPROFILE 'Downloads\A6-evento-auditoria-flyway.zip') -DestinationPath '.' -Force"
dir src\main\resources\db\migration\V12__evento_auditoria.sql
```

No hay que cambiar el `pom.xml`, el perfil `docker`, las clases Java ni las
migraciones V1–V11. Este paquete solo agrega tres archivos nuevos.

## 2. Aplicar en la base A5/A4 sin borrar datos

Con Docker Desktop encendido y tus servicios actuales activos:

```cmd
docker compose --env-file .env.docker -p marketplace-a3 up -d --build --no-deps api worker
```

El arranque reconstruye el WAR desde `src/main` y Flyway aplica V12.
No ejecutes `docker compose down -v`, `flyway clean`, `DROP` ni `TRUNCATE`.

## 3. Validacion

```cmd
docker compose --env-file .env.docker -p marketplace-a3 ps
docker compose --env-file .env.docker -p marketplace-a3 logs --tail 80 api
```

En los logs debe aparecer la migracion a la version `12 - evento auditoria`
y `Successfully applied 1 migration` (puede variar el mensaje si ambos
procesos arrancan simultaneamente). Si la base ya tiene V12, debe indicar
que el esquema esta al dia.

Para comprobar que la tabla acepta INSERT/SELECT sin conservar registros de
prueba, desde **CMD** (no PowerShell):

```cmd
docker compose --env-file .env.docker -p marketplace-a3 exec -T postgres sh -c "psql -U $POSTGRES_USER -d $POSTGRES_DB -v ON_ERROR_STOP=1" < deploy\auditoria\verificar-a6.sql
```

El comando debe terminar con:

```text
[A6] OK: V12 presente, 13 columnas e insercion/consulta correctas. No quedaron datos de prueba.
```

En caso de problema, no repitas una migracion editando su contenido: envia
el error y revisamos el estado en `flyway_schema_history`. El validador de
SQL hace `ROLLBACK` al final; no inserta eventos permanentes.

Nota: la tabla vacia es normal mientras no se habilite la integracion B2
con configuracion y credenciales validas. `B2_ENABLED=false` por defecto
impide activar su worker de almacenamiento.

## 4. Commit y push (solo despues de validar)

```cmd
git add -- src/main/resources/db/migration/V12__evento_auditoria.sql deploy/auditoria/verificar-a6.sql docs/A6-evento-auditoria-flyway.md
git diff --cached --check
git diff --cached --stat
git commit -m "feat(db): crear indice de eventos de auditoria con Flyway V12"
git push -u origin feat/evento-auditoria-flyway
```

No se agregan los archivos modificados de `ErroresApiHandler`, ni los ZIP,
ni los respaldos en `backups/postgres`.

## Observaciones

- Este cambio **no habilita** Backblaze B2 ni verifica la ejecucion real de
  la integracion, solo el esquema usado por los repositorios.
- No se reemplazan ni reescriben migraciones antiguas.
- No se probaron datos en un PostgreSQL real durante la generacion del
  paquete; la verificacion SQL anterior esta preparada para ejecutarse en tu
  PostgreSQL 18.6 existente.
