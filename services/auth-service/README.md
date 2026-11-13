# Auth Service

Auth Service owns user accounts and refresh-token state in `ecommerce_auth_db`. It signs short-lived RS256 access tokens, exposes the corresponding public key as a JWKS, and rotates opaque refresh tokens whose hashes are stored in PostgreSQL.

## Local setup

The service uses the `auth_user` PostgreSQL role and `ecommerce_auth_db`. Create them with the PostgreSQL bootstrap instructions in [`infrastructure/postgres/README.md`](../../infrastructure/postgres/README.md) if they do not already exist. Set `AUTH_DB_PASSWORD` to the same local password assigned to `auth_user`.

Configure a bootstrap administrator and an RSA key pair locally; never commit the private key or passwords.

Generate a 3072-bit RSA key pair in the temporary directory using the installed JDK. The full PowerShell key-generation and environment setup commands are in [Local Run Commands](../../LOCAL_RUN_COMMANDS.md).

First run the key-generation commands from Step 4 of Local Run Commands; they create `auth-private.der` and `auth-public.der` in the temporary directory. Only then run these environment assignments in that same PowerShell session:

```powershell
$env:AUTH_DB_PASSWORD = "<your local auth_user password>"
$env:AUTH_BOOTSTRAP_ADMIN_EMAIL = "admin@example.com"
$env:AUTH_BOOTSTRAP_ADMIN_PASSWORD = "<strong password of at least 12 characters>"
$env:AUTH_JWT_PRIVATE_KEY_BASE64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes("$env:TEMP\auth-private.der"))
$env:AUTH_JWT_PUBLIC_KEY_BASE64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes("$env:TEMP\auth-public.der"))
```

Start PostgreSQL and Discovery Server first, then run Auth Service by itself from the repository root:

```powershell
mvn -pl services/auth-service spring-boot:run
```

If Maven reports that `common-lib` is missing from the local repository, install the reactor dependencies first with `mvn -pl services/auth-service -am install -DskipTests`, then rerun the standalone command. Do not combine `-am` with `spring-boot:run`.

The first launch applies the Flyway migration. Set `AUTH_DB_URL`, `AUTH_DB_USERNAME`, `AUTH_SERVER_PORT`, `AUTH_JWT_ISSUER`, `AUTH_JWT_AUDIENCE`, `AUTH_ACCESS_TOKEN_TTL`, or `AUTH_REFRESH_TOKEN_TTL` to override defaults. If both bootstrap administrator variables are omitted, startup logs a warning and no administrator is created; setting only one is a startup error.

## Endpoints

| Method | Path | Access |
| --- | --- | --- |
| POST | `/api/v1/auth/register` | Public; always creates a `CUSTOMER` |
| POST | `/api/v1/auth/login` | Public; email/password login |
| POST | `/api/v1/auth/refresh` | Public; rotates the supplied refresh token |
| POST | `/api/v1/auth/logout` | Public; revokes the supplied refresh token |
| GET | `/api/v1/auth/me` | Authenticated |
| POST | `/api/v1/auth/users` | `ADMIN`; provisions users with a selected role |
| GET | `/.well-known/jwks.json` | Public; signing public key only |

Public registration cannot assign privileged roles. `ADMIN` can provision `ADMIN`, `CONTENT_MANAGER`, `CUSTOMER`, or `SERVICE` accounts. Service clients use a dedicated `SERVICE` account and the same login/token flow; do not reuse a customer identity.

Example registration:

```json
{
  "email": "customer@example.com",
  "password": "a-strong-password-2026"
}
```

Passwords are BCrypt-hashed, must contain at least 12 characters, and are limited to 72 UTF-8 bytes. Refresh values are random opaque tokens; only SHA-256 hashes are persisted. Refresh is single-use: reuse revokes the entire token family.

## Gateway and CMS integration

The gateway and CMS verify the JWT signature using the JWKS endpoint and independently validate issuer, audience, and expiry. Set `AUTH_JWK_SET_URI` when the services do not share the local default address. CMS catalog routes require `ADMIN` or `CONTENT_MANAGER` at both the gateway and CMS service.

The gateway applies per-instance in-memory limits to login (10/minute), registration (5/minute), and refresh (30/minute) per remote IP. These limits are a local-development baseline, not a distributed production limiter; replace them with a shared store before horizontal scaling or public deployment. Use TLS and a managed key-rotation process in deployed environments.

## Verify

```powershell
mvn -pl services/auth-service,services/api-gateway,services/cms-service -am test
```

JWT-key and gateway request-context tests run without Docker. PostgreSQL/Flyway integration tests use Testcontainers and are skipped if Docker is unavailable.
