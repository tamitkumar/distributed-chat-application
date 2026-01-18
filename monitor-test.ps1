# Real-Time Distributed Chat Test Monitor

Write-Host "========================================"
Write-Host "  DISTRIBUTED CHAT TEST MONITOR"
Write-Host "========================================"
Write-Host ""

# Container Status
Write-Host "`n== CONTAINER STATUS ==" -ForegroundColor Cyan
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | Select-String -Pattern "chat"

# Server 1 Activity
Write-Host "`n== SERVER 1 (PORT 8080) - RECENT ACTIVITY ==" -ForegroundColor Cyan
docker logs chat-app-1 --since 30s 2>&1 | Select-String -Pattern "Connected|BROADCAST|UNICAST|MULTICAST|Routing|Received" | Select-Object -Last 15

# Server 2 Activity  
Write-Host "`n== SERVER 2 (PORT 8081) - RECENT ACTIVITY ==" -ForegroundColor Cyan
docker logs chat-app-2 --since 30s 2>&1 | Select-String -Pattern "Connected|BROADCAST|UNICAST|MULTICAST|Routing|Received" | Select-Object -Last 15

# Error Check
Write-Host "`n== ERROR CHECK ==" -ForegroundColor Cyan
$errors1 = docker logs chat-app-1 --since 1m 2>&1 | Select-String -Pattern "ERROR|Exception" | Select-Object -Last 5
$errors2 = docker logs chat-app-2 --since 1m 2>&1 | Select-String -Pattern "ERROR|Exception" | Select-Object -Last 5

if ($null -eq $errors1 -and $null -eq $errors2) {
    Write-Host "NO ERRORS FOUND!" -ForegroundColor Green
} else {
    Write-Host "ERRORS DETECTED:" -ForegroundColor Red
    if ($null -ne $errors1) {
        Write-Host "`nServer 1:" -ForegroundColor Yellow
        $errors1
    }
    if ($null -ne $errors2) {
        Write-Host "`nServer 2:" -ForegroundColor Yellow
        $errors2
    }
}

# Redis Status
Write-Host "`n== REDIS STATUS ==" -ForegroundColor Cyan
$redisStatus = docker exec chat-redis redis-cli PING 2>&1
if ($redisStatus -eq "PONG") {
    Write-Host "Redis: HEALTHY" -ForegroundColor Green
} else {
    Write-Host "Redis: UNHEALTHY" -ForegroundColor Red
}

# MySQL Status
Write-Host "`n== MYSQL STATUS ==" -ForegroundColor Cyan
$mysqlCount = docker exec chat-mysql mysql -uchatuser -pchatpass -D chat -e "SELECT COUNT(*) as total FROM messages;" 2>&1 | Select-String -Pattern "^\d"
Write-Host "Total messages in DB: $mysqlCount" -ForegroundColor Cyan

Write-Host "`n========================================"
Write-Host "  MONITORING COMPLETE"
Write-Host "========================================`n"
