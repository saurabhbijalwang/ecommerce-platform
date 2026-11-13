# PostgreSQL Local Infrastructure

Phase 3 provisions a local PostgreSQL 17 container and creates the service-owned databases and login roles.

## Databases and permissions

| Database | Owner/login | Access boundary |
| --- | --- | --- |
| `ecommerce_cms_db` | `cms_writer` | Sole catalog writer |
| `ecommerce_ingestion_db` | `ingestion_user` | Ingestion batch data only; no CMS database access |
| `ecommerce_auth_db` | `auth_user` | Auth-owned data only |
| `ecommerce_inventory_db` | `inventory_user` | Inventory-owned data only |
| `ecommerce_order_db` | `order_user` | Order-owned data only |
| `ecommerce_payment_db` | `payment_user` | Payment-owned data only |
| `ecommerce_notification_db` | `notification_user` | Notification-owned data only |

`cms_reader` has connect and schema usage privileges on the CMS database and default read-only privileges on tables created by `cms_writer`. It is not a catalog writer. The search service receives no PostgreSQL login; it reads from Elasticsearch and Redis.

Each application login is restricted to its own database. Public database connections are revoked. The bootstrap admin credentials are for local infrastructure administration, not application connections.

## Start

From the repository root in PowerShell:

```powershell
Copy-Item .env.example .env
notepad .env
docker compose up -d postgres
docker compose ps
docker compose logs -f postgres
```

Replace every placeholder in `.env` with a unique local-only password before starting the container. Never commit `.env` or use these local values in deployed environments.

The database initialization script runs only when the named volume is first created. After initialization, `docker compose up -d postgres` waits for PostgreSQL's health check before reporting the service healthy.

## Verify databases and roles

```powershell
docker compose exec postgres psql -U ecommerce_admin -d ecommerce_admin -c "SELECT datname FROM pg_database WHERE datistemplate = false ORDER BY datname;"
docker compose exec postgres psql -U ecommerce_admin -d ecommerce_admin -c "SELECT rolname FROM pg_roles WHERE rolname IN ('cms_writer', 'cms_reader', 'ingestion_user', 'auth_user', 'inventory_user', 'order_user', 'payment_user', 'notification_user') ORDER BY rolname;"
```

The CMS writer can connect to its database; the ingestion login cannot:

```powershell
docker compose exec postgres psql -U cms_writer -d ecommerce_cms_db -c "SELECT current_database(), current_user;"
docker compose exec postgres psql -U ingestion_user -d ecommerce_cms_db -c "SELECT 1;"
```

The last command is expected to fail with a permission error. Ingestion talks to CMS through its REST API and uses its own database only.

## Stop and reset local data

```powershell
docker compose down
```

This stops the container but keeps the named database volume. To intentionally reset local data, remove only this Compose project's named volume:

```powershell
docker compose down --volumes
```

This deletes the local PostgreSQL data stored in `postgres_data`. It does not affect a remote or production database.

## Troubleshooting

- **Port 5432 is already in use:** set `POSTGRES_PORT` to another available local port in `.env`.
- **Missing environment variable:** ensure every password placeholder in `.env` was replaced.
- **Database/role was not created after editing bootstrap:** initialization scripts run only for an empty data directory. Back up anything needed before resetting the local volume with `docker compose down --volumes`.
- **Application connection denied:** use that service's own login and database; never use the bootstrap admin account in an application.
