# ============================================================
# DB 重置（DROP 所有表 + 重建 + 重灌 seed）（Windows PowerShell）
# ============================================================
# Destructive 操作！所有資料會被清空。
#
# 用途：
#   - schema 改變了（ALTER 欄位、改型別、刪欄位等），idempotent 套用做不到
#   - demo 過程資料被改得亂七八糟，想還原乾淨
#   - 想驗證「fresh install」的體驗
#
# 不需要砍 container / volume，只清表（保留 DB container 跟 named volume）。
#
# 前置：db container 必須是 running 狀態
# 用法：.\db-reset.ps1
# ============================================================

$ErrorActionPreference = "Stop"

$DbUser = "root"
$DbPass = "root"
$DbName = "forum"

Write-Host "==========================================" -ForegroundColor Red
Write-Host "  DB Reset (WILL DROP ALL DATA!)" -ForegroundColor Red
Write-Host "==========================================" -ForegroundColor Red
Write-Host ""
$confirm = Read-Host "確定要砍掉 $DbName 所有表並重建嗎？(y/N)"
if ($confirm -ne "y" -and $confirm -ne "Y") {
    Write-Host "已取消"
    exit 0
}

# 1. 確認 db container 在跑
$dbStatus = docker compose ps db 2>$null | Out-String
if ($dbStatus -notmatch "Up") {
    Write-Host "[!] db container 沒在跑，請先跑 'docker compose up -d'" -ForegroundColor Red
    exit 1
}

# 2. DROP 全部表
Write-Host "[1/2] Dropping all tables..." -ForegroundColor Yellow
$dropSql = @"
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS comment_likes;
DROP TABLE IF EXISTS article_likes;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS articles;
DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;
"@
$dropSql | docker compose exec -T db mysql --default-character-set=utf8mb4 -u"$DbUser" -p"$DbPass" $DbName
Write-Host "      OK"

# 3. 重新跑 db-init.ps1
Write-Host "[2/2] Re-initializing..." -ForegroundColor Yellow
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
& "$scriptDir\db-init.ps1"
