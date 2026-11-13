# Phase 3: Docker and PostgreSQL Initialization

## Architecture (Hindi/Hinglish)

Is phase mein local development ke liye Docker Compose se ek PostgreSQL container start hota hai. Container first initialization par har service ke liye alag database aur login create karta hai. CMS catalog database ka owner `cms_writer` hai; `cms_reader` ko read-only access milta hai. Ingestion aur baaki application users ko sirf apne database ka access hai. Search ko PostgreSQL credentials nahi diye jaate—woh Elasticsearch aur Redis use karega.

Passwords `.env` se aate hain, `.env.example` mein sirf placeholders hain, aur PostgreSQL host port loopback (`127.0.0.1`) par bind hota hai. Docker named volume database ko container restarts ke across persist karta hai. Yeh local infrastructure hai; services, Kafka, Elasticsearch, Redis aur monitoring containers apne later phases mein add honge.

## Folder structure

```text
.
├── .env.example
├── docker-compose.yml
├── infrastructure/
│   └── postgres/
│       ├── README.md
│       └── init/
│           └── 00-init-databases.sh
└── docs/
    ├── environment-variables.md
    └── phase-3-local-setup.md
```

## Maven dependencies

None. Phase 3 only adds local infrastructure configuration and PostgreSQL bootstrap SQL/permissions; it does not change Maven modules.

## Configuration and initialization

- `docker-compose.yml` runs `postgres:17-alpine`, persists data in `postgres_data`, publishes the database only on loopback, and uses `pg_isready` for health checks.
- `infrastructure/postgres/init/00-init-databases.sh` creates isolated application roles/databases and configures the CMS reader's default table grants.
- `.env.example` lists local settings without including usable credentials. Copy it to `.env` and replace every password placeholder.
- Initialization runs only when PostgreSQL first initializes an empty data volume.

## Run and verify

See [PostgreSQL Local Infrastructure](../infrastructure/postgres/README.md) for PowerShell commands to start, inspect, and verify database permissions.

Expected database names:

```text
ecommerce_admin
ecommerce_auth_db
ecommerce_cms_db
ecommerce_ingestion_db
ecommerce_inventory_db
ecommerce_notification_db
ecommerce_order_db
ecommerce_payment_db
```

An ingestion connection attempt against `ecommerce_cms_db` should fail. This confirms ingestion must use the CMS REST API rather than touching catalog tables.

## Tests and verification status

Docker Compose validation could not be run in this environment because Docker is not installed. Run `docker compose config` after installing Docker Desktop to validate the Compose file, then use the database verification commands in the infrastructure README. No container startup or runtime test is claimed.

## Common errors

- **Docker command not found:** install and start Docker Desktop, then reopen the terminal.
- **Port collision:** select an available port through `POSTGRES_PORT`.
- **Initialization did not rerun:** Compose init scripts are first-boot-only. Preserve needed data; reset only the local volume if a clean bootstrap is intended.
- **Unexpected application database permission:** confirm the service uses its dedicated DB/user pair rather than the bootstrap admin login.

## Pending for later phases

Kafka, Schema Registry, Elasticsearch, Redis, service containers, and observability containers are intentionally not included yet.
