# 🏫 Smart Campus Issue Reporter

> **Portfolio Project** — A full-stack campus issue management platform built to demonstrate professional Spring Boot architecture, real business logic, and a modern frontend.
>

---

## 📋 Project Overview

**Smart Campus Issue Reporter** solves a real-world problem: campus students have no structured way to report infrastructure issues (broken equipment, electrical faults, plumbing failures, etc.), and administrators have no visibility into issue severity or workload distribution.

This platform provides:
- A **student portal** for reporting issues with automatic priority calculation
- An **admin dashboard** with real-time charts and issue management tools
- A **transparent priority engine** that explains *why* an issue is critical

---

## ✨ Key Features

| Feature | Description |
|---|---|
| 🧠 **Priority Engine** | Weighted scoring algorithm that auto-assigns priority based on safety, affected users, category, and age |
| ⏱️ **SLA Tracking** | Tracks and highlights resolution time and highlights long-unresolved issues |
| 📊 **Live Dashboard** | Real-time stats and Chart.js charts backed by live database queries |
| 🔄 **Lifecycle Workflow** | Enforced status transitions (OPEN → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED) |
| 🔍 **Smart Search** | Multi-criteria search by category, status, priority, location, reporter, keyword, and date range |
| 🏷️ **Issue Codes** | Human-readable tracking codes in `SCI-YYYYMMDD-XXXX` format |
| 📱 **Responsive UI** | Works on desktop and mobile |
| 🛡️ **Validation** | Bean Validation on API + client-side form validation |
| 🚨 **Error Handling** | Centralized structured JSON error responses |

---

## 🏗️ Architecture

```
smart-campus-issue-reporter/
├── src/main/java/com/srikarthik/smartcampus/
│   ├── SmartCampusApplication.java
│   ├── controller/         # REST API controllers
│   ├── service/            # Business logic (IssueService, DashboardService)
│   ├── repository/         # Spring Data JPA repositories
│   ├── model/              # Issue entity + enums
│   ├── dto/                # Request/Response DTOs
│   ├── exception/          # Custom exceptions + global handler
│   ├── config/             # CORS configuration
│   └── util/               # PriorityEngine, WorkflowValidator, IssueCodeGenerator
├── src/main/resources/
│   ├── application.properties.example
│   └── static/             # Frontend (HTML/CSS/JS)
├── src/test/               # 46 unit + integration tests
├── database/schema.sql     # MySQL schema + sample data
├── pom.xml
└── README.md
```

---

## 🧠 Priority Engine

The `PriorityEngine` computes a **score out of 100** using four factors:

| Factor | Max Points | Logic |
|---|---|---|
| **Safety Impact** | 40 | Safety risk → 40 pts, else 0 |
| **Affected Users** | 25 | 50+ → 25 pts, 20–49 → 18, 5–19 → 10, <5 → 5 |
| **Category Risk** | 20 | SAFETY/ELECTRICAL → 20, PLUMBING → 15, WIFI/CLASSROOM → 10 |
| **Issue Age** | 15 | 7+ days → 15, 3+ days → 10, 1+ day → 5, today → 0 |

**Score → Priority mapping:**
- `≥ 75` → **CRITICAL**
- `50–74` → **HIGH**
- `25–49` → **MEDIUM**
- `< 25` → **LOW**

> Clients cannot set priority — it is always calculated server-side. The score is stored on the issue entity for full transparency and audit trail.

---

## 🔄 Issue Lifecycle

```
OPEN → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED
```

Invalid transitions (e.g., resolving an OPEN issue, re-opening a CLOSED issue) are rejected with a `409 Conflict` response and a clear error message.

---

## 🌐 API Overview

### Issues
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/issues` | Create issue (priority auto-calculated) |
| `GET` | `/api/issues` | List all issues |
| `GET` | `/api/issues/{id}` | Get issue by ID |
| `PUT` | `/api/issues/{id}` | Update issue (recalculates priority) |
| `DELETE` | `/api/issues/{id}` | Delete issue |
| `PUT` | `/api/issues/{id}/assign` | Assign staff member |
| `PUT` | `/api/issues/{id}/status` | Update status (validates transition) |
| `PUT` | `/api/issues/{id}/resolve` | Resolve with mandatory notes |
| `GET` | `/api/issues/search` | Multi-filter search |

### Dashboard
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/dashboard/summary` | Full statistics summary |
| `GET` | `/api/dashboard/by-category` | Count per category |
| `GET` | `/api/dashboard/by-status` | Count per status |
| `GET` | `/api/dashboard/by-priority` | Count per priority |

### Error Response Format
```json
{
  "timestamp": "2026-09-28T11:00:00",
  "status": 409,
  "error": "Invalid Workflow Transition",
  "message": "Cannot transition from OPEN to RESOLVED. OPEN issues can only be moved to ASSIGNED",
  "path": "/api/issues/5/status"
}
```

---

## 🗄️ Database Structure

```sql
TABLE issues (
  id               BIGINT AUTO_INCREMENT PK,
  issue_code       VARCHAR(20) UNIQUE NOT NULL,  -- e.g. SCI-20260928-4721
  reported_by      VARCHAR(100) NOT NULL,
  category         ENUM(ELECTRICAL|PLUMBING|CLASSROOM|WIFI|CLEANLINESS|SAFETY|OTHER),
  location         VARCHAR(200) NOT NULL,
  description      TEXT NOT NULL,
  affected_users   INT NOT NULL,
  safety_impact    TINYINT NOT NULL,
  priority         ENUM(LOW|MEDIUM|HIGH|CRITICAL),
  priority_score   INT,                           -- Engine score for transparency
  status           ENUM(OPEN|ASSIGNED|IN_PROGRESS|RESOLVED|CLOSED),
  assigned_to      VARCHAR(100),
  resolution_notes TEXT,
  reported_at      DATETIME,
  updated_at       DATETIME,
  resolved_at      DATETIME
)
```

Indexes: `status`, `priority`, `category`, `reported_by`, `reported_at`

---

## 🛠️ Technology Stack

**Backend**
- Java 17
- Spring Boot 3.2.4
- Spring Data JPA + Hibernate
- Spring Web (REST)
- Bean Validation (Jakarta)
- MySQL 8+ / H2 (tests)
- Maven

**Frontend**
- HTML5, CSS3, Vanilla JavaScript
- Chart.js 4.4 (dashboard charts)
- Google Fonts (Inter)
- Responsive design

---

## 📸 Screenshots

> *(Add screenshots here after first deployment)*

| Landing Page | Student Dashboard | Admin Dashboard |
|---|---|---|
| *(screenshot)* | *(screenshot)* | *(screenshot)* |

| Report Issue | Issue Details | Priority Breakdown |
|---|---|---|
| *(screenshot)* | *(screenshot)* | *(screenshot)* |

---

## ⚙️ Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8+

### 1. Database Setup

```bash
mysql -u root -p
```

```sql
CREATE DATABASE smart_campus CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Optionally load sample data:
```bash
mysql -u root -p smart_campus < database/schema.sql
```

### 2. Configuration

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Edit `application.properties` with your MySQL credentials:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smart_campus?...
spring.datasource.username=your_username
spring.datasource.password=your_password
```

> `application.properties` is in `.gitignore` — your credentials will never be committed.

### 3. Run the Backend

```bash
mvn spring-boot:run
```

The application starts at `http://localhost:8080`.

### 4. Access the Frontend

Open `http://localhost:8080` in your browser.

No separate frontend server needed — Spring Boot serves the static files.

---

## 🧪 Running Tests

```bash
mvn test
```

Tests run against H2 in-memory database — no MySQL required for testing.

**Test coverage:**
- `PriorityEngineTest` — 18 tests covering all scoring tiers and edge cases
- `SlaCalculatorTest` — 5 tests covering SLA tracking bounds
- `IssueWorkflowValidatorTest` — 10 tests covering all valid/invalid transitions
- `IssueServiceTest` — 14 integration tests (creation, lifecycle, search, delete)
- `DashboardServiceTest` — 4 integration tests verifying live counts

**Total: 51 tests, 0 failures**

---

## 🚀 Future Enhancements

- JWT-based authentication with Spring Security
- Email/SMS notifications on issue assignment
- File attachment support (photo of the issue)
- Priority escalation cron job (re-evaluates open issues daily)
- Admin role management (multiple admin users)
- PDF report export for campus management
- Mobile app (Android/iOS) using the REST API

---

## 📝 Notes

- This project is built for portfolio/placement demonstration purposes.
- The priority engine algorithm is intentionally designed to be simple and explainable.
- All dashboard statistics are backed by live database queries — no hardcoded values.
- The project follows clean layered architecture principles: Controller → Service → Repository → Entity.
