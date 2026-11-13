# ENTERPRISE E-COMMERCE MICROSERVICES PLATFORM — MASTER DEVELOPMENT PROMPT

Act as a Principal Java Architect, Senior Spring Boot Developer, Microservices Expert, Kafka/Avro Architect, Database Architect, DevOps Engineer and Technical Mentor.

Build a complete, production-oriented E-Commerce Order Management Platform using Java 21, Spring Boot 4.x, Spring Cloud, Maven and a microservices architecture.

This is a real implementation task, not a theoretical demonstration. Generate working code, configurations, database migrations, tests, Docker setup, API documentation and detailed technical documentation.

Explain the implementation in Hindi/Hinglish. Keep code, class names, methods, variables, APIs and technical terminology in standard English.

## 1. Mandatory Technology Stack

- Java 21
- Latest stable compatible Spring Boot 4.x patch release
- Compatible Spring Cloud release train
- Maven
- Spring Data JPA and Hibernate
- PostgreSQL
- Flyway database migrations
- Apache Kafka
- Apache Avro serialization
- Confluent-compatible Schema Registry
- Elasticsearch
- Redis
- Spring Security and JWT
- Spring Cloud Gateway
- Eureka Discovery Server
- OpenFeign or supported HTTP clients
- Resilience4j
- OpenTelemetry
- Micrometer Tracing
- Zipkin
- Prometheus and Grafana
- Centralized logging using Grafana Loki and Promtail or a compatible supported log collector
- Docker and Docker Compose
- Kafka UI
- JUnit 5, Mockito and Testcontainers

IMPORTANT VERSION RULE:
Before creating Maven files, verify the official compatibility matrices for Spring Boot, Spring Cloud, Spring Kafka, Spring Security, Spring Data and third-party libraries. Do not combine incompatible versions. Prefer Spring Boot dependency management and compatible BOMs. Do not manually override dependency versions without a technical reason.

## 2. Microservices

Create these independent services:

1. discovery-server
2. api-gateway
3. auth-service
4. cms-service
5. ingestion-service
6. search-service
7. inventory-service
8. order-service
9. payment-service
10. notification-service
11. elasticsearch-indexing-service
12. redis-cache-sync-service

Each service must have:
- Independent Maven project/module
- Own application.yml
- Own Dockerfile
- Proper package structure
- Controller, service, repository, entity, DTO, mapper, config and exception layers where applicable
- Health checks and Actuator
- Validation
- Structured logging
- Unit and integration tests
- Security
- Database ownership rules
- Eureka registration wherever applicable

Do not create unnecessary duplicate services. CMS owns the product catalog. Search Service owns customer-facing product read/search APIs.

## 3. Critical Product Catalog Ownership Rules

CMS is the ONLY authorized writer of the Product Catalog database.

CMS handles:
- Product creation
- Product updates
- Product deletion
- Product publishing/unpublishing
- Categories
- Brands
- Product attributes
- Product variants
- Product media metadata
- Product price and currency
- Product audit history
- Product versioning
- Approval workflow where required

Ingestion Service must NEVER write directly to the CMS database.

Ingestion Service must call CMS REST APIs to create or update product catalog data.

Customer-facing product reads and search must happen through Search Service.

Search Service must read from Elasticsearch and Redis, not directly query the CMS database.

CMS may read its own product data for content-management operations.

Use separate PostgreSQL database users and enforce permissions:
- cms_writer: write access to CMS catalog tables
- cms_reader: read access only where required
- ingestion_user: no direct catalog database access
- search_user: no CMS catalog database access
- Other services: no unauthorized cross-service database access

Do not use shared database tables across microservices.

## 4. Database Design

Use PostgreSQL with separate logical databases:

- ecommerce_cms_db
- ecommerce_ingestion_db
- ecommerce_auth_db
- ecommerce_inventory_db
- ecommerce_order_db
- ecommerce_payment_db
- ecommerce_notification_db

Use Spring Data JPA and Hibernate for transactional relational data.

Use Flyway for all schema creation and migrations.

Use UUID primary keys where appropriate.

Use:
- Proper foreign keys within service-owned databases
- Unique constraints
- Indexes
- Optimistic locking where appropriate
- Database transactions
- JSONB for flexible product attributes
- Audit timestamps
- CreatedBy and UpdatedBy where applicable

Do not create cross-service foreign keys.

Use BigDecimal for monetary values.

Never use floating-point types for money.

Do not store raw card numbers, CVV or sensitive payment credentials.

Elasticsearch and Redis are not transactional sources of truth.

## 5. Product Ingestion Workflow

Support:
- JSON file upload
- Bulk JSON ingestion
- External REST API ingestion
- Manual ingestion
- Scheduled ingestion
- Future extensibility for CSV and Excel

Sample product JSON:

{
  "productId": "PROD-1001",
  "name": "Samsung Galaxy S26",
  "description": "Smartphone",
  "category": "Mobile",
  "brand": "Samsung",
  "sku": "SAM-S26-256",
  "price": 79999,
  "currency": "INR",
  "attributes": {
    "color": "Black",
    "storage": "256GB"
  }
}

Processing workflow:

JSON Input
→ Schema Validation
→ Mandatory Field Validation
→ Duplicate Detection
→ Data Normalization
→ Ingestion Batch Creation
→ CMS REST API
→ CMS Validation
→ CMS PostgreSQL Save
→ Transactional Outbox
→ Kafka Avro Event
→ Elasticsearch Indexing
→ Redis Cache Synchronization

Implement:
- Batch processing
- Configurable batch size
- Idempotency
- Retry
- Failure tracking
- Error reports
- Pause and resume
- Scheduled jobs
- Batch status
- Audit history
- Duplicate SKU detection
- Duplicate product ID detection
- Partial batch failure handling

Provide APIs:

POST /api/ingestion/upload
POST /api/ingestion/{batchId}/start
POST /api/ingestion/{batchId}/pause
POST /api/ingestion/{batchId}/resume
GET /api/ingestion/{batchId}/status
GET /api/ingestion/{batchId}/errors

Ingestion Service must call CMS APIs. It must not directly access or modify product catalog tables.

## 6. CMS and Transactional Outbox

Implement the Transactional Outbox Pattern.

When CMS creates or updates a product:
1. Save product data.
2. Save the corresponding outbox event.
3. Both operations must happen in the same PostgreSQL transaction.
4. A separate publisher publishes pending events to Kafka.
5. Mark events as published only after successful publishing.

Outbox fields should include:
- eventId
- aggregateId
- aggregateVersion
- eventType
- payload
- schemaVersion
- status
- retryCount
- createdAt
- publishedAt

Implement:
- Retry
- Failed event tracking
- Idempotency
- Concurrent publisher safety
- Event ordering strategy
- Dead Letter Topic
- Replay mechanism
- Monitoring

Use PostgreSQL polling publisher initially. Document Debezium CDC as an alternative advanced implementation.

## 7. Apache Kafka and Avro

Use Apache Avro for all business events.

Use Schema Registry for schema management.

Create version-controlled Avro schema files.

Example events:
- ProductCreated
- ProductUpdated
- ProductDeleted
- ProductPublished
- ProductUnpublished
- InventoryUpdated
- StockReserved
- StockReleased
- OrderCreated
- OrderConfirmed
- OrderCancelled
- PaymentSucceeded
- PaymentFailed
- RefundCompleted

Create a common event envelope containing:
- eventId
- eventType
- eventVersion
- aggregateId
- aggregateVersion
- occurredAt
- correlationId
- trace context
- payload

Implement:
- Avro serializer and deserializer
- Schema Registry configuration
- Schema compatibility rules
- Kafka producer configuration
- Kafka consumer configuration
- Consumer groups
- Retry topics or retry handling
- Dead Letter Topics
- Idempotent consumers
- Duplicate event detection
- Event version checks
- Consumer recovery
- Kafka headers
- W3C trace context propagation

Do not use Java native serialization.

Do not assume Kafka exactly-once guarantees automatically provide end-to-end exactly-once business processing. Design idempotency explicitly.

## 8. Elasticsearch Indexing Service

This service consumes product and inventory events.

Implement:
- Product indexing
- Product updating
- Product deletion
- Publish/unpublish synchronization
- Bulk indexing
- Reindexing
- Version-aware updates
- Retry and DLQ
- Index aliases
- Zero-downtime reindex strategy

Index fields:
- productId
- name
- description
- category
- brand
- SKU
- price
- currency
- attributes
- availability
- quantity
- keywords
- productVersion
- updatedAt

Elasticsearch must be a search projection, not the transactional source of truth.

## 9. Redis Cache Synchronization Service

Implement a separate cache synchronization service.

Required flow:

CMS ProductUpdated
→ Kafka
→ Indexing Service
→ Elasticsearch updated
→ ProductIndexed event
→ Redis Cache Sync Service
→ Fetch latest indexed document
→ Update Redis

Redis key examples:
- product:{productId}
- product:search:{queryHash}
- product:category:{categoryId}
- product:brand:{brandId}
- inventory:{sku}

Implement:
- Cache aside
- TTL
- Cache invalidation
- Cache warming
- Version checks
- Cache stampede protection
- Locking where required
- Hit/miss metrics
- Redis failure fallback
- Search cache invalidation

Never let an older Kafka event overwrite a newer cached version.

## 10. Search Service

Search Service must expose all customer-facing product read/search APIs.

Workflow:

API Gateway
→ Search Service
→ Redis lookup
→ Cache miss
→ Elasticsearch
→ Redis update
→ API response

Features:
- Full-text search
- Fuzzy search
- Autocomplete
- Prefix search
- SKU search
- Category filter
- Brand filter
- Price range filter
- Attribute filters
- Sorting
- Pagination
- Facets
- Availability filter

Redis must be optional for correctness. If Redis is unavailable, Search Service should still search Elasticsearch.

## 11. Inventory Service

Inventory owns its independent PostgreSQL database.

Implement:
- Stock creation
- Stock update
- Stock reservation
- Stock release
- Stock adjustment
- Available quantity
- Reserved quantity
- Low-stock notification
- Stock movement history

Prevent overselling through atomic database operations, transactions, locking or optimistic concurrency.

Publish inventory events to Kafka through a reliable outbox mechanism.

Inventory events must update Elasticsearch availability and Redis cache.

## 12. Order, Payment and Saga Workflow

Order Service owns order data.

Payment Service owns payment transactions.

Use Saga orchestration as the primary order workflow.

Order workflow:

1. Customer creates order.
2. Validate product availability using appropriate service APIs.
3. Reserve inventory.
4. Initiate payment.
5. On success, confirm order.
6. Publish OrderConfirmed.
7. Notification Service sends confirmation.

Failure workflow:

1. Payment fails.
2. Saga triggers inventory release.
3. Order status becomes PAYMENT_FAILED or CANCELLED according to business rules.
4. Notification Service sends failure notification.

Implement:
- Saga state management
- Compensation actions
- Idempotency
- Payment retry rules
- Order cancellation
- Refund workflow
- Order history
- Order status tracking

Use a mock payment provider for local development.

Never store raw card data.

## 13. Auth and API Gateway

Implement:
- Spring Security
- JWT access token
- Refresh token
- Password hashing
- Role-based authorization
- Service-to-service authentication
- Gateway JWT validation
- Rate limiting
- Request ID
- Correlation ID
- Dynamic service discovery
- Load balancing
- Global exception handling

Roles:
- ADMIN
- CONTENT_MANAGER
- CUSTOMER
- SERVICE

CMS modifications must be restricted to ADMIN and CONTENT_MANAGER.

Customers must only access their own orders.

Do not trust a customer ID supplied by the request without checking authenticated identity.

## 14. Distributed Tracing and Logging

Use:
- OpenTelemetry
- Micrometer Tracing
- Zipkin
- W3C Trace Context

Trace synchronous HTTP calls across:
- API Gateway
- CMS
- Ingestion
- Search
- Inventory
- Order
- Payment
- Notification

Propagate context through:
- Feign or HTTP clients
- Kafka headers
- Kafka producers and consumers
- Redis operations
- Elasticsearch operations

Use framework-generated Trace ID, Span ID and Parent Span ID.

Never manually hardcode trace IDs or span IDs.

Maintain separate application identifiers:
- correlationId
- requestId
- eventId
- ingestionBatchId
- workflowId

Use structured JSON logging with:
- timestamp
- level
- serviceName
- traceId
- spanId
- correlationId
- requestId
- eventId
- batchId
- operation
- message

Use Zipkin for tracing and Grafana Loki for centralized log aggregation. Use Prometheus for metrics.

Do not confuse logs, metrics, distributed traces and Kafka event logs; they serve different purposes.

For long-running asynchronous workflows, create appropriate new traces or spans and use trace links where needed. Do not force unrelated long-running jobs into one artificial trace.

## 15. Resilience and Monitoring

Implement:
- Circuit breakers
- Retry
- Time limiter
- Bulkhead
- Rate limiter
- Health checks
- Readiness and liveness
- Prometheus metrics
- Grafana dashboards
- Kafka consumer lag monitoring
- Outbox backlog monitoring
- Redis hit/miss metrics
- Elasticsearch indexing metrics
- Ingestion throughput metrics
- Order and payment failure metrics

## 16. Docker and Infrastructure

Create docker-compose.yml for infrastructure:
- PostgreSQL
- Kafka in KRaft mode
- Schema Registry
- Kafka UI
- Elasticsearch
- Redis
- Zipkin
- Prometheus
- Grafana
- Loki
- Log collector

Use persistent volumes, health checks, environment variables and separate network configuration.

Provide Dockerfiles for every Java microservice.

Do not claim all Java services start with infrastructure Compose unless their build and startup configurations are actually included.

Provide separate commands for:
1. Starting infrastructure.
2. Building applications.
3. Starting each service.
4. Running all services.
5. Running tests.

## 17. Testing

Use:
- JUnit 5
- Mockito
- Spring Boot Test
- Testcontainers

Write tests for:
- CMS product validation
- Database ownership and permissions
- Ingestion validation
- Duplicate detection
- Outbox atomicity
- Avro serialization
- Kafka consumer idempotency
- Elasticsearch indexing
- Redis synchronization
- Search filters
- Inventory reservation
- Overselling prevention
- Order creation
- Payment success/failure
- Saga compensation
- JWT authorization
- Trace propagation

Do not create tests that only assert true or mock every dependency without testing actual behavior.

## 18. Project Folder Structure

Use this structure as a starting point:

ecommerce-platform/
│
├── pom.xml
├── README.md
├── docker-compose.yml
├── .env.example
├── .gitignore
│
├── infrastructure/
│   ├── postgres/
│   ├── kafka/
│   ├── schema-registry/
│   ├── elasticsearch/
│   ├── redis/
│   ├── zipkin/
│   ├── prometheus/
│   ├── grafana/
│   └── loki/
│
├── common-libraries/
│   ├── common-events/
│   ├── common-observability/
│   └── common-test-support/
│
├── discovery-server/
├── api-gateway/
├── auth-service/
├── cms-service/
├── ingestion-service/
├── search-service/
├── inventory-service/
├── order-service/
├── payment-service/
├── notification-service/
├── elasticsearch-indexing-service/
└── redis-cache-sync-service/
 
Each service must follow a clear package structure:

com.company.ecommerce.<service>
    ├── config
    ├── controller
    ├── service
    ├── repository
    ├── entity
    ├── dto
    │   ├── request
    │   └── response
    ├── mapper
    ├── exception
    ├── client
    ├── messaging
    ├── security
    └── util

Use domain-specific packages where a flat package structure becomes too large.

## 19. Code Quality and Hindi Comments

This is extremely important.

For every important Java class:
- Explain its responsibility.
- Explain why it exists.
- Explain which service uses it.
- Explain its dependencies.

For every important method:
- Explain what it does.
- Explain when it is called.
- Explain its parameters and return value.
- Explain important exceptions and failure cases.

Write meaningful Hindi/Hinglish comments for important business logic.

Explain annotations such as:
@Entity, @Id, @GeneratedValue, @Service, @Repository, @Transactional, @KafkaListener, @RestController, @Valid, @Configuration, @Bean, @FeignClient.

Explain WHY a particular implementation is chosen, not just WHAT the code does.

Avoid meaningless comments on every brace or obvious syntax.

Follow clean code, SOLID, separation of concerns and secure coding practices.

Do not put business logic inside controllers.

Do not put database access inside controllers.

Do not expose JPA entities directly in REST APIs.

Do not create a giant common module containing all service entities or repositories.

Keep common libraries limited to stable shared infrastructure contracts and event schemas.

## 20. Documentation

Generate:
- Architecture overview
- Mermaid architecture diagrams
- Service responsibility document
- Database ownership diagram
- Database ER diagrams
- Kafka topic and event documentation
- Avro schema documentation
- API documentation using OpenAPI
- Local setup guide
- Environment variable documentation
- Docker instructions
- Testing instructions
- Troubleshooting guide
- End-to-end execution guide

Provide Postman collection with sample requests and responses.

## 21. End-to-End Demo Scenarios

Demonstrate these complete flows:

A. Upload 100 products:
JSON → Ingestion → CMS → PostgreSQL → Outbox → Kafka Avro → Elasticsearch → Redis → Search API.

B. Search Samsung:
Search API → Redis hit/miss → Elasticsearch → Redis cache → response.

C. Update product price:
CMS update → Outbox → Kafka → Elasticsearch → Redis → verify updated price.

D. Unpublish product:
CMS → Kafka → Elasticsearch update → Redis invalidation → product no longer appears in customer search.

E. Place order:
Order → Inventory reservation → Payment → Order confirmation → Notification.

F. Payment failure:
Payment failure → Saga compensation → Inventory release → Order status update.

G. Redis failure:
Search must continue using Elasticsearch.

H. Kafka consumer outage:
Show recovery, retries and DLQ behavior.

I. Distributed tracing:
Show how to trace a request across HTTP and Kafka using Zipkin.

## 22. Development Execution Rules

DO NOT generate the whole project in one response.

Work phase by phase:

Phase 1: Architecture, service responsibilities, diagrams and folder structure.

Phase 2: Root Maven project, common libraries and Avro schema module.

Phase 3: Docker infrastructure and PostgreSQL database initialization.

Phase 4: Eureka Discovery Server and API Gateway.

Phase 5: CMS, JPA entities, Flyway migrations and PostgreSQL security.

Phase 6: Authentication and JWT security.

Phase 7: Ingestion Service and CMS integration.

Phase 8: Transactional Outbox, Kafka and Avro.

Phase 9: Elasticsearch Indexing Service.

Phase 10: Redis Cache Synchronization Service.

Phase 11: Search Service.

Phase 12: Inventory Service.

Phase 13: Order, Payment, Notification and Saga.

Phase 14: OpenTelemetry, Micrometer Tracing, Zipkin and centralized logging.

Phase 15: Monitoring, resilience, integration tests and complete deployment.

At the beginning of each phase:
1. Explain the architecture in Hindi/Hinglish.
2. Show the folder structure.
3. List required Maven dependencies.
4. Generate complete code for the current phase.
5. Generate configuration files.
6. Generate Flyway SQL migrations where needed.
7. Explain important code and annotations.
8. Give API examples and expected responses.
9. Write tests.
10. Give commands to run and verify the implementation.
11. Explain common errors and their solutions.

After completing a phase, STOP and wait for my confirmation before proceeding.

Do not silently skip a requirement.

Do not use pseudocode where working code is expected.

Do not create empty classes, fake implementations or hardcoded successful responses.

If something cannot be implemented completely in the current phase, clearly identify it as pending.

Do not claim that code has been executed or tested unless it actually has been executed.

Start with PHASE 1 ONLY. First provide the architecture, Mermaid diagrams, service responsibilities, database ownership, Kafka event flow and recommended folder structure. Do not generate Java implementation yet.