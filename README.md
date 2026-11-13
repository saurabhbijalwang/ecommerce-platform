# E-Commerce Microservices Platform

This repository is being built in phases as described in the project prompt.

## Current phase

Phases 1 and 2 establish the architecture and Maven/shared-module foundation. Phase 3 adds local Docker/PostgreSQL infrastructure.

Service implementation and the remaining infrastructure will be added in later phases.

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

The repository contains skeleton service directories under [services/](services/), each planned for separate Maven modules in later phases.

For local PostgreSQL setup, follow [Phase 3 local setup](docs/phase-3-local-setup.md).
