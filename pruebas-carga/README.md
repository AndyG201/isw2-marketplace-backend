# Pruebas de carga

Hay dos niveles:

## 1. Prueba de concurrencia dentro de la suite (se ejecuta siempre)

`BusquedaConcurrenciaIntegracionTest` lanza muchos hilos a la vez contra la aplicación completa (seguridad, PostgreSQL y Redis reales)
y comprueba que la búsqueda no falla ni devuelve resultados distintos para la misma consulta. Corre con `./mvnw clean verify`.
No mide tiempos: eso depende de la máquina y haría la prueba inestable.

## 2. Prueba de carga con k6 (se ejecuta a mano contra un servidor)

`busqueda.js` simula visitantes buscando alojamientos: una carga normal de 20 usuarios simultáneos y un pico de 100.
Termina con error si no se cumplen los umbrales:

| Umbral | Valor |
|--------|-------|
| Peticiones fallidas | menos del 1 % |
| Tiempo de respuesta (percentil 95) | menos de 800 ms |
| Verificaciones (estado 200 y lista de publicaciones) | más del 99 % |

Ejecución, con el backend levantado (perfil de la API, con PostgreSQL y Redis):

```bash
k6 run -e BASE_URL=http://localhost:8080 pruebas-carga/busqueda.js
```

Sin instalar k6, con Docker:

```bash
docker run --rm -i -e BASE_URL=http://host.docker.internal:8080 grafana/k6 run - < pruebas-carga/busqueda.js
```

Recomendaciones:

- Córrela contra un ambiente de pruebas con datos (cientos o miles de publicaciones), no contra producción.
- Compara una ejecución con la caché de Redis encendida y otra con Redis apagado: la búsqueda debe seguir respondiendo
  (más lenta), no fallar.
- Guarda el resultado de cada ejecución (por ejemplo con `--summary-export resultado.json`) para ver si el rendimiento empeora entre versiones.
