# ============================================================
# Forum（Spring Boot + Vue）一鍵啟動腳本（Windows PowerShell）
# ============================================================
# 用法：
#   .\setup.ps1
#
# 若 PowerShell 阻擋執行：以系統管理員身份開 PowerShell 跑一次
#   Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
#
# 設計：
#   setup.ps1   = 啟動 container + 第一次 DB init（內部 call db-init.ps1）
#   db-init.ps1 = 套用 schema + 灌 seed（之後 schema 改了單獨跑這個就好）
#   db-reset.ps1 = DROP 全部表重來
# ============================================================

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Forum (Spring Boot + Vue) setup" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 確認 Docker 在跑
try { docker info | Out-Null } catch {
    Write-Host "[!] Docker daemon 沒在跑，請先開啟 Docker Desktop 後重試" -ForegroundColor Red
    exit 1
}

# 2. 啟動 container
Write-Host "[1/5] Building images and starting containers (first time ~5-10 min)..." -ForegroundColor Yellow
docker compose up -d --build

# 3. 等 MySQL
Write-Host "[2/5] Waiting for MySQL to be ready..." -ForegroundColor Yellow
while ($true) {
    docker compose exec -T db mysqladmin ping -h localhost -uroot -proot --silent 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { break }
    Write-Host "." -NoNewline
    Start-Sleep -Seconds 1
}
Write-Host " ready"

# 4. 套用 DB schema + seed（用獨立腳本，跟 setup 解耦）
Write-Host "[3/5] Initializing DB schema and seed data..." -ForegroundColor Yellow
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
& "$scriptDir\db-init.ps1"

# 5. 等 Spring Boot
Write-Host "[4/5] Waiting for Spring Boot to start (Maven downloads ~3-5 min first time)..." -ForegroundColor Yellow
Write-Host "    Tip: 想看進度 -> 另開一個 terminal 跑 'docker compose logs -f backend'" -ForegroundColor Gray
while ($true) {
    try {
        Invoke-WebRequest -Uri "http://localhost:8090/api/articles" -UseBasicParsing -TimeoutSec 2 | Out-Null
        break
    } catch {
        Write-Host "." -NoNewline
        Start-Sleep -Seconds 3
    }
}
Write-Host " ready"

# 6. 等 Vite
Write-Host "[5/5] Waiting for Vite dev server to start (npm install ~1-2 min first time)..." -ForegroundColor Yellow
while ($true) {
    try {
        Invoke-WebRequest -Uri "http://localhost:5173" -UseBasicParsing -TimeoutSec 2 | Out-Null
        break
    } catch {
        Write-Host "." -NoNewline
        Start-Sleep -Seconds 2
    }
}
Write-Host " ready"

Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host "  Setup complete!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Frontend  : http://localhost:5173"
Write-Host "  Backend   : http://localhost:8090"
Write-Host "  Mailpit   : http://localhost:8026"
Write-Host "  MySQL     : localhost:3308 (root / root)"
Write-Host ""
Write-Host "  Demo accounts (password: 'password'):"
Write-Host "    alice@example.com"
Write-Host "    bob@example.com"
Write-Host "    charlie@example.com"
Write-Host ""
Write-Host "  常用指令："
Write-Host "    docker compose down       停止 container（DB 資料保留）"
Write-Host "    .\db-init.ps1             套用新 schema（git pull 後跑）"
Write-Host "    .\db-reset.ps1            砍掉所有表重建（destructive）"
Write-Host ""
