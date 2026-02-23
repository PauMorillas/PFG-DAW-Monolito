# 🚀 PFG: Sistema Integral de Gestión de Eventos para PYMES y particulares

Este repositorio contiene el **Core** del sistema (Monolito Java + Microservicio de Seguridad). El proyecto utiliza una arquitectura híbrida que combina renderizado en servidor (SSR) para usuarios finales y una arquitectura de microservicios para la gestión de seguridad.

## 🏗️ Arquitectura del Sistema

El ecosistema completo se divide en dos grandes bloques:

1.  **Core (Este repositorio):** Backend Java + Spring Boot, Vistas Thymeleaf para el iFrame de los clientes y una API en Laravel para la gestión de los dominios permitidos por el servidor.
2.  **Panel de Administración:** Frontend SPA desarrollado en **Angular 21**. 
    * 🔗 [Repositorio del Front de Gerentes](https://github.com/PauMorillas/PFG-DAW-ANGULARFRONT)
3.  **Migración a Laravel:** Evolución técnica del backend hacia **PHP + Laravel 11**. En este repositorio se aplica **Ingeniería de Software** de alto nivel para la reescritura del sistema.
    * 🔗 [Repositorio de la Migración a Laravel](https://github.com/PauMorillas/Migration-PFG-DAW-ToLaravel)
   
### **Tecnologías del Core**
* **Java Spring Boot 3.x:** Motor principal de la lógica de negocio.
* **Spring Security:** Control de acceso robusto y filtrado de peticiones mediante `ApiTokenFilter`.
* **Thymeleaf:** Motor de plantillas para las páginas servidas directamente por el servidor.
* **Laravel API:** Microservicio especializado en la gestión de dominios permitidos.
* **FullCalendar JS:** Integración de librería de terceros para la gestión visual de calendarios en el frontend.

## 🛠️ Despliegue y Automatización

Para facilitar el despliegue y la limpieza de contenedores, se incluyen scripts de automatización:

* **`cleanup.sh`**: Script para entornos Linux/Bash.
* **`cleanuppodman.sh`**: Script optimizado para entornos Windows con Podman.

### **Levantamiento con Docker**
El sistema se orquesta mediante `docker-compose.yml`, levantando los siguientes servicios:
* **`pfg_app`**: Aplicación Java Spring Boot.
* **`pfg_mysql_db`**: Base de datos MySQL 8.0 (con volumen persistente `db_data`).
* **`pfg_allowed_domains_api`**: API de seguridad en Laravel.
* **`pfg_cloudflared`**: Túnel de Cloudflare para exposición segura.

```bash
# Para iniciar el proyecto:
./cleanup.sh  # O cleanuppodman.sh en Windows
docker-compose up -d
