# ✅ Validation Document - Distributed Chat Application

## Table of Contents
1. [Pre-Deployment Checklist](#pre-deployment-checklist)
2. [Environment Setup Validation](#environment-setup-validation)
3. [Application Build Validation](#application-build-validation)
4. [Docker Setup Validation](#docker-setup-validation)
5. [Database Validation](#database-validation)
6. [Redis Validation](#redis-validation)
7. [Application Startup Validation](#application-startup-validation)
8. [API Endpoint Validation](#api-endpoint-validation)
9. [WebSocket Validation](#websocket-validation)
10. [Integration Testing Validation](#integration-testing-validation)
11. [Performance Validation](#performance-validation)
12. [Security Validation](#security-validation)

---

## Pre-Deployment Checklist

### Required Software

| Software | Required Version | Check Command | Status |
|----------|------------------|---------------|--------|
| Java | 21+ (25 recommended) | `java -version` | ⬜ |
| Gradle | 8.0+ (9.2 recommended) | `gradle --version` | ⬜ |
| Docker | 20.0+ | `docker --version` | ⬜ |
| Docker Compose | 2.0+ | `docker-compose --version` | ⬜ |
| Git | 2.0+ | `git --version` | ⬜ |

### Optional Software (for database access)

| Software | Purpose | Status |
|----------|---------|--------|
| MySQL Client | Connect to MySQL from terminal | ⬜ |
| IntelliJ DataGrip / MySQL Workbench | GUI database access | ⬜ |

### Validation Steps

```bash
# 1. Check Java version
java -version
# Expected: java version "21" or higher

# 2. Check Gradle
gradle --version
# Expected: Gradle 8.x or higher

# 3. Check Docker
docker --version
# Expected: Docker version 20.x or higher

# 4. Check Docker Compose
docker-compose --version
# Expected: Docker Compose version 2.x or higher
```

**✅ Validation Criteria**: All software installed with correct versions

---

## Environment Setup Validation

### Step 1: Clone Repository
```bash
cd D:\KAnsS\DEMO
ls distributed-chat-application
```

**✅ Expected Output**: Project directory exists with all files

### Step 2: Verify Project Structure
```bash
cd distributed-chat-application
ls -R
```

**✅ Expected Structure**:
```
distributed-chat-application/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
├── build.gradle
├── docker-compose.yml
├── Dockerfile
└── README.md
```

### Step 3: Check Configuration Files
```bash
# Check application.yml
cat src/main/resources/application.yml

# Check docker-compose.yml
cat docker-compose.yml
```

**✅ Validation Criteria**: All configuration files present and properly formatted

---

## Application Build Validation

### Step 1: Clean Build
```bash
./gradlew clean
```

**✅ Expected Output**:
```
BUILD SUCCESSFUL in Xs
```

### Step 2: Compile Application
```bash
./gradlew build -x test
```

**✅ Expected Output**:
```
BUILD SUCCESSFUL in Xs
X actionable tasks: X executed
```

**❌ If Build Fails**:
1. Check Java version: `java -version`
2. Check for compilation errors in output
3. Verify all dependencies are accessible
4. Run: `./gradlew clean build --refresh-dependencies`

### Step 3: Run Unit Tests
```bash
./gradlew test
```

**✅ Expected Output**:
```
BUILD SUCCESSFUL in Xs
70 tests completed, 0 failed, 0 skipped
```

**Test Results Location**: `build/reports/tests/test/index.html`

**✅ Validation Criteria**: 
- All tests pass (70/70)
- No compilation errors
- Test coverage > 85%

---

## Docker Setup Validation

### Step 1: Build Docker Image
```bash
docker build -t chat-app .
```

**✅ Expected Output**:
```
Successfully built <image-id>
Successfully tagged chat-app:latest
```

### Step 2: Verify Docker Image
```bash
docker images | grep chat-app
```

**✅ Expected Output**:
```
chat-app    latest    <image-id>    X minutes ago    XXX MB
```

### Step 3: Start Docker Services
```bash
docker-compose up -d
```

**✅ Expected Output**:
```
Creating chat-mysql ... done
Creating chat-redis ... done
Creating chat-app-1 ... done
Creating chat-app-2 ... done
```

### Step 4: Check Running Containers
```bash
docker-compose ps
```

**✅ Expected Output**:
```
NAME                 STATUS          PORTS
chat-haproxy         Up X minutes    0.0.0.0:80->80/tcp, 0.0.0.0:8404->8404/tcp
chat-mysql           Up X minutes    0.0.0.0:3307->3306/tcp
chat-redis           Up X minutes    0.0.0.0:6379->6379/tcp
chat-kafka           Up X minutes    0.0.0.0:9092->9092/tcp
chat-zookeeper       Up X minutes    2181/tcp
chat-app-1           Up X minutes    0.0.0.0:8080->8080/tcp
chat-app-2           Up X minutes    0.0.0.0:8081->8080/tcp
```

**✅ Validation Criteria**: All 7 containers running and healthy

**⚠️ Important Port Changes:**
- MySQL exposed on **3307** (not 3306) to avoid conflicts with local MySQL
- HAProxy on port 80 (main) and 8404 (stats dashboard)
- Kafka on port 9092 for message distribution

---

## Database Validation

### Step 1: Check MySQL Container
```bash
docker exec -it chat-mysql mysql -uroot -proot -e "SHOW DATABASES;"
```

**✅ Expected Output**:
```
+--------------------+
| Database           |
+--------------------+
| chat               |
| information_schema |
| mysql              |
| performance_schema |
+--------------------+
```

### Step 2: Verify Database Users (mysql-init.sql)
```bash
docker exec -it chat-mysql mysql -uroot -proot -e "SELECT user, host FROM mysql.user WHERE user IN ('root', 'chatuser');"
```

**✅ Expected Output**:
```
+----------+-----------+
| user     | host      |
+----------+-----------+
| chatuser | %         |
| chatuser | 127.0.0.1 |
| chatuser | localhost |
| root     | %         |
| root     | 127.0.0.1 |
| root     | localhost |
+----------+-----------+
```

**Note**: The `mysql-init.sql` script automatically creates these users with proper authentication.

### Step 3: Test External Connection (from Host)
```bash
# Using MySQL client from Windows/Mac/Linux
mysql -h 127.0.0.1 -P 3307 -u chatuser -p
# Enter password: chatpass

# Once connected:
SHOW DATABASES;
USE chat;
SHOW TABLES;
```

**✅ Expected**: Successful connection from host machine on port 3307

### Step 4: Verify Database Tables
```bash
docker exec -it chat-mysql mysql -uroot -proot -e "USE chat; SHOW TABLES;"
```

**✅ Expected Output**:
```
+------------------+
| Tables_in_chat   |
+------------------+
| messages         |
| rooms            |
| room_members     |
| user_rooms       |
| users            |
+------------------+
```

**Note**: Tables are auto-created by Hibernate on first application startup.

### Step 3: Check Table Structure
```bash
docker exec -it chat-mysql mysql -uroot -proot -e "USE chat; DESCRIBE messages;"
```

**✅ Expected Output**:
```
+------------------+--------------+------+-----+---------+-------+
| Field            | Type         | Null | Key | Default | Extra |
+------------------+--------------+------+-----+---------+-------+
| id               | varchar(36)  | NO   | PRI | NULL    |       |
| room_id          | varchar(36)  | NO   |     | NULL    |       |
| sender_id        | varchar(36)  | NO   |     | NULL    |       |
| sender_username  | varchar(100) | NO   |     | NULL    |       |
| content          | text         | NO   |     | NULL    |       |
| type             | varchar(20)  | NO   |     | NULL    |       |
| timestamp        | datetime     | NO   |     | NULL    |       |
| server_id        | varchar(36)  | NO   |     | NULL    |       |
+------------------+--------------+------+-----+---------+-------+
```

**✅ Validation Criteria**: 
- Database `chat` exists
- All tables created
- Correct schema structure

---

## Kafka Validation

### Step 1: Check Kafka and Zookeeper
```bash
# Check containers
docker ps | grep -E 'kafka|zookeeper'

# Check Zookeeper
docker exec chat-zookeeper bash -c "echo ruok | nc localhost 2181"
# Expected: imok
```

**✅ Expected**: Both containers running and healthy

### Step 2: List Kafka Topics
```bash
docker exec chat-kafka kafka-topics --list --bootstrap-server localhost:9092
```

**✅ Expected Output**:
```
chat.broadcast
chat.multicast
chat.unicast
```

**Note**: These 3 fixed topics are created automatically by the application.

### Step 3: Monitor Kafka Messages (Optional)
```bash
# Watch UNICAST messages
docker exec chat-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic chat.unicast \
  --from-beginning

# In another terminal, send a test message through the app
# You should see JSON messages appearing
```

**✅ Validation Criteria**: 
- Kafka and Zookeeper running
- 3 topics created (unicast, multicast, broadcast)
- Messages flowing through topics

---

## Redis Validation

### Step 1: Check Redis Connection
```bash
docker exec -it chat-redis redis-cli ping
```

**✅ Expected Output**:
```
PONG
```

### Step 2: Test Redis Operations
```bash
docker exec -it chat-redis redis-cli SET test "Hello Redis"
docker exec -it chat-redis redis-cli GET test
```

**✅ Expected Output**:
```
OK
"Hello Redis"
```

### Step 3: Check Redis Info
```bash
docker exec -it chat-redis redis-cli INFO server
```

**✅ Expected Output**: Redis server information displayed

**✅ Validation Criteria**: 
- Redis responding to PING
- Can SET and GET values
- Server info accessible

---

## Application Startup Validation

### Step 1: Check Application Logs
```bash
docker logs chat-app-1
```

**✅ Expected Output (Key Lines)**:
```
Starting ChatApplication...
Tomcat started on port(s): 8080 (http)
Started ChatApplication in X.XXX seconds
```

### Step 2: Verify Application Health
```bash
curl http://localhost:8080/actuator/health
```

**✅ Expected Output**:
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "redis": {"status": "UP"}
  }
}
```

### Step 3: Check Application Info
```bash
curl http://localhost:8080/actuator/info
```

**✅ Expected Output**: Application information displayed

**✅ Validation Criteria**: 
- Application starts without errors
- Health endpoint returns UP
- All components (DB, Redis) healthy

---

## API Endpoint Validation

### Test 1: Health Check
```bash
curl http://localhost:8080/actuator/health
```

**✅ Expected Output**:
```json
{
  "status": "UP"
}
```

### Test 2: OTP Authentication - Send OTP
```bash
curl -X POST http://localhost:8080/api/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+919876543210"
  }'
```

**✅ Expected Output**:
```json
{
  "message": "OTP sent successfully"
}
```

**Check OTP in logs**:
```bash
docker logs chat-app-1 --tail 20 | Select-String "OTP"
```

### Test 3: OTP Authentication - Verify OTP
```bash
# Use the OTP from logs (in demo mode)
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+919876543210",
    "otp": "123456",
    "username": "TestUser"
  }'
```

**✅ Expected Output**:
```json
{
  "id": "uuid-here",
  "phoneNumber": "+919876543210",
  "username": "TestUser",
  "isOnline": false,
  "createdAt": "2026-01-18T..."
}
```

### Test 4: Create Room
```bash
curl -X POST http://localhost:8080/api/chat/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Room",
    "description": "Test Description",
    "createdBy": "user-1",
    "isPrivate": false,
    "maxMembers": 100
  }'
```

**✅ Expected Output**:
```json
{
  "id": "<uuid>",
  "name": "Test Room",
  "description": "Test Description",
  "createdBy": "user-1",
  "createdAt": "2026-01-10T...",
  "private": false,
  "maxMembers": 100,
  "memberIds": ["user-1"]
}
```

### Test 3: Get All Rooms
```bash
curl http://localhost:8080/api/chat/rooms
```

**✅ Expected Output**:
```json
[
  {
    "id": "<uuid>",
    "name": "Test Room",
    ...
  }
]
```

### Test 4: Send Message
```bash
curl -X POST http://localhost:8080/api/chat/messages \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": "<room-id>",
    "senderId": "user-1",
    "senderUsername": "testuser",
    "content": "Hello World",
    "type": "BROADCAST"
  }'
```

**✅ Expected Output**:
```json
{
  "roomId": "<room-id>",
  "senderId": "user-1",
  "senderUsername": "testuser",
  "content": "Hello World",
  "type": "BROADCAST",
  "timestamp": "2026-01-10T...",
  "serverId": "<server-id>"
}
```

### Test 5: Get Message History
```bash
curl "http://localhost:8080/api/chat/rooms/<room-id>/messages?limit=50"
```

**✅ Expected Output**:
```json
[
  {
    "roomId": "<room-id>",
    "content": "Hello World",
    ...
  }
]
```

**✅ Validation Criteria**: 
- All endpoints respond with 200 OK
- Correct JSON structure returned
- Data persisted in database

---

## Frontend Chat Application Validation

### Test 1: Open Frontend
```bash
# Open chat-app.html in your browser
# Windows: Double-click the file or use:
start chat-app.html

# Or use file path:
# file:///D:/KAnsS/DEMO/distributed-chat-application/chat-app.html
```

**✅ Expected**: Login screen with phone number and username fields

### Test 2: User Registration Flow
**Step-by-Step**:
1. Enter phone number: `+919632064441`
2. Enter username: `TestUser1`
3. Click "Send OTP"
4. Check Docker logs for OTP:
   ```bash
   docker logs chat-app-1 --tail 20 | Select-String "OTP"
   ```
5. Enter OTP in frontend
6. Click "Verify & Login"

**✅ Expected**: Redirected to chat interface with 3 tabs (Room Chat, Direct, Broadcast)

### Test 3: Direct Message Flow
**Open 2 browser windows** (side-by-side):

**Window 1 (User A)**:
1. Login with `+919632064441` (Amit)
2. Click "Direct" tab
3. Click "+ New Direct Message"
4. Enter target phone: `+918085715271`
5. Type message: "Hi there!"
6. Click Send

**Window 2 (User B)**:
1. Login with `+918085715271` (Maa)
2. Click "Direct" tab
3. Should see "Amit" in conversation list
4. Click on "Amit"
5. Should see the message "Hi there!"
6. Reply: "Hello Amit!"

**✅ Expected**: 
- Both users see conversation list updated
- Messages appear in real-time
- Unread counts work correctly

### Test 4: Room Chat Flow
**Using same 2 windows**:

**Window 1 (Amit)**:
1. Click "Room Chat" tab
2. Click "+ Create Room"
3. Name: "Test Room", Description: "Testing"
4. Click "Create Room"
5. Room appears in list
6. Click on room
7. Send message: "Hello room!"

**Window 2 (Maa)**:
1. Click "Room Chat" tab
2. Should see "Test Room" in list
3. Click on "Test Room"
4. Should see Amit's message
5. Send reply: "Hello Amit!"

**✅ Expected**: Both users see all messages in the room

### Test 5: Broadcast
**Window 1 (Amit)**:
1. Click "Broadcast" tab
2. Type: "Important announcement!"
3. Click Send

**✅ Expected**: Window 2 (Maa) receives broadcast message immediately

### Test 6: Message Deletion
**Either window**:
1. Hover over your own message
2. Click red '✕' button
3. Confirm (if prompted)

**✅ Expected**: Message deleted from view and database

**✅ Validation Criteria**: 
- Frontend loads without errors
- OTP authentication works
- All 3 message types functional (UNICAST, MULTICAST, BROADCAST)
- Real-time message delivery
- Conversation list updates
- Message deletion works

---

## WebSocket Validation

### Test 1: WebSocket Connection
```javascript
// Using browser console or WebSocket client
const ws = new WebSocket('ws://localhost:8080/ws/chat');

ws.onopen = () => {
    console.log('✅ Connected');
};

ws.onmessage = (event) => {
    console.log('✅ Message received:', event.data);
};
```

**✅ Expected Output**:
```
✅ Connected
✅ Message received: {"type":"connected","message":"Welcome!"}
```

### Test 2: Send WebSocket Message
```javascript
ws.send(JSON.stringify({
    roomId: 'room-1',
    senderId: 'user-1',
    senderUsername: 'testuser',
    content: 'Hello via WebSocket',
    type: 'BROADCAST'
}));
```

**✅ Expected Output**: Message broadcast to all connected clients

**✅ Validation Criteria**: 
- WebSocket connection established
- Welcome message received
- Messages can be sent and received

---

## Integration Testing Validation

### Step 1: Run Integration Tests
```bash
./gradlew test --tests "*IntegrationTest"
```

**✅ Expected Output**:
```
BUILD SUCCESSFUL
10 tests completed, 0 failed
```

### Step 2: Full Flow Test

**Scenario**: Create room → Send message → Retrieve message

```bash
# 1. Create room
ROOM_ID=$(curl -X POST http://localhost:8080/api/chat/rooms \
  -H "Content-Type: application/json" \
  -d '{"name":"Integration Test Room","createdBy":"user-1"}' \
  | jq -r '.id')

echo "Room ID: $ROOM_ID"

# 2. Send message
curl -X POST http://localhost:8080/api/chat/messages \
  -H "Content-Type: application/json" \
  -d "{\"roomId\":\"$ROOM_ID\",\"senderId\":\"user-1\",\"senderUsername\":\"testuser\",\"content\":\"Test message\",\"type\":\"BROADCAST\"}"

# 3. Retrieve messages
curl "http://localhost:8080/api/chat/rooms/$ROOM_ID/messages"
```

**✅ Expected Output**: Message appears in room history

**✅ Validation Criteria**: 
- Complete flow works end-to-end
- Data persists across requests
- No errors in logs

---

## Performance Validation

### Test 1: Response Time
```bash
# Test API response time
time curl http://localhost:8080/api/chat/health
```

**✅ Expected**: Response time < 100ms

### Test 2: Concurrent Requests
```bash
# Send 100 concurrent requests
for i in {1..100}; do
  curl http://localhost:8080/api/chat/health &
done
wait
```

**✅ Expected**: All requests succeed, no errors

### Test 3: Load Test (Optional)
```bash
# Using Apache Bench
ab -n 1000 -c 10 http://localhost:8080/api/chat/health
```

**✅ Expected**:
- Requests per second > 100
- No failed requests
- Average response time < 100ms

**✅ Validation Criteria**: 
- Fast response times
- Handles concurrent requests
- No memory leaks

---

## Security Validation

### Test 1: CORS Configuration
```bash
curl -H "Origin: http://example.com" \
  -H "Access-Control-Request-Method: POST" \
  -X OPTIONS http://localhost:8080/api/chat/rooms
```

**✅ Expected**: CORS headers present

### Test 2: Input Validation
```bash
# Send invalid data
curl -X POST http://localhost:8080/api/chat/messages \
  -H "Content-Type: application/json" \
  -d '{}'
```

**✅ Expected**: 400 Bad Request or validation error

### Test 3: SQL Injection Prevention
```bash
# Try SQL injection
curl -X POST http://localhost:8080/api/chat/messages \
  -H "Content-Type: application/json" \
  -d '{"content":"'; DROP TABLE messages; --"}'
```

**✅ Expected**: Message stored safely, no SQL execution

**✅ Validation Criteria**: 
- CORS properly configured
- Input validation working
- SQL injection prevented

---

## Final Validation Checklist

### Pre-Production Checklist

| Item | Status | Notes |
|------|--------|-------|
| ⬜ All unit tests passing (70/70) | | |
| ⬜ Integration tests passing (10/10) | | |
| ⬜ Code coverage > 85% | | |
| ⬜ Docker containers running | | |
| ⬜ MySQL database accessible | | |
| ⬜ Redis cache working | | |
| ⬜ All API endpoints responding | | |
| ⬜ WebSocket connections working | | |
| ⬜ No errors in application logs | | |
| ⬜ Performance tests passed | | |
| ⬜ Security tests passed | | |
| ⬜ Documentation complete | | |

### Sign-Off

**Validated By**: _______________  
**Date**: _______________  
**Status**: ⬜ PASS / ⬜ FAIL  
**Notes**: _______________

---

## Troubleshooting Guide

### Issue 1: Application Won't Start
**Symptoms**: Container exits immediately

**Solution**:
```bash
# Check logs
docker logs chat-app-1

# Common fixes:
1. Verify MySQL is running
2. Check Redis connection
3. Verify application.yml configuration
4. Ensure port 8080 is not in use
```

### Issue 2: Database Connection Failed
**Symptoms**: "Cannot connect to MySQL"

**Solution**:
```bash
# Check MySQL container
docker ps | grep mysql

# Restart MySQL
docker-compose restart mysql

# Wait for health check
docker-compose ps
```

### Issue 3: Redis Connection Failed
**Symptoms**: "Cannot connect to Redis"

**Solution**:
```bash
# Check Redis container
docker ps | grep redis

# Test Redis
docker exec -it chat-redis redis-cli ping

# Restart Redis
docker-compose restart redis
```

### Issue 4: Tests Failing
**Symptoms**: Unit tests fail

**Solution**:
```bash
# Clean and rebuild
./gradlew clean test

# Run specific test
./gradlew test --tests ChatServiceTest

# Check test report
open build/reports/tests/test/index.html
```

---

## Next Steps

1. ✅ Complete all validation steps above
2. ✅ Check off all items in Final Validation Checklist
3. ✅ Document any issues found
4. ✅ Fix issues and re-validate
5. ✅ Get sign-off from team lead
6. ✅ Deploy to production

---

**Validation Status**: ⬜ IN PROGRESS / ⬜ COMPLETE  
**Last Updated**: 2026-01-10  
**Version**: 1.0.0

