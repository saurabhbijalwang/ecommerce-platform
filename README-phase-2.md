# Phase 2: Root Maven Project and Shared Modules

This phase establishes the shared build foundation for the entire platform.

## Included modules

- `common-lib`: reusable API responses, constants, error types, and utilities
- `avro-schema`: Kafka Avro definitions used by producer and consumer services

## Build command

```bash
mvn clean install
```

## Notes

The root project is intentionally minimal at this stage. It provides the initial dependency alignment and common contracts that later service modules will reuse.
