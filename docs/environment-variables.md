# Environment Variables

Copy `.env.example` to `.env` for local PostgreSQL infrastructure. Compose reads `.env` from the repository root. `.env` is ignored by Git; `.env.example` contains placeholders only.

| Variable | Required | Purpose |
| --- | --- | --- |
| `POSTGRES_ADMIN_USER` | No | Bootstrap administrator login; defaults to `ecommerce_admin` |
| `POSTGRES_ADMIN_PASSWORD` | Yes | Local PostgreSQL bootstrap administrator password |
| `POSTGRES_PORT` | No | Host port bound to loopback; defaults to `5432` |
| `CMS_WRITER_PASSWORD` | Yes | Password for the CMS catalog writer |
| `CMS_READER_PASSWORD` | Yes | Password for the CMS read-only login |
| `INGESTION_DB_PASSWORD` | Yes | Password for the ingestion database login |
| `AUTH_DB_PASSWORD` | Yes | Password for the auth database login |
| `INVENTORY_DB_PASSWORD` | Yes | Password for the inventory database login |
| `ORDER_DB_PASSWORD` | Yes | Password for the order database login |
| `PAYMENT_DB_PASSWORD` | Yes | Password for the payment database login |
| `NOTIFICATION_DB_PASSWORD` | Yes | Password for the notification database login |

Use distinct, strong values for each password. These values are for local development; production secrets must be injected through a dedicated secret manager and must never be committed.
