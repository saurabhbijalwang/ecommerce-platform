# CMS Service

CMS is the authoritative writer for the product catalog. It persists products, categories, brands, variants, media metadata, JSONB attributes, audit records, and immutable product snapshots in `ecommerce_cms_db`.

## Database setup

The service connects only as `cms_writer`; it does not use a PostgreSQL administrator account.

1. Create `ecommerce_cms_db` and the `cms_writer` role using the PostgreSQL initialization instructions in `infrastructure/postgres/init`.
2. If the database and roles already exist, execute `02-cms-schema-permissions.sql` while connected to `ecommerce_cms_db` as the database administrator.
3. Configure the password locally. It must match the password assigned to `cms_writer`:

   ```powershell
   $env:CMS_WRITER_PASSWORD = "<your local cms_writer password>"
   ```

`CMS_DB_URL`, `CMS_DB_USERNAME`, and `CMS_SERVER_PORT` can be set to override their local defaults. `CMS_WRITER_PASSWORD` has no default, so startup fails until it is configured. Flyway applies the versioned SQL migration, and Hibernate is configured to validate rather than create or alter tables.

## Run locally

Start PostgreSQL, Discovery Server, and Auth Service, then run CMS by itself from the repository root:

```powershell
$env:CMS_WRITER_PASSWORD = "<your local cms_writer password>"
mvn -pl services/cms-service spring-boot:run
```

If Maven reports that `common-lib` is missing from the local repository, install the reactor dependencies first with `mvn -pl services/cms-service -am install -DskipTests`, then rerun the standalone command. Do not combine `-am` with `spring-boot:run`.

CMS listens on `http://localhost:8083` by default and registers as `cms-service`. The API Gateway already routes `/api/v1/admin/catalog/**` to this service.

## Catalog API

All endpoints below are admin catalog routes:

| Method | Path | Purpose |
| --- | --- | --- |
| GET, POST | `/api/v1/admin/catalog/categories` | List or create categories |
| GET, PUT | `/api/v1/admin/catalog/categories/{id}` | Read or update a category |
| GET, POST | `/api/v1/admin/catalog/brands` | List or create brands |
| GET, PUT | `/api/v1/admin/catalog/brands/{id}` | Read or update a brand |
| GET, POST | `/api/v1/admin/catalog/products` | Search/list or create products |
| GET, PUT, DELETE | `/api/v1/admin/catalog/products/{id}` | Read, update, or archive a product |
| PATCH | `/api/v1/admin/catalog/products/{id}/status` | Change product lifecycle status |
| GET | `/api/v1/admin/catalog/products/{id}/versions` | Read immutable product snapshots |
| GET | `/api/v1/admin/catalog/products/{id}/audit` | Read product audit history |

Product listing supports `query`, `status`, `categoryId`, `brandId`, and Spring pagination parameters (`page`, `size`, `sort`). Page size is capped at 100.

Create a product with:

```json
{
  "productCode": "PHONE-001",
  "name": "Example Phone",
  "description": "128 GB model",
  "sku": "PHONE-001-BLK",
  "categoryId": "<category UUID>",
  "brandId": "<brand UUID>",
  "price": 899.99,
  "currency": "USD",
  "attributes": {
    "color": "black",
    "storage": "128 GB"
  },
  "variants": [
    {
      "sku": "PHONE-001-WHT",
      "name": "White, 128 GB",
      "price": 899.99,
      "attributes": {
        "color": "white",
        "storage": "128 GB"
      }
    }
  ],
  "media": [
    {
      "mediaUrl": "https://cdn.example.test/phone-front.png",
      "altText": "Front view",
      "mediaType": "IMAGE",
      "sortOrder": 0
    }
  ]
}
```

Updates use the same body and require `expectedVersion`, copied from the latest product response. A stale version returns HTTP 409 instead of silently overwriting a concurrent change. Deletion archives the product; it does not remove catalog history.

## Design and safety notes

- Flyway owns schema creation and migration. No cross-service foreign keys are created.
- Product writes, audit entries, and version snapshots share one transaction.
- Product price uses `BigDecimal`; flexible attributes and history snapshots use PostgreSQL `JSONB`.
- Audit actors use the authenticated JWT subject; service-layer operations without a request context use `system`.
- Both CMS and the gateway validate JWT issuer, audience, and signature. Catalog endpoints are restricted to `ADMIN` and `CONTENT_MANAGER`.
- Set `AUTH_JWT_ISSUER`, `AUTH_JWT_AUDIENCE`, and `AUTH_JWK_SET_URI` consistently with Auth Service.
- The transactional outbox and Kafka publishing are intentionally deferred to Phase 8.

## Verify

```powershell
mvn -pl services/auth-service,services/api-gateway,services/cms-service -am test
```

Request validation, JWT-key, and gateway request-context tests run without Docker. PostgreSQL/Flyway integration tests use Testcontainers and are skipped when Docker is unavailable.
