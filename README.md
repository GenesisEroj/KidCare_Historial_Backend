# KidCare — Microservicio de Historial

Microservicio encargado de almacenar los resúmenes clínicos generados por Claude a partir de las interacciones seleccionadas por el tutor para compartir con el médico durante la consulta.

---

## Equipo

| Nombre | Rol |
|---|---|
| Génesis Rojas | Líder de Proyecto / DBA / Analista Funcional |
| Francisco Monsalve | Frontend Mobile / QA |
| Benjamín Peña | Backend / Integración IA / DevOps |

---

## Tecnologías

- Java 21
- Spring Boot 3.5.14
- Spring Security + JWT (jjwt 0.12.6)
- Spring Data JPA
- MySQL 8.0 (Docker)
- Lombok
- Maven

**Puerto:** `8084`

---

## Estructura del proyecto

```
src/main/java/com/kidcare/historial_service/
│
├── model/
│   └── Historial.java → Resumen generado por Claude para el médico
│
├── repository/
│   └── HistorialRepository.java → Búsqueda por menor, último historial
│
├── dto/
│   ├── HistorialRequestDTO.java  → Datos para crear un historial
│   └── HistorialResponseDTO.java → Datos de respuesta
│
├── security/
│   ├── JwtUtil.java        → Genera, valida y extrae datos de tokens JWT
│   ├── JwtFilter.java      → Intercepta requests y valida el JWT del header
│   └── SecurityConfig.java → Rutas públicas y protegidas, política de sesión stateless
│
├── service/
│   └── HistorialService.java → Creación, listado y obtención del último historial
│
├── controller/
│   └── HistorialController.java → Endpoints de /api/historial
│
└── exception/
    └── GlobalExceptionHandler.java → Errores de validación → 400 Bad Request
```

---

## Endpoints

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| POST | `/api/historial` | Autenticado | Crea un historial con el resumen generado por Claude |
| GET | `/api/historial/menor/{idMenor}` | Autenticado | Lista todos los historiales de un menor |
| GET | `/api/historial/medico/{idMenor}` | Público | Obtiene el último historial para el médico |

---

## Lógica del historial

- Este microservicio almacena **únicamente resúmenes**, no interacciones individuales.
- Las interacciones completas viven en el MS Chatbot (MongoDB).
- El tutor selecciona qué interacciones incluir y Claude genera el resumen estructurado.
- El resumen **no contiene diagnósticos ni recomendaciones clínicas**.
- La ruta `/api/historial/medico/{idMenor}` es pública porque el médico accede sin cuenta, solo con el enlace temporal generado por el MS Acceso.
- Si la API de Claude falla, el médico ve las interacciones directamente sin resumen.

---

## Cómo iniciar en otro equipo

### Prerrequisitos

| Herramienta | Versión mínima | Descarga |
|---|---|---|
| Java JDK | 21 | https://adoptium.net |
| Maven | 3.9+ | https://maven.apache.org/download.cgi |
| Docker Desktop | 4.x | https://www.docker.com/products/docker-desktop |
| Git | cualquiera | https://git-scm.com |

Verifica la instalación:
```bash
java -version    # debe decir openjdk 21
mvn -version     # debe decir Apache Maven 3.9.x
docker --version # debe decir Docker version 24.x o superior
```

---

### Paso 1 — Clonar el repositorio

```bash
git clone https://github.com/vareeth227/KidCare_Historial_Backend.git
cd KidCare_Historial_Backend
```

---

### Paso 2 — Iniciar MySQL con Docker

Crea el archivo `docker-compose.yml` en la carpeta raíz del proyecto:

```yaml
services:
  mysql:
    image: mysql:8.0
    container_name: kidcare-mysql
    environment:
      MYSQL_ROOT_PASSWORD: kidcare123
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    command: >
      --default-authentication-plugin=mysql_native_password
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci

volumes:
  mysql_data:
```

Inicia el contenedor:

```bash
docker compose up -d
```

Espera 15–20 segundos y verifica:

```bash
docker ps
```

Debes ver `kidcare-mysql` con estado `Up`.

---

### Paso 3 — Crear la base de datos

```bash
docker exec -it kidcare-mysql mysql -u root -pkidcare123 -e "CREATE DATABASE IF NOT EXISTS db_historial CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

Verifica:

```bash
docker exec -it kidcare-mysql mysql -u root -pkidcare123 -e "SHOW DATABASES;"
```

Debes ver `db_historial` en la lista.

---

### Paso 4 — Revisar application.properties

El archivo `src/main/resources/application.properties` ya está configurado para conectarse a MySQL local con las credenciales del Paso 2. No necesitas cambiar nada para desarrollo local.

---

### Paso 5 — Compilar

```bash
mvn clean install -DskipTests
```

Espera a que aparezca `BUILD SUCCESS`.

---

### Paso 6 — Ejecutar

```bash
mvn spring-boot:run
```

Espera a que aparezca:

```
Started HistorialServiceApplication in X.XXX seconds
```

El servicio queda disponible en `http://localhost:8084`.

---

### Paso 7 — Verificar

Necesitas un token JWT válido del usuario-service (puerto 8081). Con ese token:

**PowerShell:**
```powershell
$token = "eyJ..."  # pega tu token JWT aquí
Invoke-RestMethod -Uri "http://localhost:8084/api/historial/menor/1" -Method GET -Headers @{Authorization="Bearer $token"}
```

Respuesta esperada: lista vacía `[]` — confirma que el JWT fue validado correctamente.

La ruta pública del médico no requiere token:
```powershell
Invoke-RestMethod -Uri "http://localhost:8084/api/historial/medico/1" -Method GET
```

---

## Notas importantes

- El token JWT debe enviarse en el header `Authorization: Bearer <token>` en todas las rutas protegidas.
- La ruta `/api/historial/medico/{idMenor}` es pública porque el médico no tiene cuenta en el sistema.
- La clave `jwt.secret` debe ser la misma en todos los microservicios de KidCare: `kidcare-secret-key-2024-segura-32chars`
