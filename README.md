# Marketplace de Alojamientos - Backend

## Descripción del proyecto

Este repositorio contiene el backend del proyecto **Marketplace de Alojamientos**, desarrollado para la asignatura Ingeniería de Software II de la Universidad El Bosque.

El proyecto consiste en desarrollar una plataforma web para la empresa Inversiones LR, donde los anfitriones puedan publicar sus alojamientos y los huéspedes puedan buscarlos, consultar su disponibilidad y realizar reservas.

El backend se encargará de manejar los datos, las reglas de negocio y las operaciones necesarias para el funcionamiento del sistema.

## Funcionalidades principales

Las funcionalidades que se planean desarrollar son:

- Registro e inicio de sesión de usuarios.
- Gestión de perfiles y permisos.
- Publicación y administración de alojamientos.
- Búsqueda y disponibilidad de alojamientos.
- Cotización y gestión de reservas.
- Simulación de pagos.
- Cancelación de reservas.
- Reseñas y calificaciones.
- Notificaciones.
- Administración, reportes y auditoría.

## Tecnologías del proyecto

Para el desarrollo del backend utilizaremos principalmente:

- **Java 21:** lenguaje de programación.
- **Spring Boot:** framework para desarrollar el backend.
- **PostgreSQL:** base de datos principal.
- **Redis:** almacenamiento temporal, caché y apoyo al procesamiento de tareas.
- **Docker:** contenedores para ejecutar los componentes del sistema.
- **Stripe Sandbox:** simulación de pagos.
- **AWS EC2:** infraestructura prevista para el despliegue.

También utilizaremos las herramientas solicitadas en la asignatura:

- **GitHub:** repositorio y control de versiones.
- **GitHub Actions:** ejecución automática de pruebas y verificaciones.
- **SonarCloud:** análisis de calidad del código.
- **CodeRabbit:** apoyo en la revisión del código.
- **Jira:** gestión de historias de usuario y sprints.
- **Confluence:** documentación del proyecto.
- **OpenAPI:** documentación de los servicios del backend.

## Arquitectura y patrones de diseño

Para el backend se ha planteado una arquitectura de **monolito modular**, utilizando Spring Boot.

El código se organizará en módulos según las funcionalidades del sistema, separando las responsabilidades de presentación, aplicación, dominio e infraestructura, y utilizando puertos y adaptadores para las integraciones externas.

También se contempla ejecutar la API y los procesos en segundo plano de manera separada, pero compartiendo el mismo código del backend.

Entre los patrones de diseño que se planean aplicar están:

- **Strategy:** para manejar las diferentes políticas de cancelación.
- **State:** para gestionar los cambios de estado de las publicaciones y reservas.
- **Repository:** para organizar el acceso a los datos.
- **Adapter:** para integrar servicios externos sin afectar directamente la lógica de negocio.

Estos patrones se irán implementando según los módulos y las necesidades del sistema.

## Organización del trabajo

El proyecto se desarrollará utilizando la metodología Scrum, trabajando por sprints y organizando las historias de usuario mediante Jira.

Se utilizarán ramas de GitHub para desarrollar las funcionalidades y Pull Requests para revisar e integrar los cambios a la rama principal.

También se realizarán pruebas y revisiones de calidad durante el desarrollo.

## Estado del proyecto

Actualmente, el backend se encuentra en su etapa inicial de desarrollo.

Se comenzará con el módulo de autenticación, usuarios y perfiles, y posteriormente se implementarán los demás módulos de acuerdo con la planificación del proyecto.

Este README se actualizará a medida que avance el desarrollo.

## Integrantes

- Sophia Mateus Ruiz
- Anderson Oswaldo Gamba Cubillos
- Camila Valentina Martínez Cantor

**Asignatura:** Ingeniería de Software II

**Universidad:** Universidad El Bosque

**Año:** 2026
