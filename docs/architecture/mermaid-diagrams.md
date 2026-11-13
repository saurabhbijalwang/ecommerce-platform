# Mermaid Architecture Diagrams

## 1. Platform architecture overview

```mermaid
flowchart LR
    Client[Web / Mobile / Admin Client] --> Gateway[API Gateway]
    Gateway --> Auth[Auth Service]
    Gateway --> CMS[CMS Service]
    Gateway --> Search[Search Service]
    Gateway --> Order[Order Service]
    Gateway --> Inventory[Inventory Service]

    Ingestion[Ingestion Service] -->|REST API| CMS
    CMS -->|Write product data| CMSDB[(CMS PostgreSQL)]
    CMS -->|Transactional Outbox| Kafka[(Kafka)]
    Kafka --> ESIndex[Elasticsearch Indexing Service]
    Kafka --> RedisSync[Redis Cache Sync Service]
    ESIndex --> ES[(Elasticsearch)]
    RedisSync --> Redis[(Redis)]

    Search --> ES
    Search --> Redis
    Order --> Inventory
    Order --> Payment[Payment Service]
    Order --> Notify[Notification Service]
    Payment --> DBOrder[(Order DB / Payment DB)]
    Inventory --> InvDB[(Inventory DB)]

    Auth --> AuthDB[(Auth DB)]
```

## 2. Catalog ownership and event propagation diagram

```mermaid
sequenceDiagram
    participant User as Admin / Ingestion Job
    participant Ingestion as Ingestion Service
    participant CMS as CMS Service
    participant DB as CMS PostgreSQL
    participant Outbox as Transactional Outbox
    participant Kafka as Kafka
    participant ES as Elasticsearch Indexing Service
    participant Redis as Redis Cache Sync Service
    participant Search as Search Service

    User->>Ingestion: Upload product JSON / trigger batch
    Ingestion->>CMS: POST /products or update API
    CMS->>DB: Save product + audit + version
    CMS->>Outbox: Record event
    Outbox->>Kafka: Catalog created/updated/deleted event
    Kafka->>ES: Consume product index event
    ES->>Search: Update search index
    Kafka->>Redis: Consume product cache invalidation/update
    Redis->>Search: Refresh cache / invalidate stale key
    Search-->>User: Search API response
```

## 3. Database ownership map

```mermaid
flowchart TB
    CMS[CMS Service] --> CMSDB[(ecommerce_cms_db)]
    Ingestion[Ingestion Service] --> IngestionDB[(ecommerce_ingestion_db)]
    Auth[Auth Service] --> AuthDB[(ecommerce_auth_db)]
    Inventory[Inventory Service] --> InvDB[(ecommerce_inventory_db)]
    Order[Order Service] --> OrderDB[(ecommerce_order_db)]
    Payment[Payment Service] --> PaymentDB[(ecommerce_payment_db)]
    Notify[Notification Service] --> NotifyDB[(ecommerce_notification_db)]

    CMSDB -. has catalog write authority .-> Ingestion
    Search[Search Service] -. read only from Elasticsearch & Redis .-> ES[(Elasticsearch)]
    Search -. read only from Redis .-> Redis[(Redis)]
```

## 4. Kafka event flow for product catalog synchronization

```mermaid
flowchart LR
    CMS[CMS Service] -->|ProductCreated| Topic1[product.catalog.created.v1]
    CMS -->|ProductUpdated| Topic2[product.catalog.updated.v1]
    CMS -->|ProductDeleted| Topic3[product.catalog.deleted.v1]
    CMS -->|ProductUnpublished| Topic4[product.catalog.unpublished.v1]

    Topic1 --> ESIndex[Elasticsearch Indexing Service]
    Topic2 --> ESIndex
    Topic3 --> ESIndex
    Topic4 --> ESIndex

    Topic1 --> RedisSync[Redis Cache Sync Service]
    Topic2 --> RedisSync
    Topic3 --> RedisSync
    Topic4 --> RedisSync

    ESIndex --> ES[(Elasticsearch)]
    RedisSync --> Redis[(Redis)]
    ES --> Search[Search Service]
    Redis --> Search
```
