# GuauMiau Microservice

Microservicio REST para gestión de mascotas compatible con la aplicación Android GuauMiau.

## 🚀 Tecnologías

- Java 17
- Spring Boot 4.0.0
- PostgreSQL (Supabase)
- Spring Data JPA
- Maven

## 📋 Endpoints

### Base URL
```
http://localhost:8080/api/pets
```

### Operaciones

#### Crear mascota
```http
POST /api/pets
Content-Type: application/json

{
  "name": "Rex",
  "type": "Perro",
  "userEmail": "user@example.com"
}
```

#### Obtener mascotas por email
```http
GET /api/pets?userEmail=user@example.com
```

#### Obtener mascota por ID
```http
GET /api/pets/{id}
```

#### Eliminar mascota
```http
DELETE /api/pets/{id}
```

## 🛠️ Instalación y Ejecución Local

### Requisitos
- Java 17+
- Maven 3.6+

### Configuración

1. Clona el repositorio:
```bash
git clone <tu-repo>
cd guaumiau
```

2. Configura las variables de entorno (opcional):
```bash
export DATABASE_URL=jdbc:postgresql://tu-host:5432/postgres
export DATABASE_USERNAME=tu-usuario
export DATABASE_PASSWORD=tu-password
```

3. Ejecuta la aplicación:
```bash
./mvnw spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

## 📦 Despliegue en Railway/Render

### Variables de entorno requeridas:
```
DATABASE_URL=jdbc:postgresql://aws-1-us-east-2.pooler.supabase.com:5432/postgres
DATABASE_USERNAME=postgres.mwgzoamzvvehbatfsran
DATABASE_PASSWORD=tu-password-aqui
PORT=8080
SHOW_SQL=false
CORS_ORIGINS=*
```

## 🗄️ Base de Datos

La aplicación crea automáticamente la tabla `pets` con la siguiente estructura:

| Columna     | Tipo      | Descripción          |
|-------------|-----------|----------------------|
| id          | INTEGER   | ID autogenerado      |
| name        | VARCHAR   | Nombre de la mascota |
| type        | VARCHAR   | Tipo de mascota      |
| user_email  | VARCHAR   | Email del dueño      |

## 📱 Compatibilidad Android

Este microservicio está diseñado para ser consumido por la aplicación Android GuauMiau, con tipos de datos y estructura compatibles con Kotlin/Room.

## 📄 Licencia

MIT
