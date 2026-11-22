// Prueba de carga de la búsqueda pública de alojamientos (k6).
//
// Ejecutar contra un servidor ya levantado:
//   k6 run -e BASE_URL=http://localhost:8080 pruebas-carga/busqueda.js
// o con Docker, sin instalar nada:
//   docker run --rm -i -e BASE_URL=http://host.docker.internal:8080 grafana/k6 run - < pruebas-carga/busqueda.js
//
// Los umbrales (thresholds) hacen que k6 termine con error si el servicio no cumple.
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Mezcla de filtros parecida al uso real: unas consultas se repiten (aciertos de caché) y otras varían.
const CIUDADES = ['bogota', 'medellin', 'cali', 'cartagena', 'barranquilla', 'santa marta'];
const TIPOS = ['', 'apartamento', 'casa'];

export const options = {
  scenarios: {
    // Carga normal sostenida.
    carga_normal: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 20 },
        { duration: '2m', target: 20 },
        { duration: '20s', target: 0 },
      ],
      exec: 'busqueda',
    },
    // Pico de tráfico corto, para ver cómo responde el servicio y la caché.
    pico: {
      executor: 'ramping-vus',
      startTime: '3m',
      startVUs: 0,
      stages: [
        { duration: '20s', target: 100 },
        { duration: '40s', target: 100 },
        { duration: '20s', target: 0 },
      ],
      exec: 'busqueda',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],      // menos de 1 % de errores
    http_req_duration: ['p(95)<800'],    // el 95 % de las respuestas en menos de 800 ms
    checks: ['rate>0.99'],
  },
};

function aleatorio(lista) {
  return lista[Math.floor(Math.random() * lista.length)];
}

export function busqueda() {
  const ciudad = aleatorio(CIUDADES);
  const tipo = aleatorio(TIPOS);
  let url = `${BASE_URL}/publicaciones/busqueda?ciudad=${encodeURIComponent(ciudad)}&pagina=0&tamano=20`;
  if (tipo) {
    url += `&tipoAlojamiento=${tipo}`;
  }

  const respuesta = http.get(url, { tags: { name: 'GET /publicaciones/busqueda' } });

  check(respuesta, {
    'estado 200': (r) => r.status === 200,
    'trae la lista de publicaciones': (r) => Array.isArray(r.json('publicaciones')),
  });

  // También se consulta el catálogo completo, que es lo primero que ve un visitante.
  if (Math.random() < 0.2) {
    const catalogo = http.get(`${BASE_URL}/publicaciones/catalogo`, { tags: { name: 'GET /publicaciones/catalogo' } });
    check(catalogo, { 'catálogo 200': (r) => r.status === 200 });
  }

  sleep(Math.random() * 1.5);
}
