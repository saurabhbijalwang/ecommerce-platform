# API Gateway

Reactive Spring Cloud Gateway registered with Eureka. It uses `lb://` service URIs so Spring Cloud LoadBalancer resolves service instances from the registry.

## Routes

| Public path | Eureka service |
| --- | --- |
| `/api/v1/auth/**` | `auth-service` |
| `/api/v1/products/**` | `search-service` |
| `/api/v1/admin/catalog/**` | `cms-service` |
| `/api/v1/orders/**` | `order-service` |

These downstream services are added in later phases; until they register with Eureka, matching requests cannot be forwarded.

JWT validation and rate limiting are scheduled for the authentication phase; do not expose this gateway outside the local development environment yet.

## Run

Start the Discovery Server first, then run from the repository root:

```bash
mvn -pl services/api-gateway spring-boot:run
```

The gateway listens on `http://localhost:8080`. Set `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` to override the default registry URL.
