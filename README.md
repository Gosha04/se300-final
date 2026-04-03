# Smart Store Testing Framework

This project is a Java-based smart store framework created for modeling, structuring, and testing the major layers of a smart retail system. Its primary goal is to demonstrate software design, layered architecture, and strong automated test coverage rather than to serve as a production-ready smart store platform with every feature fully implemented.

The codebase focuses on representing how a smart store could be organized through domain models, services, repositories, controllers, and REST endpoints. Just as importantly, it shows how those layers can be verified through unit, integration, mock-server, and end-to-end testing.

## Project Purpose

The main purpose of this project is to:

- Model the core concepts of a smart store system
- Organize those concepts using a layered Java application architecture
- Provide a framework for validating behavior through automated tests
- Demonstrate code coverage and quality reporting using JaCoCo and Sonar

This means the repository should be read as a testing-oriented academic/software engineering project, not as a finished commercial smart store implementation.

## What The Framework Models

The project includes models and workflows for:

- Stores
- Aisles and shelves
- Products and inventory
- Customers and baskets
- Users and authentication
- Devices, sensors, and appliances
- Basic REST API interactions for store and user management

The architecture is split across:

- `model` for domain objects and store concepts
- `service` for business logic and orchestration
- `repository` for persistence-facing logic
- `controller` for REST-facing behavior
- `data` for application data management
- `servlet` for servlet utilities and JSON handling

## Testing Focus

Testing coverage is the centerpiece of this project.

The repository includes:

- Unit tests for models, services, repositories, and controllers
- Integration tests for repositories, services, and REST controllers
- Internal mock server tests
- External mock server tests
- End-to-end "big bang" system tests

Based on the generated reports currently in the repository:

- `110` tests passed
- `0` failures
- `0` errors
- JaCoCo line coverage is `98%`
- JaCoCo branch coverage is `91%`

Generated test artifacts can be found under:

- `target/surefire-reports/`
- `target/site/jacoco/`

## Tech Stack

- Java 21
- Maven
- JUnit 5
- Mockito
- Rest Assured
- MockServer
- Embedded Tomcat
- JaCoCo
- SonarCloud/SonarQube configuration

## Running The Project

### Prerequisites

- Java 21
- Maven 3+

### Run tests

```bash
mvn test
```

### Generate coverage report

```bash
mvn test
```

After running tests, open the JaCoCo report from:

```text
target/site/jacoco/index.html
```

### Application entry point

The main application class is:

```text
com.se300.store.SmartStoreApplication
```

When the embedded server is started, the main API endpoints are exposed at:

- `http://localhost:8080/api/v1/stores`
- `http://localhost:8080/api/v1/users`

## API Notes

An OpenAPI description is included here:

- `src/main/resources/api/openapi.yaml`

The API layer exists to support testing and architectural demonstration. Some endpoints and workflows are intentionally lightweight because the project emphasis is on verification coverage and system structure.

## Scope And Limitations

This project does not aim to fully implement every real-world smart store behavior. Instead, it provides a strong structural and testable foundation for:

- store layout modeling
- inventory-related workflows
- customer and basket interactions
- device and sensor concepts
- REST and service-layer validation

In other words, the framework shows how such a system can be designed and tested, even where production-grade persistence, security, hardware integration, and full business functionality are outside the project scope.

## Summary

This repository is best understood as a smart store modeling and testing framework. Its strongest achievement is not feature completeness, but the breadth of automated verification across the system and the high level of coverage used to validate the design.
