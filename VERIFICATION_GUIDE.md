# Turaco QA Project - Verification Guide

## Overview

Complete vertical slice of a production-ready fintech claims processing application with database, REST API, Selenium automation, and CI/CD pipeline.

---

## Project Structure

```
turaco-qa-project/
├── backend/                          # Spring Boot application
│   ├── src/main/java/com/example/demo/
│   │   ├── Application.java         # Spring Boot entry point
│   │   ├── Claim.java              # JPA Entity
│   │   ├── ClaimRepository.java    # Data access layer
│   │   └── ClaimController.java    # REST API endpoints
│   ├── src/main/resources/
│   │   └── application.properties   # Database configuration
│   ├── Dockerfile                   # Multi-stage Docker build
│   └── pom.xml                      # Maven dependencies (PostgreSQL, Spring Boot)
│
├── automation/                       # Selenium Test Framework
│   ├── src/test/java/
│   │   ├── pages/ClaimsPage.java   # Page Object Model
│   │   └── tests/ClaimsTest.java   # Test cases
│   ├── pom.xml                      # Maven + Selenium + TestNG
│   └── testng.xml                   # TestNG configuration
│
├── k8s/                             # Kubernetes manifests
│   └── deployment.yaml              # K8s deployment (2 replicas)
│
├── docker-compose.yml               # PostgreSQL + Backend services
├── .github/workflows/main.yml       # CI/CD pipeline
├── .gitignore                       # Repository exclusions
└── README.md                         # This file
```

---

## Step 1: Start PostgreSQL + Backend (Docker Compose)

```bash
cd ~/turaco-qa-project~
docker-compose up --build -d
```

**Expected Output:**

```
Creating network "turaco-qa-project_default" with the default driver
Creating turaco-qa-project_db_1 ... done
Creating turaco-qa-project_backend_1 ... done
```

**Verify services are running:**

```bash
docker ps
```

You should see:

- `postgres:15` container (port 5432)
- `claims-api` container (port 8080)

---

## Step 2: Verify Database Connection

```bash
docker-compose exec db psql -U turaco_admin -d claims_db -c "SELECT version();"
```

**Expected Output:**

```
PostgreSQL 15.x on x86_64-...
```

---

## Step 3: Verify API is Live

**Option A: Using curl**

```bash
curl http://localhost:8080/api/v1/claims
```

**Expected Response:**

```json
[]
```

**Option B: Using Postman**

1. Create GET request to: `http://localhost:8080/api/v1/claims`
2. You should get a `200 OK` response with empty array `[]`

---

## Step 4: Test API Endpoints

### Create a Claim (POST)

```bash
curl -X POST http://localhost:8080/api/v1/claims \
  -H "Content-Type: application/json" \
  -d '{
    "policyId": "POL-2026-001",
    "claimAmount": 5000.00
  }'
```

**Expected Response:**

```json
{
  "id": 1,
  "policyId": "POL-2026-001",
  "claimAmount": 5000.0,
  "status": "SUBMITTED"
}
```

### Get All Claims (GET)

```bash
curl http://localhost:8080/api/v1/claims
```

**Expected Response:**

```json
[
  {
    "id": 1,
    "policyId": "POL-2026-001",
    "claimAmount": 5000.0,
    "status": "SUBMITTED"
  }
]
```

### Update Claim Status (PUT)

```bash
curl -X PUT http://localhost:8080/api/v1/claims/1 \
  -H "Content-Type: application/json" \
  -d '{"status": "APPROVED"}'
```

---

## Step 5: Run Selenium Tests

```bash
cd automation
mvn test
```

**Expected Output:**

```
[INFO] Running tests.ClaimsTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## Step 6: Run Backend Unit Tests

```bash
cd backend
mvn test
```

---

## Step 7: Build Backend Docker Image

```bash
cd backend
mvn clean package
docker build -t claims-api:1.0 .
```

**Verify Image:**

```bash
docker images | grep claims-api
```

---

## Step 8: Deploy to Kubernetes

```bash
# Create secret
kubectl create secret generic db-password --from-literal=password=TuracoSecure2026

# Deploy application
kubectl apply -f k8s/deployment.yaml

# Verify deployment
kubectl get pods
kubectl get svc
```

---

## Step 9: CI/CD Pipeline Verification

The GitHub Actions pipeline runs automatically on every push and includes:

1. **Security Scan** - Snyk vulnerability scanning (optional)
2. **Backend Build** - Maven compilation
3. **Backend Tests** - Unit test execution
4. **Automation Tests** - Selenium test execution
5. **Docker Build** - Container image creation
6. **Test Reports** - Published results

---

## "Friday Interview" Expert Talking Points

### 1. **Idempotency**

> "In a production system, the `/api/v1/claims` endpoint would check for duplicate `policyId` values before saving. This prevents customers from being double-charged if they accidentally submit the same claim twice."

**Implementation Example:**

```java
@PostMapping
public Claim submitClaim(@RequestBody Claim claim) {
    // Check if claim already exists
    Optional<Claim> existing = repository.findByPolicyId(claim.getPolicyId());
    if (existing.isPresent()) {
        return existing.get(); // Return existing claim (idempotent)
    }
    return repository.save(claim);
}
```

### 2. **Observability**

> "Since the application runs in Kubernetes, I've configured liveness and readiness probes in the `deployment.yaml`. This ensures the Selenium tests don't start until the Spring Boot application is fully healthy and ready to accept requests."

**Probes in deployment.yaml:**

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5
```

### 3. **Security Scanning**

> "Our GitHub Actions pipeline doesn't just test the code—it _vets_ it. We use Snyk to scan all Java dependencies for known vulnerabilities. Every library (Selenium, Spring Boot, TestNG) is checked against the Snyk vulnerability database. This prevents supply-chain attacks."

**GitHub Actions Step:**

```yaml
- name: Snyk Security Scan
  uses: snyk/actions/maven@master
  env:
    SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
```

### 4. **Multi-Stage Docker Build**

> "Our Dockerfile uses a two-stage build process: the build stage compiles the JAR with Maven (heavy image), and the runtime stage uses a slim OpenJDK image. This reduces the production image size from 700MB to ~200MB. Additionally, we run the container as a non-root user (`turaco`) for security hardening."

### 5. **Page Object Model**

> "The `ClaimsPage` class abstracts all UI interactions. This makes tests maintainable—if the UI changes, I only update the locators in one place instead of every test."

### 6. **DevOps Workflow**

> "Everything is containerized and orchestrated. The developer runs `docker-compose up`, the CI/CD runs tests on every commit, and production deploys to Kubernetes. This 'shift-left' approach catches bugs early."

---

## Cleanup

**Stop Services:**

```bash
docker-compose down
```

**Remove Containers & Volumes:**

```bash
docker-compose down -v
```

---

## Troubleshooting

| Issue                         | Solution                                             |
| ----------------------------- | ---------------------------------------------------- |
| `Port 5432 already in use`    | `docker-compose down` then try again                 |
| `Database connection refused` | Wait 30 seconds for PostgreSQL to fully start        |
| `Selenium tests fail`         | Ensure backend is running on `http://localhost:8080` |
| `Maven build fails`           | Run `mvn clean` and try again                        |
| `Docker image build fails`    | Ensure Java 17 is installed                          |

---

## Key Technologies

- **Backend**: Spring Boot 3.0 + Spring Data JPA
- **Database**: PostgreSQL 15
- **Testing**: Selenium 4 + TestNG
- **CI/CD**: GitHub Actions
- **Container**: Docker + Kubernetes
- **Build**: Maven 3.9

---

## Next Steps (Beyond MVP)

- [ ] Add Spring Security for OAuth2
- [ ] Implement GraphQL API
- [ ] Add distributed tracing (Jaeger/OpenTelemetry)
- [ ] Set up ELK stack for logs
- [ ] Configure auto-scaling policies
- [ ] Add performance testing (JMeter)
- [ ] Implement feature flags (LaunchDarkly)

---

**Built with DevOps excellence! 🚀**
