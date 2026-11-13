# E-Commerce Microservices Platform

This repository is being built in phases as described in the project prompt.

## Current phase

Phase 4 implements the Eureka Discovery Server and reactive API Gateway. PostgreSQL initialization is present, while the rest of the Phase 3 Docker infrastructure remains in progress.

## Phase 1 deliverables

- [docs/architecture/phase-1-architecture.md](docs/architecture/phase-1-architecture.md)
- [docs/architecture/mermaid-diagrams.md](docs/architecture/mermaid-diagrams.md)
- [docs/architecture/service-responsibilities.md](docs/architecture/service-responsibilities.md)
- [docs/architecture/database-ownership.md](docs/architecture/database-ownership.md)
- [docs/folder-structure.md](docs/folder-structure.md)
- [docs/phase-3-local-setup.md](docs/phase-3-local-setup.md)
- [docs/environment-variables.md](docs/environment-variables.md)
- [infrastructure/postgres/README.md](infrastructure/postgres/README.md)

## High-level architecture summary

The platform follows a domain-driven microservices model with independent service ownership, clear database boundaries, event-driven catalog updates, and search cache synchronization.

- CMS owns the Product Catalog write path.
- Ingestion Service never writes to CMS tables directly; it calls CMS APIs.
- Search Service reads from Elasticsearch and Redis, not directly from CMS database.
- Kafka carries domain events and outbox events for synchronization.
- Spring Cloud Gateway exposes APIs while Discovery Server manages service registration.

## Service layout

Implemented Phase 4 services live under [services/](services/); remaining services are still planned for later phases.

For local PostgreSQL setup, follow [Phase 3 local setup](docs/phase-3-local-setup.md).
