# 💬 Chat Service (Instagram Clone Backend)

The **Chat Service** is a core microservice in the [Instagram Clone Microservices Backend](https://github.com/Instagram-Api-Clone). It manages direct private messaging (DMs) between users, retrieves conversation history, and handles real-time messaging events.

---

## 🛠️ Features & Responsibilities
* **Private Direct Messaging**: Exposes APIs to send private textual messages between authenticated users.
* **Paginated Conversation History**: Fetches message history between two specific users with efficient database indices.
* **Event Dispatching**: Publishes messaging events to Apache Kafka topics for background processing, analytics, or real-time websocket delivery.
* **Microservice Security**: Relies on user context headers pre-validated and injected by the API Gateway.

---

## 🧱 Tech Stack
* **Framework**: Spring Boot 3
* **Language**: Java 21
* **Database**: PostgreSQL (relational message storage)
* **Message Broker**: Apache Kafka (message fanout events)
* **Discovery & Config**: Spring Cloud Client (integrated with Eureka and Config Server)
* **Monitoring**: New Relic APM agent
* **Containerization**: Docker (via Jib container builder)

---

## 📡 Key API Endpoints

All endpoints are protected and must be routed via the API Gateway. Downstream port: `8085`.

| Method | Endpoint | Headers required | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/chats/send` | `X-User-Id` (forwarded by Gateway) | Send a private chat message to a user |
| `GET` | `/api/v1/chats/history/{userId}` | `X-User-Id` (forwarded by Gateway) | Retrieve conversation history with a specific user |

---

## ⚙️ Running Locally

### Prerequisites
1. Spring Cloud **Discovery Server** must be running on port `8761`.
2. Spring Cloud **Configuration Server** must be running on port `8888`.
3. A running **PostgreSQL** instance.
4. A running **Apache Kafka** cluster.

### Launching
1. Set the following environment variables:
   * `DB_USER` & `DB_PASSWORD`: PostgreSQL server credentials.
   * `DB_NAME`: The name of the database (defaults to `instagram_chat_service`).
   * `KAFKA_BOOTSTRAP_SERVERS` (Optional): Kafka cluster address (defaults to `localhost:9092`).
2. Run the application:
   ```bash
   ./gradlew bootRun
   ```
   * The service starts up locally on port **`8085`**.
