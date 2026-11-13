# Service Responsibilities

| Service | Primary Role | Data Ownership | External Dependency | Notes |
| --- | --- | --- | --- | --- |
| discovery-server | Service discovery | N/A | Eureka Registry | Must register all app instances |
| api-gateway | Edge API layer | N/A | Auth, CMS, Search, Order | Router + security + resilience |
| auth-service | Authentication and authorization | `ecommerce_auth_db` | JWT, user profiles | Issues/validates tokens |
| cms-service | Product catalog authority | `ecommerce_cms_db` | Product CRUD, audit, versioning | Only service allowed to write catalog |
| ingestion-service | Data intake and batch orchestration | `ecommerce_ingestion_db` | CMS REST APIs | Validates + normalizes incoming data |
| elasticsearch-indexing-service | Search document synchronization | N/A | Kafka, Elasticsearch | Consumes catalog events |
| redis-cache-sync-service | Cache synchronization | N/A | Kafka, Redis | Invalidates or updates cached product data |
| search-service | Customer-facing product search | N/A | Elasticsearch, Redis | Read-only search APIs |
| inventory-service | Stock level tracking | `ecommerce_inventory_db` | Order service | Reservation/release workflow |
| order-service | Order lifecycle | `ecommerce_order_db` | Inventory, payment, notification | Saga orchestration and status updates |
| payment-service | Payment processing | `ecommerce_payment_db` | Order service | Stores payment metadata only |
| notification-service | Event notifications | `ecommerce_notification_db` | Kafka / event triggers | Sends email, push, or notification events |

## Ownership constraints

- CMS owns product creation, update, deletion, publishing, category and brand management.
- Search Service owns customer-facing product read/search experience.
- Ingestion Service must never write catalog tables directly.
- No service should assume shared database tables or cross-service foreign keys.
- Inventory and order systems must remain logically separated even if they share the same deployment environment.

## Integration responsibilities

- CMS writes catalog state to its primary database.
- Transactional outbox emits Kafka events on catalog mutations.
- Elasticsearch Indexing Service updates the search index.
- Redis Cache Sync Service invalidates or refreshes product keys in Redis.
- Search Service reads from Elasticsearch and Redis for customer-facing queries.
- Payment and order flows remain decoupled from catalog updates to keep the catalog write path simpler.
