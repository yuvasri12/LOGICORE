# LogiCore OMS

LogiCore OMS is the internal core order management and logistics service for the DevBridge OMS pilot.

It owns shipment/order lifecycle data, warehouse receive actions, storage assignment, dispatch, status history, and a public-safe tracking API for TenantTrack Portal.

## Tech Stack

- Java 8
- Spring Boot 2.7.18
- Spring Web
- Spring Data JPA
- PostgreSQL for project database
- H2 for quick local demo
- REST APIs

## Run Locally

Install Maven, then run:

```powershell
cd logicore-oms
mvn spring-boot:run
```

The default profile uses H2 in-memory database. To use PostgreSQL, set these environment variables:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/logicore_oms"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
mvn spring-boot:run
```

## Main APIs

- `POST /api/shipments`
- `GET /api/shipments`
- `GET /api/shipments/{id}`
- `PUT /api/shipments/{id}`
- `DELETE /api/shipments/{id}`
- `PATCH /api/shipments/{id}/status`
- `POST /api/warehouse/receive`
- `POST /api/warehouse/assign-location`
- `POST /api/dispatch`
- `GET /api/tracking/{trackingNumber}`

## Demo Flow

1. Create shipment.
2. Receive freight at warehouse.
3. Assign storage location.
4. Dispatch shipment.
5. Query tracking API using tracking number.
