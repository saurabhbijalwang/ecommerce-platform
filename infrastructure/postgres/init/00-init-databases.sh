#!/usr/bin/env bash
set -Eeuo pipefail

required_env_vars=(
  CMS_WRITER_PASSWORD
  CMS_READER_PASSWORD
  INGESTION_DB_PASSWORD
  AUTH_DB_PASSWORD
  INVENTORY_DB_PASSWORD
  ORDER_DB_PASSWORD
  PAYMENT_DB_PASSWORD
  NOTIFICATION_DB_PASSWORD
)

for variable in "${required_env_vars[@]}"; do
  if [[ -z "${!variable:-}" ]]; then
    printf 'Required environment variable %s is not set\n' "$variable" >&2
    exit 1
  fi
done

psql \
  --set=ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" <<'SQL'
\getenv cms_writer_password CMS_WRITER_PASSWORD
\getenv cms_reader_password CMS_READER_PASSWORD
\getenv ingestion_db_password INGESTION_DB_PASSWORD
\getenv auth_db_password AUTH_DB_PASSWORD
\getenv inventory_db_password INVENTORY_DB_PASSWORD
\getenv order_db_password ORDER_DB_PASSWORD
\getenv payment_db_password PAYMENT_DB_PASSWORD
\getenv notification_db_password NOTIFICATION_DB_PASSWORD

CREATE ROLE cms_writer LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT PASSWORD :'cms_writer_password';
CREATE ROLE cms_reader LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT PASSWORD :'cms_reader_password';
CREATE ROLE ingestion_user LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT PASSWORD :'ingestion_db_password';
CREATE ROLE auth_user LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT PASSWORD :'auth_db_password';
CREATE ROLE inventory_user LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT PASSWORD :'inventory_db_password';
CREATE ROLE order_user LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT PASSWORD :'order_db_password';
CREATE ROLE payment_user LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT PASSWORD :'payment_db_password';
CREATE ROLE notification_user LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT PASSWORD :'notification_db_password';

CREATE DATABASE ecommerce_cms_db OWNER cms_writer;
REVOKE ALL ON DATABASE ecommerce_cms_db FROM PUBLIC;
GRANT CONNECT ON DATABASE ecommerce_cms_db TO cms_writer, cms_reader;

CREATE DATABASE ecommerce_ingestion_db OWNER ingestion_user;
REVOKE ALL ON DATABASE ecommerce_ingestion_db FROM PUBLIC;
GRANT CONNECT ON DATABASE ecommerce_ingestion_db TO ingestion_user;

CREATE DATABASE ecommerce_auth_db OWNER auth_user;
REVOKE ALL ON DATABASE ecommerce_auth_db FROM PUBLIC;
GRANT CONNECT ON DATABASE ecommerce_auth_db TO auth_user;

CREATE DATABASE ecommerce_inventory_db OWNER inventory_user;
REVOKE ALL ON DATABASE ecommerce_inventory_db FROM PUBLIC;
GRANT CONNECT ON DATABASE ecommerce_inventory_db TO inventory_user;

CREATE DATABASE ecommerce_order_db OWNER order_user;
REVOKE ALL ON DATABASE ecommerce_order_db FROM PUBLIC;
GRANT CONNECT ON DATABASE ecommerce_order_db TO order_user;

CREATE DATABASE ecommerce_payment_db OWNER payment_user;
REVOKE ALL ON DATABASE ecommerce_payment_db FROM PUBLIC;
GRANT CONNECT ON DATABASE ecommerce_payment_db TO payment_user;

CREATE DATABASE ecommerce_notification_db OWNER notification_user;
REVOKE ALL ON DATABASE ecommerce_notification_db FROM PUBLIC;
GRANT CONNECT ON DATABASE ecommerce_notification_db TO notification_user;

\connect ecommerce_cms_db
REVOKE ALL ON SCHEMA public FROM PUBLIC;
GRANT USAGE, CREATE ON SCHEMA public TO cms_writer;
GRANT USAGE ON SCHEMA public TO cms_reader;
ALTER DEFAULT PRIVILEGES FOR ROLE cms_writer IN SCHEMA public
  GRANT SELECT ON TABLES TO cms_reader;
SQL
