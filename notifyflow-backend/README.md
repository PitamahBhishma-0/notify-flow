# NotifyFlow — Multi-Channel Notification Engine

A production-grade, microservices-based notification delivery platform built with Java 21, Spring Boot 3, ActiveMQ, Redis, and Docker.

## Architecture

```
Client → API Gateway (JWT auth)
              ↓
    ┌─────────┼──────────┐
    ↓         ↓          ↓
Notification  User    Delivery
 Service    Service   Service
    ↓                    ↑
 ActiveMQ ───────────────┘
 (HIGH / MEDIUM / LOW queues)
    ↓
  Redis (dedup + rate limiting)
```

## Services

| Service | Port | Responsibility |
|---|---|---|
| `api-gateway` | 8080 | JWT auth, routing |
| `notification-service` | 8081 | Accept, validate, queue |
| `user-service` | 8082 | Auth, user management |
| `delivery-service` | 8083 | Consume, route, retry |

## Tech Stack

- **Backend**: Java 21, Spring Boot 3.2, Spring Cloud Gateway
- **Messaging**: ActiveMQ (priority queues: HIGH / MEDIUM / LOW)
- **Cache**: Redis (deduplication, rate limiting)
- **Database**: MySQL 8 (separate DB per service)
- **Auth**: JWT (RS256)
- **Containerization**: Docker, Docker Compose, Kubernetes (Minikube)
- **CI/CD**: GitHub Actions → Docker Hub → Render
- **Monitoring**: Prometheus + Grafana Cloud
- **API Docs**: Swagger UI (springdoc-openapi)

## Quick Start (local)

```bash
# Start all infra + services
docker-compose up -d

# API Gateway:        http://localhost:8080
# Swagger UI:         http://localhost:8081/swagger-ui.html
# ActiveMQ Console:   http://localhost:8161  (admin/admin)
```

## API Usage

### Register & login
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"secret123","name":"Gaurav"}'

# Returns: { "token": "eyJ..." }
```

### Dispatch a notification
```bash
curl -X POST http://localhost:8080/api/notifications \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "recipientUserId": "usr_abc123",
    "channel": "EMAIL",
    "priority": "HIGH",
    "subject": "Payment confirmed",
    "body": "Your payment of $240 has been received."
  }'
```

### Check delivery status
```bash
curl http://localhost:8080/api/notifications/{id} \
  -H "Authorization: Bearer <token>"
```

## Key Design Decisions

- **Priority queues**: HIGH messages are consumed with 5–10 concurrent workers, LOW with 1–3 — ensuring critical notifications are never starved.
- **Redis deduplication**: Each notification ID is stored with a 24h TTL — duplicate deliveries are impossible even under retry storms.
- **Rate limiting**: Max 10 notifications per user per channel per minute, enforced at the delivery layer.
- **Retry with backoff**: Failed deliveries go to a retry queue, then DLQ after 3 attempts.
- **Zero shared DB**: Each service owns its schema — true microservice independence.


## Kubernetes (local demo)

```bash
minikube start
kubectl apply -f k8s/
kubectl get pods -n notifyflow
```
