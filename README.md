# Movie API Service 🎬

A REST API for Movie management built with the latest **Spring Boot 4.0** and **Java 26**.

![Java](https://img.shields.io/badge/Java-26-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Keycloak](https://img.shields.io/badge/Keycloak-JWT-4D4D4D?logo=keycloak&logoColor=white)
![Bucket4j](https://img.shields.io/badge/Bucket4j-Rate%20Limiting-blue)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED)

![JUnit5](https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white)
![Mockito](https://img.shields.io/badge/Mockito-5-green)
![Testcontainers](https://img.shields.io/badge/Testcontainers-PostgreSQL-2496ED?logo=docker&logoColor=white)
![Python](https://img.shields.io/badge/Python-3.x-3776AB?logo=python&logoColor=white)
![Bruno](https://img.shields.io/badge/Bruno-API%20Client-orange?logo=bruno&logoColor=white)

## 🚀 Features
- **Contract-First Development**: API defined and generated via OpenAPI specs.
- **Advanced Security**: OAuth2 Resource Server with JWT integration.
- **Production Ready**: Rate limiting (Bucket4j) and Docker builds.
- **Robust Testing**: Unit tests with **JUnit 5** & **Mockito**, and Integration tests using **Testcontainers** (PostgreSQL).

## 🛠 Tech Stack
- **Backend**: Java 26, Spring Boot 4.0.x, Maven
- **Database**: PostgreSQL, Spring Data JPA
- **Automation**: Python 3.x
- **DevOps**: Docker & Docker Compose

## ⚙️ Configuration
The application uses environment variables for configuration. You can set these in your `.env` file or environment.

### 🗄️ Database
| Variable | Description | Example |
|---|---|---|
| `DB_URL` | PostgreSQL JDBC connection URL | `jdbc:postgresql://localhost:5432/moviedb` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `secret` |

### 🌐 Server
| Variable | Description | Example |
|---|---|---|
| `SERVER_SERVLET_CONTEXT_PATH` | Base path for all API endpoints | `/api/v1` |

### 🎬 Movie Settings
| Variable | Description | Example |
|---|---|---|
| `MOVIE_PAGE_SIZE` | Default page size for movie listings | `20` |
| `MOVIE_TOP_RATED_LIMIT` | Default limit for top rated movies | `10` |
| `MOVIE_IMAGE_UPLOAD_DIR` | Local directory for uploaded images | `/uploads/images` |
| `MOVIE_IMAGE_SERVER_BASE_URL` | Base URL for serving images | `http://localhost:8080` |

### 🔐 Security (Keycloak)

| Variable | Description | Example |
|---|---|---|
| `KEYCLOAK_ISSUER_URI` | Keycloak JWT issuer URI | `http://localhost:8180/realms/movie-realm` |
| `KEYCLOAK_JWK_SET_URI` | Keycloak JWK set URI for token validation | `http://localhost:8180/realms/movie-realm/protocol/openid-connect/certs` |

> Actuator health is available on port `8081` at `/actuator/health`.

## 🚦 Quick Start
The easiest way to start and test the entire stack is using the provided automation script:

1. **Run & Test**:
   ```bash
   python scripts/run_and_test.py
   ```
   *This handles environment setup, docker-compose, and health checks.*

2. **Manual Launch**: `docker-compose up -d`
3. **Explore API**: `http://localhost:8080/api/v1/swagger-ui/index.html`

## 📂 Project Structure
- `/src`: Core application and Java tests.
- `/scripts`: Python automation scripts (including `run_and_test.py`).
- `/collection`: API test collections for Postman/Bruno.

## 🧪 Testing
- **Java Suite**: `mvn test` (JUnit, Mockito, IT, Testcontainers).
- **Automation Suite**: `python scripts/run_and_test.py`
- **Manual API Tests**: Use collections in `/collection`.

---
**Author**: Nikola Maravic
