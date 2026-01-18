# 💬 Distributed Chat Application

A production-ready, horizontally scalable, real-time chat application built with Spring Boot 4.0, Kafka, WebSocket, Redis, and MySQL.

## 🚀 Features

### Core Features
- ✅ **Phone-based Authentication** with OTP verification (demo mode)
- ✅ **Real-time messaging** via WebSocket with automatic reconnection
- ✅ **Full-Featured Web UI** (chat-app.html) - No additional frontend needed!
- ✅ **Multiple message types**: 
  - **UNICAST** - Direct 1-to-1 private messaging with conversation list
  - **MULTICAST** - Room-based group chat
  - **BROADCAST** - Global messages to all users
- ✅ **Room management**: Create, join, search rooms with real-time updates
- ✅ **Conversation List** - Track all direct message contacts with unread counts
- ✅ **Message Deletion** - Users can delete their own messages
- ✅ **Persistent storage** with MySQL (auto-schema generation)

### Distributed Architecture
- ✅ **Kafka-based messaging** for guaranteed delivery and persistence
- ✅ **Horizontal scalability** with multiple server instances
- ✅ **HAProxy load balancing** with health checks and automatic failover
- ✅ **Redis caching** for user sessions and offline message inbox
- ✅ **Custom Kafka Serializers** (Spring Boot 4.0 compatible)

### Developer Experience
- ✅ **REST API** for room and message management
- ✅ **Swagger/OpenAPI** documentation (http://localhost:8080/swagger-ui.html)
- ✅ **Docker support** for easy deployment with single command
- ✅ **IntelliJ/MySQL Workbench** ready - Direct database connection from host
- ✅ **Comprehensive documentation** - Architecture, Testing, Validation guides
- ✅ **Auto-initialization** - Database and users created automatically

## 📋 Prerequisites

- Java 21+
- Gradle 8.0+
- Docker & Docker Compose
- MySQL 8.0+ (or use Docker)
- Redis 7.0+ (or use Docker)

## 🏗️ Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│              DISTRIBUTED CHAT APPLICATION ARCHITECTURE                 │
└────────────────────────────────────────────────────────────────────────┘

                      ┌──────────────────────┐
                      │   chat-app.html      │
                      │  (Frontend Client)   │
                      └──────────┬───────────┘
                                 │ WebSocket + REST
                                 ↓
                      ┌──────────────────────┐
                      │  HAProxy (Port 80)   │
                      │   Load Balancer      │
                      └──────────┬───────────┘
                                 │ Round Robin
                    ┌────────────┴─────────────┐
                    ↓                          ↓
         ┌──────────────────┐       ┌───────────────────┐
         │  Chat Server 1   │       │  Chat Server 2    │
         │   (Port 8080)    │       │   (Port 8081)     │
         └────┬─────────┬───┘       └───┬──────────┬────┘
              │         │               │          │
              │  ┌──────▼───────────────▼───────┐  │
              │  │     Apache Kafka             │  │
              │  │  (Message Distribution)      │  │
              │  │  Topics: unicast,            │  │
              │  │  multicast, broadcast        │  │
              │  └──────────────────────────────┘  │
              │                                    │
         ┌────▼────────────────────────────────────▼────┐
         │              Redis (Port 6379)               │
         │   • User Sessions  • Online Status           │
         │   • OTP Storage    • Offline Message Inbox   │
         └──────────────────────────────────────────────┘
                                 │
                                 ↓
         ┌────────────────────────────────────────────────┐
         │         MySQL (Port 3307 external,             │
         │                Port 3306 internal)             │
         │   • users  • rooms  • messages  • user_rooms   │
         └────────────────────────────────────────────────┘

🔑 Key Design Decisions:
  • Kafka (not Redis Pub/Sub) - Guaranteed message delivery & persistence
  • HAProxy - Automatic failover and health monitoring
  • Port 3307 - Avoids conflict with local MySQL installations
  • Custom Serializers - Spring Boot 4.0 compatibility
```

## 🚀 Quick Start

### Option 1: Using Docker (Recommended)

```bash
# 1. Navigate to project directory
cd D:\KAnsS\DEMO\distributed-chat-application

# 2. Build the application
./gradlew clean build

# 3. Start all services (builds image if needed)
docker-compose up -d

# 4. Check status (wait for all services to be healthy)
docker-compose ps

# 5. View application logs
docker logs chat-app-1 -f

# 6. Open the chat application
# Open chat-app.html in your browser (double-click or use Live Server)
# Or: file:///D:/KAnsS/DEMO/distributed-chat-application/chat-app.html

# 7. Check OTP in logs (demo mode - OTPs are printed to console)
docker logs chat-app-1 --tail 30 | Select-String "OTP"
```

**Services will be available at:**
- 🌐 **Frontend**: `file:///D:/KAnsS/DEMO/distributed-chat-application/chat-app.html`
- 🚀 **HAProxy (Load Balancer)**: http://localhost:80
- 📊 **HAProxy Stats Dashboard**: http://localhost:8404/stats
- 🖥️ **Server 1 (Direct)**: http://localhost:8080
- 🖥️ **Server 2 (Direct)**: http://localhost:8081
- 🗄️ **MySQL (External)**: `127.0.0.1:3307` (user: `chatuser`, password: `chatpass`)
- 💾 **Redis**: `localhost:6379`
- 📚 **Swagger UI**: http://localhost:8080/swagger-ui.html

### 🎯 Quick Test - 2 Users Chatting

1. **Open two browser windows** with `chat-app.html`

2. **Login as User 1 (Amit)** - Left window:
   - Phone: `+919632064441`
   - Username: `Amit`
   - Click "Send OTP" → Get OTP from logs → Verify

3. **Login as User 2 (Maa)** - Right window:
   - Phone: `+918085715271`
   - Username: `Maa`
   - Click "Send OTP" → Get OTP from logs → Verify

4. **Test Direct Message**:
   - Amit clicks "Direct" tab → "+ New Direct Message"
   - Enter Maa's phone: `+918085715271`
   - Send: "Hi Maa!"
   - Maa should see "Amit" in conversation list
   - Maa can reply!

5. **Test Room Chat**:
   - Both click "Room Chat" tab
   - Amit creates room "Test Room"
   - Maa joins "Test Room"
   - Both can chat in the room!

### 🛑 Stop Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v

# Clean everything (Docker + build artifacts)
docker-compose down -v
docker system prune -af
./gradlew clean
```

### Option 2: Local Development

```bash
# 1. Start MySQL and Redis
docker-compose up -d mysql redis

# 2. Build the application
./gradlew clean build

# 3. Run the application
./gradlew bootRun

# Or run the JAR
java -jar build/libs/distributed-chat-application-1.0-SNAPSHOT.jar
```

## 📚 API Documentation

### Swagger UI
Open http://localhost:8080/swagger-ui.html for interactive API documentation.

### REST Endpoints

#### Authentication (OTP-based)
```bash
# Send OTP to phone number
POST /api/auth/send-otp
Content-Type: application/json

{
  "phoneNumber": "+919876543210"
}

# Response: OTP sent (check server logs in demo mode)

# Verify OTP and login/register
POST /api/auth/verify-otp
Content-Type: application/json

{
  "phoneNumber": "+919876543210",
  "otp": "123456",
  "username": "Alice"  // Optional: auto-generated if not provided
}

# Response: User object with details

# Get all users
GET /api/auth/users
```

#### Health Check
```bash
GET /actuator/health
GET /api/chat/health
```

#### Room Management
```bash
# Create room
POST /api/chat/rooms
Content-Type: application/json

{
  "name": "General Chat",
  "description": "Public chat room",
  "createdBy": "+919876543210",
  "private": false,
  "maxMembers": 100
}

# Get all rooms
GET /api/chat/rooms

# Get specific room
GET /api/chat/rooms/{roomId}

# Join room
POST /api/chat/rooms/{roomId}/join
Content-Type: application/json

{
  "userId": "+919876543210"
}

# Get room members
GET /api/chat/rooms/{roomId}/members

# Delete room
DELETE /api/chat/rooms/{roomId}
```

#### Message Management
```bash
# Send message (REST - alternatively use WebSocket)
POST /api/chat/messages
Content-Type: application/json

{
  "roomId": "room-id-or-target-phone",
  "senderId": "+919876543210",
  "senderUsername": "Alice",
  "content": "Hello World",
  "type": "UNICAST"  // or "MULTICAST" or "BROADCAST"
}

# Get message history
GET /api/chat/rooms/{roomId}/messages?limit=50

# Get paginated messages
GET /api/chat/rooms/{roomId}/messages/page?page=0&size=20

# Get message count
GET /api/chat/rooms/{roomId}/messages/count

# Delete message (only own messages)
DELETE /api/chat/messages/{messageId}
```

### WebSocket Endpoint

```javascript
// Connect to WebSocket with userId in query param
const userId = '+919876543210';  // Your phone number
const ws = new WebSocket(`ws://localhost:8080/ws/chat?userId=${encodeURIComponent(userId)}`);

ws.onopen = () => {
    console.log('✅ Connected to chat server');
};

// Send UNICAST message (1-to-1)
ws.send(JSON.stringify({
    roomId: '+918765432109',  // Target user's phone
    senderId: '+919876543210',
    senderUsername: 'Alice',
    content: 'Hi Bob!',
    type: 'UNICAST'
}));

// Send MULTICAST message (room)
ws.send(JSON.stringify({
    roomId: 'room-uuid-here',
    senderId: '+919876543210',
    senderUsername: 'Alice',
    content: 'Hello everyone in the room!',
    type: 'MULTICAST'
}));

// Send BROADCAST message (all users)
ws.send(JSON.stringify({
    roomId: 'broadcast',
    senderId: '+919876543210',
    senderUsername: 'Alice',
    content: 'Important announcement!',
    type: 'BROADCAST'
}));

// Receive messages
ws.onmessage = (event) => {
    const message = JSON.parse(event.data);
    console.log('📨 Received:', message);
    // message.type will be: 'UNICAST', 'MULTICAST', or 'BROADCAST'
};

ws.onerror = (error) => {
    console.error('❌ WebSocket error:', error);
};

ws.onclose = () => {
    console.log('🔌 Disconnected from chat server');
};
```

**Note**: The production-ready `chat-app.html` handles all WebSocket logic for you!

## 🧪 Testing

### Manual Testing with Frontend
1. Open `chat-app.html` in browser
2. Follow the Quick Test guide above
3. Test all features: OTP login, Direct Messages, Room Chat, Broadcast

### Automated Testing Scripts
```bash
# Comprehensive API testing
./test-comprehensive.ps1

# Real-time monitoring
./monitor-test.ps1
```

### Validation Checklist
Follow the complete validation guide in `VALIDATION.md`

### Complete Documentation
- 📖 **Architecture Documentation**: Open `ARCHITECTURE_DOCUMENTATION.html` in browser for complete HLD & LLD
- ✅ **Validation Guide**: `VALIDATION.md` - Step-by-step validation checklist
- 📋 **This README**: Quick start and API reference

## 📊 Project Structure

```
distributed-chat-application/
├── src/
│   ├── main/
│   │   ├── java/com/techbrain/chat/
│   │   │   ├── ChatApplication.java          # Main application entry point
│   │   │   ├── cofig/                        # Configuration classes
│   │   │   │   ├── AppConfig.java            # ObjectMapper bean
│   │   │   │   ├── ChatDBConfiguration.java  # JPA/Hibernate config
│   │   │   │   ├── CorsConfig.java           # CORS configuration
│   │   │   │   ├── DBConfig.java             # DataSource configuration
│   │   │   │   ├── DialectConfig.java        # Custom MySQL dialect
│   │   │   │   ├── KafkaConfig.java          # Kafka producer/consumer
│   │   │   │   ├── KafkaJsonSerializer.java  # Custom JSON serializer
│   │   │   │   ├── KafkaJsonDeserializer.java# Custom JSON deserializer
│   │   │   │   ├── RedisConfig.java          # Redis configuration
│   │   │   │   └── WebSocketConfig.java      # WebSocket configuration
│   │   │   ├── controller/                   # REST API controllers
│   │   │   │   ├── AuthController.java       # OTP auth endpoints
│   │   │   │   └── ChatController.java       # Chat endpoints
│   │   │   ├── service/                      # Business logic layer
│   │   │   │   ├── ChatService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── RoomService.java
│   │   │   │   ├── MessageService.java
│   │   │   │   ├── OtpService.java
│   │   │   │   ├── KafkaProducerService.java
│   │   │   │   ├── KafkaConsumerService.java
│   │   │   │   └── impl/                     # Service implementations
│   │   │   ├── repository/                   # Data access layer
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── MessageRepository.java
│   │   │   │   └── RoomRepository.java
│   │   │   ├── entity/                       # JPA entities
│   │   │   │   ├── UserEntity.java
│   │   │   │   ├── MessageEntity.java
│   │   │   │   └── RoomEntity.java
│   │   │   ├── to/                           # Transfer Objects (DTOs)
│   │   │   │   ├── Message.java
│   │   │   │   ├── Room.java
│   │   │   │   ├── User.java
│   │   │   │   └── ServiceInfo.java
│   │   │   ├── handler/                      # WebSocket message handlers
│   │   │   │   └── ChatWebSocketHandler.java
│   │   │   ├── stretegy/                     # Message routing strategies
│   │   │   │   ├── MessageRoutingStrategy.java
│   │   │   │   └── impl/
│   │   │   │       ├── UnicastStrategy.java
│   │   │   │       ├── MulticastStrategy.java
│   │   │   │       └── BroadcastStrategy.java
│   │   │   └── utils/                        # Utility classes
│   │   │       └── MessageType.java
│   │   └── resources/
│   │       ├── application.yml               # Main configuration
│   │       └── application-docker.yml        # Docker environment config
│   └── test/                                 # Test classes (removed for now)
├── chat-app.html                             # 🌐 Frontend web application
├── mysql-init.sql                            # 🗄️ Database initialization script
├── docker-compose.yml                        # 🐳 Docker Compose configuration
├── Dockerfile                                # 🐳 Docker image definition
├── haproxy.cfg                               # ⚖️ HAProxy load balancer config
├── build.gradle                              # 🔧 Gradle build configuration
├── settings.gradle                           # 🔧 Gradle settings
├── gradlew / gradlew.bat                     # 🔧 Gradle wrapper scripts
├── test-comprehensive.ps1                    # 🧪 Comprehensive test script
├── monitor-test.ps1                          # 📊 Real-time monitoring script
├── ARCHITECTURE_DOCUMENTATION.html           # 📖 Complete HLD & LLD docs
├── VALIDATION.md                             # ✅ Validation checklist
└── README.md                                 # 📋 This file
```

## 🔧 Configuration

### Application Configuration (application.yml)
```yaml
spring:
  datasource:
    # Local development - uses Java config (DBConfig.java)
    # Port 3307 to avoid conflict with local MySQL
  
  data:
    redis:
      host: localhost
      port: 6379
  
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: ${app.server-id:server-1}

server:
  port: 8080

app:
  server-id: server-1
  otp:
    demo-mode: true  # OTPs logged to console
```

### Docker Configuration (application-docker.yml)
```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/chat  # Internal Docker network
    username: chatuser
    password: chatpass
  
  data:
    redis:
      host: redis
      port: 6379
  
  kafka:
    bootstrap-servers: kafka:9092
```

### Database Connection (from Host Machine)

**For IntelliJ DataGrip, MySQL Workbench, etc.:**

| Setting | Value |
|---------|-------|
| Host | `127.0.0.1` |
| Port | `3307` ⚠️ (NOT 3306) |
| Database | `chat` |
| Username | `chatuser` or `root` |
| Password | `chatpass` or `root` |

**Why port 3307?** To avoid conflicts with local MySQL installations.

### Environment Variables (Docker)

- `DB_HOST` - Database host (default: `mysql` in Docker, `localhost` locally)
- `DB_PORT` - Database port (default: `3306` in Docker, `3307` locally)
- `DB_USER` - Database user (default: `root`)
- `DB_PASSWORD` - Database password (default: `root`)
- `APP_SERVER_ID` - Unique server identifier for Kafka consumer groups
- `SPRING_PROFILES_ACTIVE` - Active Spring profile (e.g., `docker`)

## 📈 Monitoring

### Actuator Endpoints
```bash
# Health check
GET http://localhost:8080/actuator/health

# Application info
GET http://localhost:8080/actuator/info

# Metrics
GET http://localhost:8080/actuator/metrics
```

## 🐛 Troubleshooting

### Application won't start
```bash
# 1. Check logs
docker logs chat-app-1 --tail 50

# 2. Check all containers are running
docker-compose ps

# 3. Restart services
docker-compose restart

# 4. Clean rebuild
./gradlew clean build
docker-compose down
docker-compose up -d --build
```

### Database connection error
```bash
# Check MySQL is running
docker ps | grep mysql

# Test connection from inside Docker
docker exec -it chat-mysql mysql -uroot -proot -e "SHOW DATABASES;"

# Test connection from host machine (port 3307)
mysql -h 127.0.0.1 -P 3307 -u chatuser -p
# Enter password: chatpass

# Common issue: Local MySQL on port 3306 conflicts
# Solution: We use port 3307 for Docker MySQL
```

### Redis connection error
```bash
# Check Redis is running
docker ps | grep redis

# Test connection
docker exec -it chat-redis redis-cli ping
# Expected: PONG

# Check Redis keys
docker exec -it chat-redis redis-cli KEYS "*"
```

### Kafka errors
```bash
# Check Kafka is running
docker ps | grep kafka

# List Kafka topics
docker exec chat-kafka kafka-topics --list --bootstrap-server localhost:9092

# Watch messages in a topic
docker exec chat-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic chat.unicast \
  --from-beginning
```

### OTP not visible
```bash
# OTPs are logged to console in demo mode
docker logs chat-app-1 --tail 30 | Select-String "OTP"

# Or watch logs in real-time
docker logs chat-app-1 -f | Select-String "OTP"
```

### Can't connect from IntelliJ to MySQL
```bash
# Ensure you're using:
# Host: 127.0.0.1 (NOT localhost)
# Port: 3307 (NOT 3306)
# User: chatuser
# Password: chatpass

# Test from command line first
mysql -h 127.0.0.1 -P 3307 -u chatuser -p
```

## 🛠️ Technology Stack

| Category | Technology | Version | Purpose |
|----------|-----------|---------|---------|
| **Language** | Java | 25 | Backend development |
| **Framework** | Spring Boot | 4.0 | Application framework |
| **Messaging** | Apache Kafka | 7.5 | Reliable message distribution |
| **WebSocket** | Spring WebSocket | 6.2 | Real-time bidirectional communication |
| **Cache** | Redis | 7 | Session management, OTP storage |
| **Database** | MySQL | 8.0 | Persistent storage |
| **Load Balancer** | HAProxy | 2.8 | Traffic distribution & failover |
| **Build Tool** | Gradle | 9.2 | Project build & dependency management |
| **Containerization** | Docker | Latest | Application packaging |
| **Orchestration** | Docker Compose | Latest | Multi-container deployment |
| **Frontend** | Vanilla JS | - | Web UI (chat-app.html) |
| **Documentation** | OpenAPI/Swagger | 3.0 | API documentation |

### Key Technical Decisions

1. **Kafka over Redis Pub/Sub**: Guaranteed message delivery, persistence, replay capability
2. **Custom Serializers**: Spring Boot 4.0 compatibility (deprecated classes replaced)
3. **Port 3307 for MySQL**: Avoid conflicts with local installations
4. **Phone-based Auth**: OTP verification for user registration/login
5. **HAProxy**: Battle-tested load balancer with health checks
6. **Single-page Frontend**: No framework dependency, just HTML/CSS/JS

## 📝 Validation

Follow the complete validation checklist in [VALIDATION.md](VALIDATION.md) to ensure everything is working correctly.

## 📖 Complete Documentation

- 🏗️ **[ARCHITECTURE_DOCUMENTATION.html](ARCHITECTURE_DOCUMENTATION.html)** - Complete HLD & LLD with diagrams
- ✅ **[VALIDATION.md](VALIDATION.md)** - Deployment validation checklist
- 📋 **[README.md](README.md)** - This file - Quick start guide

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Write tests
5. Submit a pull request




