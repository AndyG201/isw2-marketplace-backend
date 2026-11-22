# A1 — Actuator y salud del backend

Preparado sobre `marketplace-backend-codigo.zip`, la versión actualizada que compartiste.
Esta entrega contiene únicamente A1. Mantiene Java 22, Spring Boot 3.4.5 y WAR.

## Archivos que se aplican

Todas las rutas parten de la carpeta que contiene `pom.xml`.

| Ruta | Cambio |
| --- | --- |
| `pom.xml` | Añade `spring-boot-starter-actuator`. |
| `src/main/java/co/edu/unbosque/marketplace/usuario/security/SecurityConfig.java` | Permite únicamente GET anónimo a `/actuator/health` y sus subrutas. |
| `src/main/resources/application-observability.properties` | Configura exposición exclusiva de health, sin componentes ni detalles internos. |
| `src/main/resources/application.yaml` | Importa la configuración de salud. No contiene conexiones ni credenciales. |
| `src/test/java/co/edu/unbosque/marketplace/config/ActuatorHealthSecurityTest.java` | Añade seis pruebas de salud y autorización. |

El ZIP también incluye esta guía, `docs/A1-actuator-health.md`.
Los archivos `.java` y `pom.xml` son completos, listos para reemplazar los del ZIP revisado.
No hay una carpeta adicional `marketplace/`: al extraer desde la raíz, las clases quedan en sus paquetes correspondientes.
El archivo local `src/main/resources/application.properties` se conserva. El ZIP no lo incluye.

## 1. Preparar la rama

Ejecutar en CMD:

```cmd
cd /d C:\Users\Anderson\Desktop\ProyectoISW2\marketplace
git status -sb
git switch -c feat/actuator-health
```

Si ya estás en una rama creada para A1, conserva esa rama y omite `git switch -c`.
Esta entrega está preparada para los archivos del último ZIP. Si has editado `pom.xml` o `SecurityConfig.java` después de enviarlo, compara esos cambios antes de reemplazarlos.

## 2. Descargar y aplicar

Guarda `A1-actuator-health.zip` en Descargas. Desde la raíz del proyecto:

```cmd
copy "%USERPROFILE%\Downloads\A1-actuator-health.zip" .
powershell -NoProfile -Command "Expand-Archive -LiteralPath '.\A1-actuator-health.zip' -DestinationPath '.' -Force"
git diff --check
git status -sb
```

No hace falta pegar fragmentos ni crear clases a mano. La extracción instala los cinco archivos en sus rutas.
No ejecutes un paquete anterior de infraestructura junto con este.
`git diff --check` no debe mostrar errores de espacios. Un aviso de conversión LF/CRLF, por sí mismo, no significa que las pruebas hayan fallado.

## 3. Ejecutar las pruebas de A1

```cmd
call .\mvnw.cmd "-Dtest=ActuatorHealthSecurityTest,SecurityConfigTest" test
```

Resultado comprobado al preparar esta entrega con Java 22.0.2 y Maven 3.10.0:

```text
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Seis pruebas corresponden a Actuator y tres a la configuración de seguridad existente.
Esta ejecución compila las clases principales y las pruebas del proyecto, pero ejecuta únicamente esas dos clases de prueba.
Las pruebas nuevas usan indicadores controlados: verifican la seguridad y las respuestas de salud sin conectarse a PostgreSQL, Redis o SMTP reales.
No se afirma que esta comprobación ejecute toda la suite.

## 4. Comprobar el endpoint contra tu aplicación

Para esta comprobación sí deben estar disponibles PostgreSQL y Redis, con las conexiones que ya tienes configuradas.
Arranca el backend como lo haces normalmente, por ejemplo desde tu IDE, o ejecuta:

```cmd
call .\mvnw.cmd spring-boot:run
```

Si ya está ejecutándose, reinícialo para cargar estos cambios. Espera a que termine de arrancar.
En otra ventana de CMD, consulta sin token:

```cmd
curl.exe -i http://localhost:8080/actuator/health
curl.exe -i http://localhost:8080/actuator/health/liveness
curl.exe -i http://localhost:8080/actuator/health/readiness
```

Si tu aplicación utiliza un puerto distinto, sustituye `8080`.

| Ruta | Qué comprueba | Resultado esperado con todo disponible |
| --- | --- | --- |
| `/actuator/health` | Salud global, incluidos los indicadores de PostgreSQL y Redis. | HTTP 200, `status: UP`. Puede listar los nombres de los grupos. |
| `/actuator/health/liveness` | Estado interno de la aplicación. | HTTP 200, `status: UP`. |
| `/actuator/health/readiness` | Aplicación lista y PostgreSQL/Redis disponibles. | HTTP 200, `status: UP`. |

No se muestran credenciales, direcciones de conexión ni el desglose de componentes.
`liveness` no depende de PostgreSQL/Redis para evitar que una caída externa se interprete como un proceso bloqueado.
El indicador SMTP está desactivado; el endpoint no prueba el envío de correo en cada consulta.

Comprueba también que otras rutas de administración no se abrieron:

```cmd
curl.exe -i http://localhost:8080/actuator/env
```

Sin token, la regla de seguridad actual responde HTTP 401. Ese endpoint, además, no está expuesto.

### Interpretar una respuesta distinta

- HTTP 503 con `status: DOWN`: la aplicación atendió la consulta; revisa la disponibilidad de PostgreSQL, Redis y los logs. No es un problema de inicio de sesión.
- HTTP 401 en `/actuator/health`: revisa que se haya reemplazado `SecurityConfig.java` y que estés consultando la aplicación reiniciada.
- HTTP 404 en `/actuator/health`: revisa que Maven haya incorporado Actuator y que los dos archivos de configuración nuevos estén en `src/main/resources`.
- Error de conexión: comprueba que el backend esté arrancado y que el puerto sea correcto.

## 5. Guardar A1 cuando las comprobaciones pasen

```cmd
git add -- pom.xml src/main/java/co/edu/unbosque/marketplace/usuario/security/SecurityConfig.java src/main/resources/application-observability.properties src/main/resources/application.yaml src/test/java/co/edu/unbosque/marketplace/config/ActuatorHealthSecurityTest.java docs/A1-actuator-health.md
git diff --cached --check
git diff --cached --stat
git commit -m "feat(infra): agregar salud del backend con Actuator"
```

Estos comandos seleccionan los archivos de A1. No añadas el ZIP ni tu configuración local al commit.
A1 queda comprobado cuando las nueve pruebas pasan y las consultas de salud responden correctamente en tu equipo.
El push, la integración a main y A2 se revisan después de confirmar este punto.

## Referencia

Spring Boot 3.4, endpoints y sondas de salud:
https://docs.spring.io/spring-boot/3.4/reference/actuator/endpoints.html
