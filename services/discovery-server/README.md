# Discovery Server

Eureka registry for service registration and discovery. This server does not register with or fetch from itself.

## Run

From the repository root:

```bash
mvn -pl services/discovery-server spring-boot:run
```

The Eureka dashboard is available at `http://localhost:8761`. Health is exposed at `/actuator/health`.
