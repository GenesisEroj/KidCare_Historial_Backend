# KidCare — Microservicio de Historial

Microservicio encargado de almacenar los resúmenes clínicos generados por Claude a partir de las interacciones seleccionadas por el tutor para compartir con el médico durante la consulta.

---

## Tecnologías

- Java 21
- Spring Boot 3.5.14
- Spring Security + JWT (jjwt 0.12.6)
- Spring Data JPA
- MySQL
- Lombok
- Maven

---

## Puerto

```
8084
```

---

## Estructura del proyecto

```
src/main/java/com/kidcare/historial_service/
│
├── model/
│   └── Historial.java → Entidad que almacena los resúmenes generados por Claude para el médico
│
├── repository/
│   └── HistorialRepository.java → Acceso a datos de Historial (búsqueda por menor, último historial)
│
├── dto/
│   ├── HistorialRequestDTO.java  → Datos para crear un historial con el resumen de Claude
│   └── HistorialResponseDTO.java → Datos de respuesta de un historial
│
├── security/
│   ├── JwtUtil.java        → Genera, valida y extrae datos de tokens JWT
│   ├── JwtFilter.java      → Intercepta cada request y valida el token JWT del header
│   └── SecurityConfig.java → Configura rutas públicas, protegidas y política de sesión
│
├── service/
│   └── HistorialService.java → Lógica de creación, listado y obtención del último historial
│
├── controller/
│   └── HistorialController.java → Endpoints de /api/historial
│
└── exception/
    └── GlobalExceptionHandler.java → Maneja errores de validación y excepciones de negocio
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

## Requisitos previos

- Java 21 instalado
- Maven instalado
- MySQL corriendo (cuando se conecte la BD)
- VS Code con Extension Pack for Java y Spring Boot Extension Pack

---

## Cómo iniciar el proyecto

### 1. Clonar el repositorio

```bash
git clone https://github.com/vareeth227/KidCare_Historial_Backend.git
cd KidCare_Historial_Backend
```

### 2. Configurar variables de entorno

Edita el archivo `src/main/resources/application.properties` con tus datos de MySQL cuando tengas la base de datos lista:

```properties
server.port=8084
spring.application.name=historial-service
spring.datasource.url=jdbc:mysql://localhost:3306/db_historial
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_PASSWORD
spring.jpa.hibernate.ddl-auto=update
jwt.secret=kidcare-secret-key-2024-segura-32chars
jwt.expiration=86400000
```

### 3. Compilar el proyecto

```bash
mvn clean install -DskipTests
```

### 4. Ejecutar el proyecto

```bash
mvn spring-boot:run
```

El microservicio estará disponible en `http://localhost:8084`

---

## Notas importantes

- El token JWT debe enviarse en el header `Authorization: Bearer <token>` en todas las rutas protegidas.
- La ruta `/api/historial/medico/{idMenor}` es pública porque el médico no tiene cuenta en el sistema.
- La clave `jwt.secret` debe ser la misma en todos los microservicios de KidCare.
- Por ahora la base de datos está desactivada en `application.properties`. Cuando se conecte Docker hay que eliminar la línea `spring.autoconfigure.exclude`.

---

## Integrantes

| Nombre | Rol |
|--------|-----|
| Génesis Rojas | Líder de Proyecto / DBA / Analista Funcional |
| Francisco Monsalve | Frontend Mobile / QA |
| Benjamín Peña | Backend / Integración IA / DevOps |
