#!/bin/sh
# A5: pg_dump diario (formato custom), verificacion preliminar y retencion.
# No restaura, cambia ni elimina datos del servidor PostgreSQL.
set -eu
umask 077

BACKUP_DIR="${BACKUP_DIR:-/backups}"
BACKUP_RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-7}"
BACKUP_HOUR="${BACKUP_HOUR:-3}"
TZ="${TZ:-America/Bogota}"
export TZ

: "${PGHOST:?PGHOST es obligatorio}"
: "${PGPORT:?PGPORT es obligatorio}"
: "${PGDATABASE:?PGDATABASE es obligatorio}"
: "${PGUSER:?PGUSER es obligatorio}"
: "${PGPASSWORD:?PGPASSWORD es obligatorio}"
export PGPASSWORD

case "$BACKUP_RETENTION_DAYS" in ''|*[!0-9]*) echo '[backup] ERROR: BACKUP_RETENTION_DAYS debe ser un entero' >&2; exit 2;; esac
case "$BACKUP_HOUR" in ''|*[!0-9]*) echo '[backup] ERROR: BACKUP_HOUR debe ser un entero' >&2; exit 2;; esac
if [ "$BACKUP_RETENTION_DAYS" -lt 1 ] || [ "$BACKUP_RETENTION_DAYS" -gt 365 ]; then
  echo '[backup] ERROR: retencion fuera del rango 1..365 dias' >&2
  exit 2
fi
if [ "$BACKUP_HOUR" -gt 23 ]; then
  echo '[backup] ERROR: BACKUP_HOUR debe estar entre 0 y 23' >&2
  exit 2
fi

SUCCESS_DAY="$BACKUP_DIR/.last-success-day"
SUCCESS_EPOCH="$BACKUP_DIR/.last-success-epoch"
partial=''
cleanup() {
  if [ -n "$partial" ]; then rm -f -- "$partial"; fi
}
trap cleanup EXIT
trap 'exit 143' TERM
trap 'exit 130' INT

last_day() {
  if [ -r "$SUCCESS_DAY" ]; then cat "$SUCCESS_DAY"; fi
}

backup_once() {
  # Evitar tocar copias antiguas antes de crear y revisar una nueva.
  if ! mkdir -p "$BACKUP_DIR"; then
    echo '[backup] ERROR: no se puede crear directorio de destino' >&2
    return 1
  fi

  stamp="$(date +%Y%m%d_%H%M%S_%N)"
  filename="marketplace_${stamp}.dump"
  target="$BACKUP_DIR/$filename"
  partial="$target.incomplete"
  echo "[backup] Iniciando respaldo de ${PGDATABASE} a las $(date +%Y-%m-%dT%H:%M:%S%z)"

  if ! pg_dump --format=custom --no-password --file="$partial" "$PGDATABASE"; then
    echo '[backup] ERROR: pg_dump fallo. Se conservan todos los respaldos anteriores.' >&2
    cleanup
    partial=''
    return 1
  fi
  if [ ! -s "$partial" ] || ! pg_restore --list "$partial" >/dev/null 2>&1; then
    echo '[backup] ERROR: archivo vacio o no reconocible por pg_restore. No se aplica retencion.' >&2
    cleanup
    partial=''
    return 1
  fi
  if ! mv -- "$partial" "$target"; then
    echo '[backup] ERROR: no se pudo publicar el respaldo.' >&2
    cleanup
    partial=''
    return 1
  fi
  partial=''

  # Guardar checksum portatil (nombre de archivo relativo al directorio de copias).
  if ! (cd "$BACKUP_DIR" && sha256sum "$filename" > "$filename.sha256.tmp"); then
    echo '[backup] ERROR: no se pudo calcular el checksum; no se eliminan copias anteriores.' >&2
    rm -f -- "$target.sha256.tmp"
    return 1
  fi
  if ! mv -- "$target.sha256.tmp" "$target.sha256"; then
    echo '[backup] ERROR: no se pudo publicar el checksum; no se eliminan copias anteriores.' >&2
    rm -f -- "$target.sha256.tmp"
    return 1
  fi

  today="$(date +%Y-%m-%d)"
  epoch="$(date +%s)"
  if ! printf '%s\n' "$today" > "$SUCCESS_DAY.tmp" || \
     ! mv -- "$SUCCESS_DAY.tmp" "$SUCCESS_DAY" || \
     ! printf '%s\n' "$epoch" > "$SUCCESS_EPOCH.tmp" || \
     ! mv -- "$SUCCESS_EPOCH.tmp" "$SUCCESS_EPOCH"; then
    echo '[backup] ERROR: respaldo creado pero no se pudo actualizar el control de exito.' >&2
    return 1
  fi

  echo "[backup] OK: $filename"
  echo '[backup] Verificacion preliminar pg_restore --list: OK (no sustituye una prueba de restauracion).'

  # Solo archivos creados por este servicio. La purga se ejecuta SOLO tras exito.
  minutes="$((BACKUP_RETENTION_DAYS * 1440))"
  find "$BACKUP_DIR" -maxdepth 1 -type f -name 'marketplace_*.dump' -mmin "+$minutes" -print \
    | while IFS= read -r expired; do
        if rm -f -- "$expired" "$expired.sha256"; then
          echo "[backup] Retencion: eliminado $(basename "$expired")"
        else
          echo "[backup] AVISO: no se pudo retirar $(basename "$expired")" >&2
        fi
      done
  return 0
}

health() {
  [ -s "$SUCCESS_EPOCH" ] || exit 1
  saved="$(cat "$SUCCESS_EPOCH")"
  case "$saved" in ''|*[!0-9]*) exit 1;; esac
  now="$(date +%s)"
  age="$((now - saved))"
  # 25 horas para dejar margen a la ejecucion diaria de las 03:00.
  [ "$age" -ge 0 ] && [ "$age" -le 90000 ]
}

mode="${1:-schedule}"
case "$mode" in
  health)
    health
    exit $?
    ;;
  once)
    backup_once
    exit $?
    ;;
  schedule)
    echo "[backup] Programado: cada dia a las ${BACKUP_HOUR}:00 (TZ=$TZ). Retencion: ${BACKUP_RETENTION_DAYS} dias."
    # Primero respalda al iniciarse si todavia no hay una copia exitosa de hoy.
    retry_pending=0
    last_attempt=0
    if [ "$(last_day)" != "$(date +%Y-%m-%d)" ]; then
      if ! backup_once; then
        echo '[backup] AVISO: reintentaremos el respaldo sin tocar los anteriores.' >&2
        retry_pending=1
        last_attempt="$(date +%s)"
      fi
    else
      echo '[backup] Ya hay respaldo de hoy; esperando la siguiente jornada.'
    fi
    while :; do
      sleep 60
      today="$(date +%Y-%m-%d)"
      if [ "$(last_day)" = "$today" ]; then
        retry_pending=0
        continue
      fi
      hour="$(date +%H)"
      # Un fallo inicial se reintenta aunque todavia no sean las 03:00.
      if [ "$hour" -lt "$BACKUP_HOUR" ] && [ "$retry_pending" -eq 0 ]; then continue; fi
      now="$(date +%s)"
      [ "$((now - last_attempt))" -ge 900 ] || continue
      last_attempt="$now"
      if backup_once; then
        retry_pending=0
      else
        echo '[backup] AVISO: fallo; nuevo intento en 15 minutos.' >&2
        retry_pending=1
      fi
    done
    ;;
  *)
    echo 'Uso: backup-postgres.sh [schedule|once|health]' >&2
    exit 2
    ;;
esac
