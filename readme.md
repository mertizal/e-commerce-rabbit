# Order Processing System (RabbitMQ)

## Project Overview

This project demonstrates an event-driven order processing system built with Spring Boot and RabbitMQ.

The main focus of the project is:

* Reliable message-driven communication
* Controlled concurrency and backpressure
* Request–reply pattern over RabbitMQ
* Observability via Spring Boot Actuator

The system processes orders asynchronously while still supporting synchronous-like stock validation when required.

---

## High-Level Architecture

```
+-------------------+
| 1. Order Created  |
|   (HTTP POST)     |
+-------------------+
          |
          v
+---------------------------+
| 2. OrderCreatedEvent      |
|   Queue (priority)        |
+---------------------------+
          |
          v
+---------------------------+
| 3. OrderCreatedConsumer   |
|                           |
| - Stock check (sync-like) |
| - Business validation     |
| - Publish PaymentRequested|
+---------------------------+
          |
          v
+---------------------------+
| 4. PaymentRequestedEvent  |
|   Queue (standard)        |
+---------------------------+
          |
          v
+---------------------------+
| 5. PaymentConsumer        |
|                           |
| - SUCCESS → PaymentOK     |
| - FAILED  → PaymentFail   |
|   (retry with delay)      |
+---------------------------+
          |
          v
+---------------------------+
| 6. Payment Result Events  |
| - Logging / Audit         |
| - Further processing     |
+---------------------------+
```

---

## Request Lifecycle

### 1. Order Creation (HTTP)

An order is created via a synchronous HTTP POST request.

After basic validation, the API publishes an `OrderCreatedEvent` to RabbitMQ and immediately returns a response to the client.

---

### 2. OrderCreatedEvent (Priority Queue)

Orders are published to a priority queue so that higher-priority orders can be processed first under load.

This allows:

* Fair processing under heavy traffic
* Queue-level prioritization without blocking producers

---

### 3. Stock Check (Request–Reply Pattern)

Although the system is event-driven, stock validation is implemented as a synchronous-like operation using RabbitMQ.

Implementation details:

* Order consumer publishes a `StockCheckRequest` message
* A unique `correlationId` is generated
* The request is sent to a dedicated stock request queue
* Stock service processes the request and sends a response
* The response is routed back via a reply queue using the same `correlationId`

This pattern provides:

* Asynchronous messaging
* Synchronous business decision
* Loose coupling between services

---

### 4. PaymentRequestedEvent

If the stock check succeeds, a `PaymentRequestedEvent` is published.

This event represents a new stage in the order lifecycle and is processed asynchronously.

---

### 5. Payment Processing

The payment consumer handles payment logic.

Possible outcomes:

* `PaymentSucceededEvent`
* `PaymentFailedEvent`

Failed payments are retried using a delayed retry mechanism to avoid overwhelming the system.

---

### 6. Payment Result Handling

Payment result events are consumed for:

* Logging
* Audit purposes
* Further downstream processing

No external notification or messaging system is involved.

---

## Reliability and Delivery Guarantees

The system is designed with the following guarantees:

* Durable queues
* Persistent messages
* Manual acknowledgements
* At-least-once delivery semantics
* Idempotent consumer logic
* Dead Letter Queues for failed messages

---

## Observability and Metrics

Application metrics are exposed via Spring Boot Actuator.

Observed metrics include:

* HTTP request latency
* JVM memory and garbage collection
* Thread pool utilization
* Message consumption behavior

Metrics are currently observed directly through Actuator endpoints.

---

## Load Testing

Load testing is performed using Apache JMeter running in Docker.

Test scenarios simulate:

* Concurrent order creation
* Increased message throughput
* Consumer backpressure and retry behavior

Results are analyzed using JMeter summary reports and application logs.

---

## CI/CD and Cloud Deployment

The system uses a fully automated CI/CD pipeline built with GitHub Actions to ensure that every change is tested, containerized, and deployed without manual intervention.

The pipeline consists of the following stages:

### Build & Test

* Compiles the application using JDK 21 and Maven
* Executes unit tests
* Packages the application as a JAR artifact

### Dockerization

* Builds a multi-architecture Docker image
* Pushes the image to Docker Hub using latest and commit-based SHA tags
* Leverages Docker layer caching for faster builds

### Cloud Deployment

* Automatically deploys the application to Fly.io using flyctl
* Pulls the container image directly from Docker Hub
* Ensures zero-downtime releases via health checks

## Infrastructure as Code

All infrastructure components are managed declaratively using Fly.io configuration files.
Application Configuration
Managed via fly.toml
Defines environment variables, secrets, and scaling parameters
RabbitMQ Configuration
Managed via fly.rabbitmq.toml
Uses Fly Volumes for persistent message storage
Configured with explicit resource limits and health checks

## Resilience and Persistence

The system is designed for reliability and data durability in a cloud environment.
Database Persistence
Uses a managed PostgreSQL instance on Fly.io
Ensures durable storage for transactional data
Message Broker Reliability
RabbitMQ runs with a dedicated persistent volume for the Mnesia database
Prevents message loss during restarts and redeployments
Publisher Confirms
Enabled using correlated confirms
Reply timeout tuned to handle cloud network latency

## Technology Stack

* Java
* Spring Boot
* RabbitMQ
* Docker & Docker Compose
* Apache JMeter
* Spring Boot Actuator
* Postgresql
