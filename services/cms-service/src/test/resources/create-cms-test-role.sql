CREATE ROLE cms_writer LOGIN PASSWORD 'cms_writer_test_password'
    NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
ALTER DATABASE ecommerce_cms_db OWNER TO cms_writer;
ALTER SCHEMA public OWNER TO cms_writer;
