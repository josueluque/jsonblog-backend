# jsonblog-backend

API REST de blog desarrollada en **Java y Spring Boot** que consume y procesa datos de
[JSONPlaceholder](https://jsonplaceholder.typicode.com), un servicio público de
prueba. Expone endpoints para listar publicaciones enriquecidas con comentarios
y datos del autor (con paginacion) y para eliminar publicaciones, con manejo
centralizado de errores y documentacion interactiva.

## Caracteristicas

- Endpoint `GET /api/posts` con paginacion y enriquecimiento: cada post se devuelve junto con sus comentarios y los datos del usuario autor.
- Endpoint `DELETE /api/posts/{id}` para eliminar una publicacion (simulado contra JSONPlaceholder).
- Manejo centralizado de errores con `@RestControllerAdvice` (HTTP 404 y 500).
- URLs del servicio externo, timeouts y cache configurables por configuracion externa (Spring properties / variables de entorno).
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
| Springdoc OpenAPI 2 | Documentacion y pruebas con Swagger UI (`springdoc-openapi-starter-webmvc-ui`) |
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
- **ControllerAdvice**: centraliza los errores y los traduce a codigos HTTP adecuados (404 / 500).

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

## Endpoints

### GET /api/posts
Devuelve los posts paginados, enriquecidos con sus comentarios y el usuario autor.

```bash
curl -X GET "http://localhost:8080/api/posts?page=0&size=10"
```

| Parametro | Tipo | Default | Descripcion                |
|-----------|------|---------|----------------------------|
| `page`    | int  | 0       | Numero de pagina (0-based, `>= 0`) |
| `size`    | int  | 10      | Cantidad de posts por pagina (`>= 1`) |

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
├── controller/    Endpoints REST
├── domain/        Entidades y DTOs de respuesta
├── exception/     Excepciones de dominio y ControllerAdvice
└── service/       Logica de negocio
src/test/java/com/backend/rest_api/   Tests unitarios y de integracion
```