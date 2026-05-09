# ARRANQUE — Historial Service (puerto 8084)

Guía paso a paso para iniciar el microservicio de historial en un equipo nuevo.
Sigue los pasos en orden, sin saltarte ninguno.

---

## Antes de empezar — verifica que tienes todo instalado

Abre una terminal y ejecuta cada comando. Si alguno falla, instala la herramienta antes de continuar.

```bash
java -version
```
Debe decir `openjdk 21`. Si no lo tienes: https://adoptium.net → descarga **Temurin 21 LTS**.

```bash
mvn -version
```
Debe decir `Apache Maven 3.9.x`. Si no lo tienes: https://maven.apache.org/download.cgi

```bash
docker --version
```
Debe decir `Docker version 24.x` o superior. Si no lo tienes: https://www.docker.com/products/docker-desktop → instala Docker Desktop y ábrelo antes de continuar.

```bash
git --version
```
Cualquier versión sirve. Si no lo tienes: https://git-scm.com

> **Importante:** este servicio depende de que el **usuario-service (puerto 8081)** esté corriendo para validar los tokens JWT. Inicia primero el usuario-service siguiendo su propio `ARRANQUE.md`.

---

## Paso 1 — Obtener el código

Si ya tienes el repositorio clonado:

```bash
cd KidCare_Historial_Backend
git fetch origin
git checkout benja
git pull origin benja
```

Si es la primera vez:

```bash
git clone https://github.com/vareeth227/KidCare_Historial_Backend.git
cd KidCare_Historial_Backend
git checkout benja
```

---

## Paso 2 — Iniciar Docker Desktop

Abre Docker Desktop desde el menú de inicio y espera a que el ícono de la ballena deje de animarse.

Verifica que Docker esté corriendo:

```bash
docker ps
```

---

## Paso 3 — Iniciar MySQL con Docker

> Si ya iniciaste MySQL para otro servicio y el contenedor `kidcare-mysql` está corriendo, salta directamente al Paso 4.

Crea un archivo `docker-compose.yml` en la carpeta raíz del proyecto:

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

Ejecuta:

```bash
docker compose up -d
```

Espera 20 segundos y verifica:

```bash
docker ps
```

Debes ver `kidcare-mysql` con estado `Up`.

---

## Paso 4 — Crear la base de datos

**Windows PowerShell:**
```powershell
docker exec kidcare-mysql mysql -u root -pkidcare123 -e "CREATE DATABASE IF NOT EXISTS db_historial CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

**Mac / Linux:**
```bash
docker exec kidcare-mysql mysql -u root -pkidcare123 -e "CREATE DATABASE IF NOT EXISTS db_historial CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

Verifica:

```bash
docker exec kidcare-mysql mysql -u root -pkidcare123 -e "SHOW DATABASES;"
```

Debes ver `db_historial` en la lista.

---

## Paso 5 — Revisar application.properties

Abre `src/main/resources/application.properties`. Las credenciales ya están configuradas para el Docker del Paso 3. No necesitas cambiar nada para desarrollo local.

---

## Paso 6 — Compilar el proyecto

```bash
mvn clean install -DskipTests
```

Espera a que aparezca:

```
BUILD SUCCESS
```

---

## Paso 7 — Iniciar el servicio

```bash
mvn spring-boot:run
```

Espera a que aparezca:

```
Started HistorialServiceApplication in X.XXX seconds
```

El servicio queda disponible en `http://localhost:8084`. **No cierres esta terminal.**

---

## Paso 8 — Verificar que funciona

Primero obtén un token JWT del usuario-service:

**Windows PowerShell:**
```powershell
$resp = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" `
  -Method POST -ContentType "application/json" `
  -Body '{"email":"test@kidcare.com","password":"Password123"}'
$token = $resp.token
```

**Mac / Linux:**
```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@kidcare.com","password":"Password123"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
```

Prueba el endpoint protegido:

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8084/api/historial/menor/1" -Method GET -Headers @{Authorization="Bearer $token"}
```

**Mac / Linux:**
```bash
curl -s http://localhost:8084/api/historial/menor/1 -H "Authorization: Bearer $TOKEN"
```

Respuesta esperada: lista vacía `[]`.

Prueba también el endpoint público del médico (sin token):

**PowerShell:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8084/api/historial/medico/1" -Method GET
```

**Mac / Linux:**
```bash
curl -s http://localhost:8084/api/historial/medico/1
```

Respuesta esperada: `null` o `{}` — confirma que la ruta pública responde correctamente.

---

## Solución de problemas frecuentes

### Error: "Communications link failure"
MySQL no está corriendo. Ejecuta `docker ps` y verifica que `kidcare-mysql` aparece con estado `Up`. Si no aparece, repite el Paso 3.

### Error 401 al probar con token
Verifica que la clave `jwt.secret` en `application.properties` es exactamente: `kidcare-secret-key-2024-segura-32chars`

### Error: "Port 8084 already in use"
```powershell
netstat -ano | findstr :8084
taskkill /PID <numero> /F
```

### Error: "BUILD FAILURE"
Haz scroll hacia arriba para ver el error real. Verifica Java 21 y que MySQL está corriendo.
