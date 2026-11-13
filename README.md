# E-Commerce Microservices Platform

This repository is being built in phases as described in the project prompt.

## Current phase

Phase 1 is complete and focuses only on architecture, service responsibilities, database ownership, Kafka flow, and the recommended folder structure.

Java implementation, Maven modules, Docker configuration, and service code will start in Phase 2 onward.

## Phase 1 deliverables

- [docs/architecture/phase-1-architecture.md](docs/architecture/phase-1-architecture.md)
- [docs/architecture/mermaid-diagrams.md](docs/architecture/mermaid-diagrams.md)
- [docs/architecture/service-responsibilities.md](docs/architecture/service-responsibilities.md)
- [docs/architecture/database-ownership.md](docs/architecture/database-ownership.md)
- [docs/folder-structure.md](docs/folder-structure.md)

## High-level architecture summary

The platform follows a domain-driven microservices model with independent service ownership, clear database boundaries, event-driven catalog updates, and search cache synchronization.

- CMS owns the Product Catalog write path.
- Ingestion Service never writes to CMS tables directly; it calls CMS APIs.
- Search Service reads from Elasticsearch and Redis, not directly from CMS database.
- Kafka carries domain events and outbox events for synchronization.
- Spring Cloud Gateway exposes APIs while Discovery Server manages service registration.

## Service layout

The repository contains skeleton service directories under [services/](services/), each planned for separate Maven modules in later phases.
