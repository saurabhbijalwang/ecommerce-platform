CREATE ROLE auth_user LOGIN PASSWORD 'auth_user_test_password'
    NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
ALTER DATABASE ecommerce_auth_db OWNER TO auth_user;
ALTER SCHEMA public OWNER TO auth_user;
