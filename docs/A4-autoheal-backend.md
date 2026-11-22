# A4 — recuperación automática selectiva (Docker Compose)

## Qué cambia

Se añade el servicio `autoheal` para observar **únicamente** los contenedores etiquetados `api` y `worker` del proyecto `marketplace-a3`. Si Docker marca alguno `unhealthy` tras sus tres intentos de healthcheck, el supervisor revisa cada 20 segundos y ejecuta `docker container restart --time 45` para ese contenedor. Se mantienen `restart: unless-stopped` para procesos que terminan.

El healthcheck **de la API** pasa de `/actuator/health/readiness` a `/actuator/health/liveness`, para no reiniciar Java solo porque PostgreSQL o Redis estén temporalmente inaccesibles. La readiness sigue disponible para diagnóstico. **Esto detecta falta de respuesta HTTP, no garantiza que un job se ejecute correctamente.** No se reinician PostgreSQL ni Redis mediante autoheal, y no se borran sus volúmenes.

## Advertencia de seguridad

`autoheal` monta `/var/run/docker.sock`, que concede capacidad de control administrativo sobre Docker Host. Aunque el script solo selecciona por etiquetas, **las etiquetas no restringen los permisos del socket**. Usar únicamente en Docker Desktop local / entorno de desarrollo de confianza. Para producción multiusuario, diseñar otra solución (orquestador, política de salud, proxy de socket con permisos mínimos, etc.). No publicar este socket por TCP.

## Instalar en Windows CMD

Desde `C:\Users\Anderson\Desktop\ProyectoISW2\marketplace`:

```cmd
git switch -c feat/autoheal-backend
powershell -NoProfile -Command "Expand-Archive -LiteralPath '$env:USERPROFILE\Downloads\A4-autoheal-backend.zip' -DestinationPath '.' -Force"
docker compose --env-file .env.docker -p marketplace-a3 config --quiet
```

Si aún estás en la rama `feat/docker-compose-backend`, la nueva rama conserva A1–A3.

## Levantar solo los servicios modificados

```cmd
docker compose --env-file .env.docker -p marketplace-a3 up -d --no-deps api worker autoheal
docker compose --env-file .env.docker -p marketplace-a3 ps
docker compose --env-file .env.docker -p marketplace-a3 logs --tail 40 autoheal
```

Espera a que `api` y `worker` indiquen `healthy` (pueden mostrar `health: starting` durante el inicio). Si no aparecen saludables, revisar sus logs antes de continuar.

```cmd
curl.exe -i http://127.0.0.1:8082/actuator/health
curl.exe -i http://127.0.0.1:8082/actuator/health/readiness
```

Ambas deben dar `200`/`UP` cuando PostgreSQL y Redis estén disponibles.

## Prueba controlada de auto-recuperación (sin detener DB)

**Solo en este entorno local de pruebas, nunca en producción.** Se usa un contenedor desechable con un healthcheck intencionalmente fallido, sin alterar API, worker, PostgreSQL ni Redis.

La prueba recomendada usa un contenedor desechable: no pausa ni modifica API/worker.

### Prueba recomendada sin tocar API/worker: contenedor de ensayo etiquetado

```cmd
docker run -d --name marketplace-a4-prueba --label com.isw2.autoheal=true --label com.isw2.autoheal.project=marketplace-a3 --label com.docker.compose.project=marketplace-a3 --health-cmd="exit 1" --health-interval=5s --health-retries=2 --health-timeout=2s alpine:3.21 sh -c "sleep 600"
docker logs --tail 40 marketplace-a3-autoheal-1
```

ATENCIÓN: la etiqueta del proyecto no significa que el contenedor de prueba pertenezca a Compose; el monitor lo detecta solo por etiquetas. Se puede comprobar el contador de reinicios con:

```cmd
docker inspect --format "{{.RestartCount}} {{.State.Health.Status}}" marketplace-a4-prueba
docker logs --tail 40 marketplace-a3-autoheal-1
docker rm -f marketplace-a4-prueba
```

**El contenedor deliberadamente insalubre será reiniciado repetidamente hasta que se elimine.** Esta prueba es útil para demostrar que el monitor actúa. El nombre del contenedor supervisor es `marketplace-a3-autoheal-1` porque el proyecto se llama `marketplace-a3`.

## Guardar solo A4

```cmd
git add -- compose.yaml docs/A4-autoheal-backend.md
git diff --cached --check
git diff --cached --stat
git commit -m "feat(infra): reiniciar contenedores no saludables"
git push -u origin feat/autoheal-backend
```

No subir `.env.docker`. No ejecutar `docker compose down -v`: elimina los volúmenes persistentes. Los cambios ajenos que ya tienes en el working tree no deben incorporarse al commit.
