-- Replace each password placeholder with the matching local value from .env.
-- Execute each CREATE DATABASE statement separately with autocommit enabled.
CREATE ROLE cms_writer LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT PASSWORD 'cms_writer';
CREATE ROLE cms_reader LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT PASSWORD 'cms_reader';
CREATE ROLE ingestion_user LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT PASSWORD 'ingestion_user';
CREATE ROLE auth_user LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT PASSWORD 'auth_user';
CREATE ROLE inventory_user LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT PASSWORD 'inventory_user';
CREATE ROLE order_user LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT PASSWORD 'order_user';
CREATE ROLE payment_user LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT PASSWORD 'payment_user';
CREATE ROLE notification_user LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT PASSWORD 'notification_user';

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
