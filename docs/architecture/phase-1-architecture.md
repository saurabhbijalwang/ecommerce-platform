# Phase 1: Architecture Overview

## Architecture in Hindi/Hinglish

Phase 1 ka core focus yeh hai ki hum platform ko sahi microservice boundaries ke saath design karein. Is project mein har service apni responsibility ko clearly own karega, aur ek strong domain boundary maintain rahega. Product catalog ki write ownership sirf CMS service ko milegi. Ingestion service ko direct CMS database mein write karne ka permission nahi hoga; wo CMS REST APIs call karega. Search service customer-facing read/search ko Elasticsearch aur Redis se handle karega, direct CMS database se nahi. Isi tarah, inventory, order, payment, notification aur auth services apne own data domains maintain karenge. 

Yeh architecture event-driven hai: CMS aur ingestion ka data flow transactional outbox se Kafka events ko push karta hai, phir Elasticsearch indexing aur Redis cache sync services un events ko process karte hain. Is approach se database integrity, system decoupling aur eventual consistency maintain hoti hai. Gateway aur discovery layer external traffic aur service registration ko manage karte hain. Security layer auth and JWT ko centralize karta hai, aur observability stack logs, metrics aur tracing ko collect karta hai.

## Target architectural style

- Microservices with independent Maven modules
- Database-per-service model
- Domain ownership boundaries
- Event-driven synchronization using Kafka
- Search optimized through Elasticsearch and Redis
- Gateway and discovery pattern for service coordination
- Secure authentication via JWT and Spring Security
- Observability via OpenTelemetry, Micrometer, Zipkin, Prometheus, Grafana, Loki

## Service topology

1. Discovery Server
   - Eureka registry
   - Service discovery and health registration

2. API Gateway
   - TLS/security policy edge layer
   - Routing to auth, CMS, search, order and other APIs
   - Rate limiting/resilience and centralized filtering

3. Auth Service
   - User identity and JWT generation/validation
   - Role-based access control

4. CMS Service
   - Product catalog write authority
   - Categories, brands, attributes, variants, media metadata, pricing, status lifecycle
   - Product versions and audit trail

5. Ingestion Service
   - Product data intake for uploads, API ingestion, and scheduled jobs
   - Validation, normalization, idempotency records, batch lifecycle
   - Calls CMS APIs instead of writing CMS tables directly

6. Elasticsearch Indexing Service
   - Consumes Kafka events and updates product documents in Elasticsearch

7. Redis Cache Sync Service
   - Consumes Kafka events and syncs product cache data in Redis

8. Search Service
   - Product search APIs
   - Reads from Elasticsearch and Redis with fallback logic

9. Inventory Service
   - SKU stock, reservation, release, and stock reconciliation

10. Order Service
   - Cart/order management, order state transitions, saga orchestration

11. Payment Service
   - Payment authorization, settlement, and payment status handling

12. Notification Service
   - Email, SMS, push or dashboard notifications

## Critical ownership rules

These rules are non-negotiable:

- CMS is the only authorized writer of Product Catalog database.
- Ingestion Service must not directly access CMS catalog tables.
- Search Service must not query CMS database directly.
- Search Service reads from Elasticsearch and Redis.
- Catalog writes are propagated using transactional outbox + Kafka events.
- Elasticsearch and Redis are read-optimized caches/index layer, not system of record.

## Domain interaction model

The architecture intentionally separates the write path and read path:

- Write path: Ingestion or CMS -> CMS DB -> Outbox -> Kafka -> Elasticsearch/Redis
- Read path: Client -> API Gateway -> Search Service -> Elasticsearch/Redis -> response
- Order workflow: Order Service -> Inventory reservation -> Payment -> Notification

## High-level system context

- External clients interact through API Gateway.
- Auth Service secures protected routes.
- CMS owns catalog write operations.
- Ingestion feeds CMS through REST.
- Kafka propagates catalog events to downstream indexing and caching services.
- Search service serves customer queries with cached and indexed data.

## Important architectural decision

This platform uses a “single writer + eventual-consistency consumers” model for catalog data. This is a deliberate design because it avoids multiple services writing to the same product catalog tables, prevents data races, and makes search and cache layers consistent without coupling search consumers to the CMS database.

## Recommended technology stack

- Java 21
- Spring Boot 4.x / compatible Spring Cloud release train
- Maven multi-module build
- PostgreSQL per service database
- Flyway migrations
- Kafka + Avro + Schema Registry
- Elasticsearch
- Redis
- Spring Security + JWT
- Spring Cloud Gateway + Eureka
- OpenFeign / resilient HTTP clients
- Resilience4j
- OpenTelemetry + Micrometer Tracing + Zipkin
- Prometheus + Grafana + Loki + Promtail
- Docker Compose for infra stack

## Phase 1 acceptance criteria

This phase is considered complete when the project has:

- a documented architecture model;
- service ownership definitions;
- database ownership rules;
- Kafka event flow guidance;
- mermaid diagrams;
- a clear folder structure for all planned microservices.

This phase intentionally does not include Java code implementation.
