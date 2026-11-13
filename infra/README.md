# Infrastructure

This directory will contain deployment assets, environment definitions, service configs, and platform runtime conventions.

## PostgreSQL initialization

For pgAdmin, use [`infrastructure/postgres/init/01-init-databases.sql`](../infrastructure/postgres/init/01-init-databases.sql) to create the service roles and databases, then connect to `ecommerce_cms_db` and run [`infrastructure/postgres/init/02-cms-schema-permissions.sql`](../infrastructure/postgres/init/02-cms-schema-permissions.sql).

Run this once against an uninitialized PostgreSQL cluster as an administrator. Replace each `REPLACE_WITH_...` password in the first script with the matching local value from `.env` (if a value contains a single quote, double it in the SQL literal). In pgAdmin, run the `CREATE ROLE` statements, then execute each `CREATE DATABASE` statement separately with autocommit enabled; run its following `REVOKE` and `GRANT` statements separately as well.

Docker Compose retains its environment-backed initialization script and runs it when it initializes a fresh PostgreSQL data volume.
