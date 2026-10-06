# jsonblog-backend

API REST de blog desarrollada en **Java y Spring Boot** que consume y procesa datos de
[JSONPlaceholder](https://jsonplaceholder.typicode.com), un servicio público de
prueba. Expone endpoints para listar publicaciones enriquecidas con comentarios
y datos del autor (con paginacion) y para eliminar publicaciones, con manejo
centralizado de errores y documentacion interactiva.

## Caracteristicas

- Endpoint `GET /api/posts` con paginacion y enriquecimiento: cada post se devuelve junto con sus comentarios y los datos del usuario autor.
- Endpoint `DELETE /api/posts/{id}` para eliminar una publicacion (simulado contra JSONPlaceholder).
- Manejo centralizado de errores con `@RestControllerAdvice` (HTTP 400, 404 y 500) que devuelve un cuerpo JSON estructurado.
- Cache en memoria (Caffeine) para reducir las llamadas al servicio externo.
- Perfiles de Spring (`dev`/`prod`) y configuracion externalizada (URLs, timeouts, cache, CORS, limites) por variables de entorno.
- Containerizacion con Docker (imagen multi-stage) y `docker-compose`.
- Trazabilidad por request: header `X-Request-Id` y logs correlacionados (MDC).
- CORS configurable y hardening basico de request.
- Documentacion interactiva con Swagger UI.
- Cobertura de tests unitarios y de integracion (JUnit 5, Mockito, MockRestServiceServer, MockMvc).

## Stack tecnologico

| Tecnologia        | Uso                                                        |
|-------------------|------------------------------------------------------------|
| Java 17           | Lenguaje principal                                         |
| Spring Boot 3.5.6 | Framework de la aplicacion                                 |
| Maven + mvnw      | Gestion de dependencias y build (con Maven Wrapper)        |
| Spring Web / MVC  | Endpoints REST                                             |
| RestTemplate      | Cliente HTTP para consumir JSONPlaceholder                 |
| Jackson / JSON    | Serializacion y deserializacion                            |
| Lombok            | Reduccion de codigo repetitivo                              |
| Caffeine + Spring Cache | Cache en memoria del servicio externo                |
| Springdoc OpenAPI 2 | Documentacion y pruebas con Swagger UI (`springdoc-openapi-starter-webmvc-ui`) |
| Docker + Compose  | Containerizacion (build multi-stage, runtime JRE 17)       |
| Spring Boot Test  | JUnit 5, Mockito, MockRestServiceServer y MockMvc          |

## Arquitectura

La aplicacion sigue una arquitectura en capas simple y desacoplada:

```
HTTP Request
     │
     ▼
┌──────────────────────────────┐
│        Controller            │  Recibe la peticion, valida params, responde JSON
└──────────────┬───────────────┘
               ▼
┌──────────────────────────────┐
│         Service              │  Logica de negocio, paginacion y armado del DTO
└──────────────┬───────────────┘
               ▼
┌──────────────────────────────┐
│  Client (RestTemplate)       │  Llamadas HTTP a JSONPlaceholder
└──────────────┬───────────────┘
               ▼
        JSONPlaceholder
```

- **Controller**: maneja las solicitudes HTTP y devuelve respuestas JSON.
- **Service**: contiene la logica de negocio (paginacion, enriquecimiento, conversion a DTO).
- **Client / RestTemplate**: gestiona las llamadas a la API externa.
- **DTOs**: exponen unicamente los datos necesarios en cada respuesta.
- **ControllerAdvice**: centraliza los errores y los traduce a codigos HTTP adecuados (400 / 404 / 500) con un cuerpo JSON estructurado.
- **RequestIdFilter**: asigna un `X-Request-Id` por request y lo publica en el MDC para correlacionar los logs.

## Requisitos previos

- **Java 17**
- **Maven 3.9+** (o usar el Maven Wrapper incluido: `./mvnw`)

## Instalacion y ejecucion

```bash
git clone https://github.com/josueluque/jsonblog-backend.git
cd jsonblog-backend

# Compilar e instalar dependencias
mvn clean install

# Ejecutar la aplicacion
mvn spring-boot:run
```

La aplicacion queda disponible en `http://localhost:8080`.

Swagger UI (documentacion interactiva de la API):

```
http://localhost:8080/swagger-ui/index.html
```

La definicion OpenAPI en JSON esta disponible en `http://localhost:8080/v3/api-docs`.

## Ejecucion con Docker

El proyecto incluye un `Dockerfile` multi-stage (build con Maven, runtime sobre
`eclipse-temurin:17-jre` corriendo como usuario **no-root**) y un `docker-compose.yml`
que levanta la app con el perfil `prod` y expone el puerto `8080`.

```bash
# Build de la imagen
docker build -t jsonblog-backend:latest .

# Levantar con docker-compose (perfil prod)
docker compose up -d

# Logs y bajada
docker compose logs -f
docker compose down
```

La configuracion se puede sobrescribir con variables de entorno (ver la seccion siguiente);
en `docker-compose.yml` se declaran como `environment:`.

## Configuracion (perfiles y variables de entorno)

La aplicacion usa perfiles de Spring. El perfil activo por defecto es `dev`; se cambia con
la variable `SPRING_PROFILES_ACTIVE` (`dev` o `prod`). Toda la configuracion sensible/variable
esta externalizada con valores por defecto razonables:

| Variable de entorno | Default | Descripcion |
|---------------------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | `dev` | Perfil activo (`dev` / `prod`) |
| `JSONPLACEHOLDER_BASE_URL` | `https://jsonplaceholder.typicode.com` | URL base del servicio externo |
| `HTTP_CLIENT_CONNECT_TIMEOUT` | `5000` | Timeout de conexion del cliente HTTP (ms) |
| `HTTP_CLIENT_READ_TIMEOUT` | `5000` | Timeout de lectura del cliente HTTP (ms) |
| `CACHE_MAXIMUM_SIZE` | `100` | Tamano maximo del cache (entradas) |
| `CACHE_EXPIRE_AFTER_WRITE_MINUTES` | `10` (`2` en `dev`) | TTL de las entradas del cache (min) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Origenes permitidos para CORS (lista separada por comas) |
| `SERVER_MAX_HTTP_HEADER_SIZE` | `8KB` | Tamano maximo de los headers de request |
| `SERVER_TOMCAT_MAX_SWALLOW_SIZE` | `2MB` | Maximo que Tomcat "traga" de un body rechazado |
| `SERVER_TOMCAT_MAX_FORM_POST_SIZE` | `256KB` | Tamano maximo de un form POST |

Ejemplo (perfil prod con un origen CORS propio):

```bash
SPRING_PROFILES_ACTIVE=prod CORS_ALLOWED_ORIGINS=https://miapp.com ./mvnw spring-boot:run
```

## Endpoints

### GET /api/posts
Devuelve los posts paginados, enriquecidos con sus comentarios y el usuario autor.

```bash
curl -X GET "http://localhost:8080/api/posts?page=0&size=10"
```

| Parametro | Tipo | Default | Descripcion                |
|-----------|------|---------|----------------------------|
| `page`    | int  | 0       | Numero de pagina (0-based, `>= 0`) |
| `size`    | int  | 10      | Cantidad de posts por pagina (`1..100`; un valor fuera de rango devuelve `400`) |

**Respuestas:**
- `200 OK` con los metadatos de paginacion y el detalle de los posts.
- `204 No Content` cuando no hay posts para la pagina solicitada.
- `400 Bad Request` si `page` o `size` no cumplen las restricciones.
- `500 Internal Server Error` si falla la comunicacion con el servicio externo.

> **Nota sobre el `204`:** hoy una pagina sin resultados (por ejemplo, `page`
> mas alla del total) devuelve `204 No Content` sin cuerpo. Esto es ambiguo para
> los clientes, ya que no permite distinguir "pagina vacia" de "sin datos" ni
> incluye los metadatos de paginacion. Comportamiento conocido; una alternativa
> mas explicita seria devolver `200 OK` con `content: []` y los metadatos.

El paginado se resuelve en el origen: la API consulta a JSONPlaceholder con `_page`/`_limit` y propaga `X-Total-Count`, devolviendo metadatos de paginacion y links `prev`/`next` (relativos).

**Ejemplo de respuesta:**
```json
{
  "page": 0,
  "size": 10,
  "totalElements": 100,
  "totalPages": 10,
  "content": [
    {
      "post": {
        "id": 1,
        "title": "titulo del post",
        "body": "contenido del post"
      },
      "user": {
        "id": 1,
        "name": "Leanne Graham",
        "email": "Sincere@april.biz"
      },
      "comments": [
        {
          "id": 1,
          "name": "comentario",
          "email": "email@ejemplo.com",
          "body": "contenido del comentario"
        }
      ]
    }
  ],
  "prev": null,
  "next": "/api/posts?page=1&size=10"
}
```

### DELETE /api/posts/{id}
Elimina una publicacion (simulado contra JSONPlaceholder).

```bash
curl -X DELETE "http://localhost:8080/api/posts/10"
```

**Respuestas:**
- `204 No Content` cuando la publicacion se elimino correctamente.
- `404 Not Found` cuando el post no existe.
- `500 Internal Server Error` si falla la comunicacion con el servicio externo.

## Formato de error

Las respuestas de error (`400`, `404`, `500`) devuelven un cuerpo JSON estructurado:

```json
{
  "timestamp": "2026-09-30T13:29:48.572Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Parametros de la solicitud invalidos",
  "requestId": "987400ec-95e7-49e7-b0d4-a21178904987",
  "path": "/api/posts"
}
```

El campo `requestId` coincide con el header `X-Request-Id` de la respuesta, lo que permite
correlacionar la respuesta con los logs del servidor.

## Observabilidad

Cada request recibe un identificador de trazabilidad: si llega el header `X-Request-Id` se
reutiliza, si no se genera un UUID. Ese id se devuelve en el header `X-Request-Id` de la
respuesta y se incluye en todas las lineas de log de esa request (via MDC), lo que facilita
seguir una peticion punta a punta.

```bash
curl -i -H "X-Request-Id: mi-trace-123" "http://localhost:8080/api/posts?size=1"
# La respuesta incluye: X-Request-Id: mi-trace-123
```

## Testing

La suite de tests cubre la logica de negocio, los clients HTTP hacia
JSONPlaceholder (simulando escenarios de exito y error) y la capa web con el
manejo de errores.

```bash
mvn test
```

## Estructura del proyecto

```
src/main/java/com/backend/rest_api/
├── client/        RestTemplate clients para JSONPlaceholder
├── config/        Beans y config (RestTemplate, Cache/Caffeine, CORS, RequestIdFilter)
├── controller/    Endpoints REST
├── domain/        Entidades del dominio
│   └── dto/       DTOs de respuesta (incluye ErrorResponse y PageResponse)
├── exception/     Excepciones de dominio y ControllerAdvice
└── service/       Logica de negocio
src/main/resources/
├── application.yml            Config base + perfil activo por defecto
├── application-dev.yml        Overrides del perfil dev
├── application-prod.yml       Overrides del perfil prod
└── logback-spring.xml         Patron de logs con requestId
Dockerfile, docker-compose.yml, .dockerignore   Containerizacion
src/test/java/com/backend/rest_api/   Tests unitarios y de integracion
```