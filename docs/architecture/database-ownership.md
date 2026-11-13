# Database Ownership and Data Boundaries

## Required databases

- `ecommerce_cms_db` — CMS service ownership
- `ecommerce_ingestion_db` — Ingestion service ownership
- `ecommerce_auth_db` — Auth service ownership
- `ecommerce_inventory_db` — Inventory service ownership
- `ecommerce_order_db` — Order service ownership
- `ecommerce_payment_db` — Payment service ownership
- `ecommerce_notification_db` — Notification service ownership

## Ownership principles

1. Database-per-service model is required.
2. No cross-service foreign keys.
3. Only CMS may write catalog tables.
4. Search Service does not query CMS database.
5. Ingestion Service does not directly access product catalog tables.
6. Service-owned DBs host only domain-relevant data.
7. Elasticsearch and Redis are not transactional source of truth.

## Database user and access model

| Database Role | Access | Purpose |
| --- | --- | --- |
| `cms_writer` | Write access to CMS catalog tables | CMS writes product catalog data |
| `cms_reader` | Read-only access for CMS internal needs | Content operations |
| `ingestion_user` | No direct catalog DB access | Calls CMS APIs only |
| `search_user` | No CMS catalog DB access | Reads from Elasticsearch/Redis only |
| `other_service_users` | No unauthorized access | Enforced by DB permissions |

## Catalog ownership map

```text
CMS Service
  ├─ products
  ├─ categories
  ├─ brands
  ├─ product_attributes
  ├─ product_variants
  ├─ product_media
  ├─ prices
  ├─ audit_history
  ├─ product_versions
  └─ status lifecycle tables

Ingestion Service
  ├─ batch_metadata
  ├─ file_uploads
  ├─ validation_errors
  ├─ ingestion_jobs
  ├─ duplicate_records
  └─ retry_logs
```

## Why this ownership model matters

This model prevents the data consistency issue where multiple services accidentally write to the same product catalog records. It creates a single source of truth for catalog data, while still allowing eventual read-side synchronization to Elasticsearch and Redis. The system remains scalable and easier to reason about during outages, retries, and batch failures.
