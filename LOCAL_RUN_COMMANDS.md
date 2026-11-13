# Local Run Commands

Follow these steps from the repository root in PowerShell. Keep real passwords and private keys out of this file and out of Git.

## 1. Check Java and Maven

Maven 3.10.0 is installed under your user profile. Add it to the current terminal if `mvn` is not recognized:

```powershell
cd D:\Development\2026\Interview\ecommerce-platform
$env:Path = "$HOME\tools\apache-maven-3.10.0\bin;$env:Path"
java -version
mvn -version
```

## 2. Make the Auth database password match

PostgreSQL must be running, with database `ecommerce_auth_db` and login role `auth_user`. The password used for the database role must exactly match `AUTH_DB_PASSWORD`.

If you see `FATAL: password authentication failed for user "auth_user"`, open pgAdmin's Query Tool as a PostgreSQL administrator and run this SQL after replacing the placeholder:

```sql
ALTER ROLE auth_user WITH PASSWORD 'REPLACE_WITH_YOUR_AUTH_DB_PASSWORD';
```

Do not run psql commands such as `\getenv` in pgAdmin; those are psql client commands, not SQL. Do not delete the database or volume to fix a password mismatch.

## 3. Start Discovery Server

Use a dedicated PowerShell terminal and keep it open:

```powershell
cd D:\Development\2026\Interview\ecommerce-platform
$env:Path = "$HOME\tools\apache-maven-3.10.0\bin;$env:Path"
mvn -pl services/discovery-server spring-boot:run
```

Do not add `-am` to `spring-boot:run`; Maven may apply that goal to the parent POM, which has no main class. Discovery Server uses port `8761` by default.

## 4. Generate JWT keys and configure Auth

Open a second PowerShell terminal. The `$env:` settings below must be in the same terminal that runs Auth Service.

If these key files do not already exist, use the installed JDK to generate them once; this does not require OpenSSL. Copy the full block into PowerShell:

```powershell
$generatorSource = Join-Path $env:TEMP 'GenerateJwtKeys.java'
@'
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPairGenerator;

public class GenerateJwtKeys {
    public static void main(String[] args) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(3072);
        var keyPair = generator.generateKeyPair();
        Files.write(Path.of(args[0]), keyPair.getPrivate().getEncoded());
        Files.write(Path.of(args[1]), keyPair.getPublic().getEncoded());
    }
}
'@ | Set-Content -LiteralPath $generatorSource -Encoding ascii

javac $generatorSource
if ($LASTEXITCODE -ne 0) { throw 'Could not compile the temporary JWT key generator.' }

java -cp $env:TEMP GenerateJwtKeys "$env:TEMP\auth-private.der" "$env:TEMP\auth-public.der"
if ($LASTEXITCODE -ne 0) { throw 'Could not generate the JWT RSA key files.' }
```

Set the JWT key variables from the DER files:

```powershell
$env:AUTH_JWT_PRIVATE_KEY_BASE64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes("$env:TEMP\auth-private.der"))
$env:AUTH_JWT_PUBLIC_KEY_BASE64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes("$env:TEMP\auth-public.der"))
```

Set the database password to the same value used in step 2, and choose a bootstrap admin email and password (at least 12 characters):

```powershell
$env:AUTH_DB_PASSWORD = "<your local auth_user password>"
$env:AUTH_BOOTSTRAP_ADMIN_EMAIL = "admin@example.com"
$env:AUTH_BOOTSTRAP_ADMIN_PASSWORD = "<strong password of at least 12 characters>"
```

Keep the private key local; do not share or commit it. Do not regenerate keys on every start. More details are in [the Auth Service README](services/auth-service/README.md).

## 5. Start Auth Service

In the second terminal, with the environment variables from step 4 set, run Auth Service:

```powershell
mvn -pl services/auth-service spring-boot:run
```

Auth Service uses port `8084` by default. Leave this terminal open while using the service.

If Maven reports that `common-lib` is missing, check for the local artifact:

```powershell
$commonLibJar = Join-Path $HOME '.m2\repository\com\ecommerce\platform\common-lib\1.0.0-SNAPSHOT\common-lib-1.0.0-SNAPSHOT.jar'
Test-Path -LiteralPath $commonLibJar
```

Only if the result is `False`, build and install the reactor dependency, then run Auth Service again:

```powershell
mvn -pl services/auth-service -am install -DskipTests
mvn -pl services/auth-service spring-boot:run
```

If that install cannot replace a `.m2` JAR because of a `.tmp`-to-`.jar` error, stop duplicate Maven builds or services using `common-lib` first. Do not delete the whole `.m2` repository.

## 6. Verify Auth Service

From a third PowerShell terminal:

```powershell
Invoke-RestMethod "http://localhost:8084/actuator/health"
Invoke-RestMethod "http://localhost:8084/.well-known/jwks.json"
```

The health endpoint should report `UP`; the JWKS endpoint should return a public key set. Do not paste private key values into chat or logs.

## Optional: start Gateway and CMS

Start these only after Discovery and Auth are running. CMS also requires PostgreSQL and `CMS_WRITER_PASSWORD` to match the `cms_writer` database role.

API Gateway, in another terminal:

```powershell
cd D:\Development\2026\Interview\ecommerce-platform
$env:Path = "$HOME\tools\apache-maven-3.10.0\bin;$env:Path"
mvn -pl services/api-gateway spring-boot:run
```

CMS, in another terminal:

```powershell
cd D:\Development\2026\Interview\ecommerce-platform
$env:Path = "$HOME\tools\apache-maven-3.10.0\bin;$env:Path"
$env:CMS_WRITER_PASSWORD = "<your local cms_writer password>"
mvn -pl services/cms-service spring-boot:run
```

If Maven reports that `common-lib` is missing, check whether its JAR is already in the local Maven repository:

```powershell
$commonLibJar = Join-Path $HOME '.m2\repository\com\ecommerce\platform\common-lib\1.0.0-SNAPSHOT\common-lib-1.0.0-SNAPSHOT.jar'
Test-Path -LiteralPath $commonLibJar
```

If the result is `False`, build and install dependencies before starting CMS:

```powershell
mvn -pl services/cms-service -am install -DskipTests
mvn -pl services/cms-service spring-boot:run
```

If that install cannot replace a `.m2` JAR because of a `.tmp`-to-`.jar` error, stop duplicate Maven builds or services using `common-lib` first. Do not delete the whole `.m2` repository.
