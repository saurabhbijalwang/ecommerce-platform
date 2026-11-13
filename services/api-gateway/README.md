# API Gateway

Reactive Spring Cloud Gateway registered with Eureka. It uses `lb://` service URIs so Spring Cloud LoadBalancer resolves service instances from the registry.

## Routes

| Public path | Eureka service |
| --- | --- |
| `/api/v1/auth/**` | `auth-service` |
| `/api/v1/products/**` | `search-service` |
| `/api/v1/admin/catalog/**` | `cms-service` |
| `/api/v1/orders/**` | `order-service` |

Auth and CMS register with Eureka in Phase 6. Other downstream services are added in later phases; matching requests cannot be forwarded until an instance registers.

## Security

The gateway validates bearer JWTs against Auth Service's JWKS endpoint and checks issuer, audience, and expiry. CMS catalog routes require `ADMIN` or `CONTENT_MANAGER`; order routes require `CUSTOMER`; product search and auth registration/login/refresh are public. CMS also enforces its own catalog role check, so direct service requests are protected.

Login, registration, and refresh have per-instance in-memory rate limits of 10, 5, and 30 requests per minute per remote IP. The limiter is suitable only for local development; it is not shared across gateway replicas. The gateway adds safe `X-Request-Id` and `X-Correlation-Id` headers to requests and responses.

Set `AUTH_JWT_ISSUER`, `AUTH_JWT_AUDIENCE`, and `AUTH_JWK_SET_URI` consistently with Auth Service. The default JWKS address is `http://localhost:8084/.well-known/jwks.json`. Keep the gateway local until TLS, shared rate limiting, and the remaining production controls are configured.

## Run

Start PostgreSQL, Discovery Server, and Auth Service first, then run from the repository root:

```bash
mvn -pl services/api-gateway spring-boot:run
```

The gateway listens on `http://localhost:8080`. Set `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` to override the default registry URL.
