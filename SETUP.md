# Guía paso a paso: poner en marcha el proyecto

Probado en Windows 11 + PowerShell 5.1. Todos los comandos se ejecutan desde la
raíz del repositorio, salvo indicación contraria.

## 0. Prerrequisitos

1. **Docker Desktop** en ejecución: `docker --version` y `docker compose version`.
   Java 17 y Maven se ejecutan dentro del contenedor, así que no es necesario
   instalarlos en Windows.
2. Puertos libres en el host: **3306** (MySQL), **27017** (MongoDB) y **8080**
   (app). Cómo comprobarlo:
   ```powershell
   Get-NetTCPConnection -LocalPort 27017,3306,8080 -State Listen |
     Select-Object LocalAddress, LocalPort, OwningProcess
   ```
   Si el 8080 está ocupado, establece `$env:HOST_SERVER_PORT="8081"` antes de
   levantar Compose. Para un `mongod` ajeno en el puerto 27017, detén ese
   proceso para evitar conflictos.

## 1. Levantar la aplicación y las bases de datos

```powershell
docker compose up -d --build
docker ps --format "{{.Names}}|{{.Status}}"
docker compose exec bank-app java -version
docker compose exec bank-app mvn -version
docker exec bank-mongo mongosh --quiet --eval "db.runCommand({ping:1})"
docker exec bank-mysql mysql -uroot -proot_password -e "SELECT VERSION();"
```

Esto crea MySQL y MongoDB y arranca la app en `http://localhost:8080`. El
contenedor `bank-app` incluye Java 17 y Maven; el código de `bank/` está montado
en `/workspace`. Credenciales por defecto de la app: `bank_app` /
`bank_app_password` (sobrescribibles con `MYSQL_PASSWORD`).

## 2. Compilar y correr las pruebas

```powershell
docker compose exec bank-app mvn test
```

Resultado esperado: `BUILD SUCCESS`. La prueba `BankApplicationTests.contextLoads` necesita MySQL
arriba (paso 1); sin base de datos ese test falla por conexión, el resto pasa.

## 3. Verificar la aplicación en ejecución

```powershell
docker compose logs -f bank-app
```

La app se inicia con `mvn spring-boot:run` dentro del contenedor. Hibernate
crea/actualiza las tablas en `bank_db` y Mongo queda listo para crear
`audit_logs` con el primer evento. Verificar salud:

```powershell
curl.exe -s http://127.0.0.1:8080/actuator/health
# {"groups":["liveness","readiness"],"status":"UP"}
```

Los comandos Java y Maven también están disponibles en el contenedor:

```powershell
docker compose exec bank-app java -version
docker compose exec bank-app mvn -version
docker compose exec bank-app mvn -DskipTests package
```

> `$env:JWT_SECRET` tiene un valor por defecto `TDEATDEATDEATDEATDEATDEATDEATDEA`
> (netamente educativo, proyecto académico) para poder arrancar sin
> configuración adicional, pero ese valor **es público** (está en el
> repositorio): cualquiera que lo lea puede forjar tokens válidos. Defínelo
> con tu propia clave aleatoria si vas a exponer el servicio fuera de tu
> máquina (por ejemplo
> `$env:JWT_SECRET = -join ((48..57) + (65..90) + (97..122) | Get-Random -Count 48 | ForEach-Object {[char]$_})`
> antes de `docker compose up`). Variables opcionales en la misma sesión:
> `$env:DB_URL`, `$env:DB_USERNAME`, `$env:DB_PASSWORD`, `$env:MONGODB_URI`,
> `$env:FRONTEND_ORIGIN`. Ver tabla en `README.md`.

## 4. Prueba de humo end-to-end (registro → login → uso → logout)

> En PowerShell, pasar JSON inline a `curl.exe` suele corromper las comillas.
> Usar **archivos** para los bodies (`-d "@ruta\file.json"`).

1. Crear los bodies (una sola vez):
   ```powershell
   $tmp = "$env:TEMP\bank-smoke"
   New-Item -ItemType Directory -Path $tmp -Force
   Set-Content "$tmp\customer.json" -Value '{"identification":"1017123456","name":"Juan Perez","email":"juan.perez@example.com","phoneNumber":"3001234567","address":"Calle 50 40-20","birthDate":"1995-08-15"}' -NoNewline
   Set-Content "$tmp\user.json" -Value '{"customerIdentification":"1017123456","username":"juanptest","password":"SecurePass123!","role":"NATURAL_CUSTOMER"}' -NoNewline
   Set-Content "$tmp\login.json" -Value '{"username":"juanptest","password":"SecurePass123!"}' -NoNewline
   ```
   (Si repites la prueba, cambia `identification`/`username` por valores nuevos.)
2. Registrar cliente → **201**:
   ```powershell
   curl.exe -s -w "`nHTTP:%{http_code}`n" -X POST http://127.0.0.1:8080/api/v1/auth/register/natural-customer -H "Content-Type: application/json" -d "@$tmp\customer.json"
   ```
3. Registrar usuario → **201**:
   ```powershell
   curl.exe -s -w "`nHTTP:%{http_code}`n" -X POST http://127.0.0.1:8080/api/v1/auth/register/user -H "Content-Type: application/json" -d "@$tmp\user.json"
   ```
4. Login → **200** con `token`, `user` y `expiresIn: 3600`:
   ```powershell
   $login = curl.exe -s -X POST http://127.0.0.1:8080/api/v1/auth/login -H "Content-Type: application/json" -d "@$tmp\login.json"
   $token = ($login | ConvertFrom-Json).token
   ```
5. Endpoint protegido con token → **200** (sin token → **403**):
   ```powershell
   curl.exe -s -w "`nHTTP:%{http_code}`n" http://127.0.0.1:8080/api/v1/natural-customer/profile -H "Authorization: Bearer $token"
   ```
6. Logout → **204**; el token viejo queda invalidado → **403**:
   ```powershell
   curl.exe -s -o NUL -w "HTTP:%{http_code}`n" -X POST http://127.0.0.1:8080/api/v1/auth/logout -H "Authorization: Bearer $token"
   curl.exe -s -o NUL -w "HTTP:%{http_code}`n" http://127.0.0.1:8080/api/v1/natural-customer/profile -H "Authorization: Bearer $token"
   ```
7. Verificar persistencia:
   ```powershell
   docker exec bank-mysql mysql -uroot -proot_password --batch -e "SHOW TABLES FROM bank_db; SELECT user_id, username, auth_token_version, status FROM bank_db.users;"
   docker exec bank-mongo mongosh --quiet audit_db --eval "db.audit_logs.countDocuments()"
   ```

## 5. Apagar todo

```powershell
docker compose down        # los volúmenes con datos se conservan
```

## 6. Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `Port 8080 was already in use` | Otro proceso ocupa el puerto | Ejecutar `$env:HOST_SERVER_PORT="8081"` antes de `docker compose up -d --build` |
| `Tests ... BankApplicationTests ... Communications link failure` | MySQL apagado | `docker compose up -d` y repetir |
| Auditoría no aparece en `bank-mongo` | Otro `mongod` nativo ocupa `127.0.0.1:27017` | Arrancar con `$env:MONGODB_URI="mongodb://[::1]:27017/audit_db"` o detener el proceso ajeno (requiere admin) |
| `400 Requesting user must be provided` en registro público | Versión vieja del código | Debe permitir auto-registro; reconstruir con `.\mvnw.cmd -q -DskipTests package` |
| Columnas legacy (`userId`, `passwordHash`) o errores DDL `user_id` | Schema de una versión anterior | Solo en desarrollo: `docker exec bank-mysql mysql -uroot -proot_password -e "DROP DATABASE bank_db;"` y rearrancar (el auto-DDL lo recrea) |
| `curl.exe` devuelve 400 con JSON inline en PowerShell | Comillas mutiladas por la shell | Usar `-d "@archivo.json"` como en el paso 4 |
