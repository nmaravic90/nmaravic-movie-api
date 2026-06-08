# Movie API Service 🎬

A hands-on REST API for Movie management built with **Spring Boot** and **Java**.
Created to practice modern Java backend development including API design, OAuth2 security,
integration testing, and Docker deployments.

## 📊 Status
[![CI Pipeline](https://github.com/nmaravic90/nmaravic-movie-api/actions/workflows/ci.yml/badge.svg)](https://github.com/nmaravic90/nmaravic-movie-api/actions/workflows/ci.yml)
## 🛠️ Technologies
![Java](https://img.shields.io/badge/Java_26-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_4.0-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL_16-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Keycloak](https://img.shields.io/badge/Keycloak-4D4D4D?style=for-the-badge&logo=keycloak&logoColor=white)
![OpenAPI](https://img.shields.io/badge/OpenAPI-6BA539?style=for-the-badge&logo=openapiinitiative&logoColor=white)
![Bucket4j](https://img.shields.io/badge/Bucket4j_Rate_Limiting-0052CC?style=for-the-badge&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit_5-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![Mockito](https://img.shields.io/badge/Mockito-5-6DB33F?style=for-the-badge&logoColor=white)
![Testcontainers](https://img.shields.io/badge/Testcontainers-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Bruno](https://img.shields.io/badge/Bruno-EA7343?style=for-the-badge&logoColor=white)
![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white)
![Python](https://img.shields.io/badge/Python_3.x-3776AB?style=for-the-badge&logo=python&logoColor=white)

## 🚀 Features
- **Contract-First Development**: API defined and generated via OpenAPI specs.
- **Advanced Security**: OAuth2 Resource Server with JWT integration.
- **Production Ready**: Rate limiting (Bucket4j) and Docker builds.
- **Robust Testing**: Unit tests with **JUnit 5** & **Mockito**, and Integration tests using **Testcontainers** (PostgreSQL).

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
**Author**: [Nikola Maravić](https://github.com/nmaravic90)
