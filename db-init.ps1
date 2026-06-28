# ============================================================
# DB 初始化 / 套用 schema 改動（Windows PowerShell）
# ============================================================
# 用途：
#   1. 第一次 setup 後建表 + 灌 seed 資料（setup.ps1 會自動 call）
#   2. 你或別人 push 了新表 schema 後，git pull 後跑這個套用
#      → schema.sql 用 CREATE TABLE IF NOT EXISTS，重複跑安全
#      → seed.sql 只在 users 表是空的時候跑（防重複資料）
#
# 不會做的事（要 destructive 改動請用 db-reset.ps1）：
#   - 不會 DROP 任何表
#   - 不會更新已存在的表結構（ALTER TABLE）
#   - 不會清空已有資料
#
# 前置：db container 必須是 running 狀態
# 用法：.\db-init.ps1
# ============================================================

$ErrorActionPreference = "Stop"

# PowerShell 預設 pipe 給 native exe 用 ASCII / Windows-1252，
# 中文 SQL 內容會被改碼 → 強制改成 UTF-8（無 BOM）。
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)

$DbUser = "root"
$DbPass = "root"
$DbName = "forum"
$SchemaFile = "forum_sb/src/main/resources/sql/schema.sql"
$SeedFile = "forum_sb/src/main/resources/sql/seed.sql"

# mysql client 連線 charset：容器內 locale 是 C，client 預設用 latin1。
# 把 UTF-8 bytes 當 latin1 收 → 中文亂碼。強制覆蓋成 utf8mb4。
$MysqlOpts = "--default-character-set=utf8mb4"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  DB Init" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. 確認 db container 在跑
$dbStatus = docker compose ps db 2>$null | Out-String
if ($dbStatus -notmatch "Up") {
    Write-Host "[!] db container 沒在跑，請先跑 'docker compose up -d'" -ForegroundColor Red
    exit 1
}

# 2. 套用 schema
Write-Host "[1/2] Applying schema..." -ForegroundColor Yellow
Get-Content $SchemaFile -Raw -Encoding UTF8 | docker compose exec -T db mysql $MysqlOpts -u"$DbUser" -p"$DbPass" $DbName
Write-Host "      OK"

# 3. 檢查 users 表是否為空
$userCountRaw = docker compose exec -T db mysql $MysqlOpts -u"$DbUser" -p"$DbPass" -N -e "SELECT COUNT(*) FROM $DbName.users" 2>$null
$userCount = ($userCountRaw -as [int])

if ($userCount -eq 0 -or $null -eq $userCount) {
    Write-Host "[2/2] users 表是空的 -> 灌 seed 資料..." -ForegroundColor Yellow
    Get-Content $SeedFile -Raw -Encoding UTF8 | docker compose exec -T db mysql $MysqlOpts -u"$DbUser" -p"$DbPass" $DbName
    Write-Host "      OK (3 users + 10 articles + comments + likes)"
} else {
    Write-Host "[2/2] users 表已有 $userCount 筆資料 -> 跳過 seed（防重複）" -ForegroundColor Yellow
    Write-Host "      想重灌 seed 請用 .\db-reset.ps1"
}

Write-Host ""
Write-Host "Demo accounts (password: 'password'):"
Write-Host "  alice@example.com"
Write-Host "  bob@example.com"
Write-Host "  charlie@example.com"
Write-Host ""
