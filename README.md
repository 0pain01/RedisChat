# 💬 Simple Chat Application using Spring Boot and Redis

This is a RESTful backend-only chat application built using **Spring Boot** and **Redis**. It allows users to:

- ✅ Create and join chat rooms
- ✅ Send and retrieve messages
- ✅ Receive real-time updates via Redis Pub/Sub
- ✅ Delete chat rooms
- ✅ Store and retrieve chat history using Redis Lists

---

## 🛠️ Tech Stack

- **Java 17+**
- **Spring Boot 3.5.x**
- **Redis**
- **Spring Data Redis**
- **Lombok**
- **JUnit + MockMvc** for unit testing

---

## 🚀 Features

| Feature                  | Status |
|--------------------------|--------|
| Create & join chat rooms | ✅      |
| Send messages            | ✅      |
| Real-time updates        | ✅      |
| Chat history (last N)    | ✅      |
| Delete room              | ✅      |
| Error handling           | ✅      |
| DTO validation           | ✅      |
| Unit test coverage       | ✅      |

---

## 📁 Redis Data Structures Used

| Key Pattern                        | Data Type   | Purpose                           |
|------------------------------------|-------------|------------------------------------|
| `chatroom:meta:{roomId}`           | Hash        | Room metadata                     |
| `chatroom:participants:{roomId}`   | Set         | List of joined users              |
| `chatroom:messages:{roomId}`       | List        | Chronological list of messages    |
| `chatroom:{roomId}` (Pub/Sub)      | Channel     | Real-time message broadcasting    |

---

## ✅ Prerequisites

- Java 17+
- Redis running locally (port 6379)
- Maven

### 🔧 Start Redis locally:
```bash
docker run -p 6379:6379 redis
```
Or use:
```bash
brew install redis
brew services start redis
```

---

## 🧪 Running the Application

### 🔧 1. Clone the repo

```bash
git clone https://github.com/yourusername/chatapp.git
cd chatapp
```

### 🔧 2. Build the app

```bash
./mvnw clean install
```

### 🔧 3. Run the app

```bash
./mvnw spring-boot:run
```

Server runs at:  
```
http://localhost:8080
```

---

## 🔌 API Endpoints

### ✅ Create a Chat Room
```http
POST /api/chatapp/chatrooms
```
**Request:**
```json
{ "roomName": "general" }
```

---

### ✅ Join a Chat Room
```http
POST /api/chatapp/chatrooms/{roomId}/join
```
**Request:**
```json
{ "participant": "guest_user" }
```

---

### ✅ Send a Message
```http
POST /api/chatapp/chatrooms/{roomId}/messages
```
**Request:**
```json
{
  "participant": "guest_user",
  "message": "Hello!"
}
```

---

### ✅ Retrieve Messages
```http
GET /api/chatapp/chatrooms/{roomId}/messages?limit=10
```
**Response:**
```json
{
  "messages": [
    {
      "participant": "guest_user",
      "message": "Hello!",
      "timestamp": "2024-01-01T10:00:00Z"
    }
  ]
}
```

---

### ✅ Delete a Chat Room
```http
DELETE /api/chatapp/chatrooms/{roomId}
```

---

## 🧪 Run Unit Tests

```bash
./mvnw test
```

---

## ✅ Sample Test Cases Covered

| Test Case                              | Status |
|----------------------------------------|--------|
| Create and join chat room              | ✅     |
| Send and retrieve messages             | ✅     |
| Real-time message via pub/sub (logged) | ✅     |
| Validation errors (missing fields)     | ✅     |
| Error handling (invalid rooms)         | ✅     |
| Delete room and confirm cleanup        | ✅     |
