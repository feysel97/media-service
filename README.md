# Spring Boot Media Microservice

A dedicated microservice for handling user media uploads (such as profile pictures) and serving media URLs securely.

## 🚀 Key Features

* **Multipart File Uploads:** Accepts and validates image uploads routed through the API Gateway.
* **Decoupled Storage:** Isolates file management from user profile metadata.
* **Gateway-Secured:** Relies on the API Gateway for JWT validation before processing uploads or URL requests.

## 🛠️ Tech Stack

* **Runtime:** Java 21 (LTS)
* **Framework:** Spring Boot 3.4.2, Spring Cloud Netflix Eureka Client
* **Build Tool:** Gradle 8.5+

## 🛤️ API Endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/v1/media/upload` | Protected | Uploads a media file (multipart/form-data) and returns its identifier/URL. |
| `GET` | `/api/v1/media/url/{filename}` | Protected | Retrieves the accessible URL or resource for a stored file. |

## ⚙️ Getting Started

1. Copy `application.yaml.example` to `application.yaml` and configure your storage path/credentials.
2. Ensure `service-registry` and `api-gateway` are running.
3. Run `./gradlew bootRun`.