# Recommended Folder Structure

```text
EcommercePlatform/
├── README.md
├── prompt.md
├── .env.example
├── docker-compose.yml
├── docs/
│   ├── architecture/
│   │   ├── phase-1-architecture.md
│   │   ├── mermaid-diagrams.md
│   │   ├── service-responsibilities.md
│   │   └── database-ownership.md
│   ├── environment-variables.md
│   ├── folder-structure.md
│   └── phase-3-local-setup.md
├── infrastructure/
│   └── postgres/
│       ├── README.md
│       └── init/
│           └── 00-init-databases.sh
├── services/
│   ├── discovery-server/
│   │   └── README.md
│   ├── api-gateway/
│   │   └── README.md
│   ├── auth-service/
│   │   └── README.md
│   ├── cms-service/
│   │   └── README.md
│   ├── ingestion-service/
│   │   └── README.md
│   ├── search-service/
│   │   └── README.md
│   ├── inventory-service/
│   │   └── README.md
│   ├── order-service/
│   │   └── README.md
│   ├── payment-service/
│   │   └── README.md
│   ├── notification-service/
│   │   └── README.md
│   ├── elasticsearch-indexing-service/
│   │   └── README.md
│   └── redis-cache-sync-service/
│       └── README.md
├── docker/
│   └── README.md
├── infra/
│   └── README.md
├── scripts/
│   └── README.md
├── .gitignore
└── .mvn/
    └── README.md
```

## Notes

- Each service directory is reserved for a separate Maven module in Phase 2.
- Infrastructure and Docker configuration will be added in Phase 3.
- Shared libraries, Avro schema module, and root pom will be created in Phase 2.
- Java code should not be added in Phase 1; this folder structure is only the architectural base.
