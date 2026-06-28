#!/usr/bin/env bash
# ============================================================
# DB 初始化 / 套用 schema 改動（Mac / Linux）
# ============================================================
# 用途：
#   1. 第一次 setup 後建表 + 灌 seed 資料（setup.sh 會自動 call）
#   2. 你或別人 push 了新表 schema 後，在 Mac 上 git pull 後跑這個套用
#      → schema.sql 用 CREATE TABLE IF NOT EXISTS，重複跑安全
#      → seed.sql 只在 users 表是空的時候跑（防重複資料）
#
# 不會做的事（要 destructive 改動請用 db-reset.sh）：
#   - 不會 DROP 任何表
#   - 不會更新已存在的表結構（ALTER TABLE）
#   - 不會清空已有資料
#
# 前置：db container 必須是 running 狀態
# 用法：./db-init.sh
# ============================================================

set -e

DB_USER="root"
DB_PASS="root"
DB_NAME="forum"
SCHEMA_FILE="forum_sb/src/main/resources/sql/schema.sql"
SEED_FILE="forum_sb/src/main/resources/sql/seed.sql"

echo "=========================================="
echo "  DB Init"
echo "=========================================="

# 1. 確認 db container 在跑
if ! docker compose ps db | grep -q "Up"; then
    echo "[!] db container 沒在跑，請先跑 'docker compose up -d'"
    exit 1
fi

# 2. 套用 schema（idempotent，安全重跑）
echo "[1/2] Applying schema..."
docker compose exec -T db mysql -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$SCHEMA_FILE"
echo "      OK"

# 3. 檢查 users 表是否為空
USER_COUNT=$(docker compose exec -T db mysql -u"$DB_USER" -p"$DB_PASS" -N -e \
    "SELECT COUNT(*) FROM $DB_NAME.users" 2>/dev/null | tr -d '\r' || echo 0)

if [ "$USER_COUNT" = "0" ]; then
    echo "[2/2] users 表是空的 → 灌 seed 資料..."
    docker compose exec -T db mysql -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$SEED_FILE"
    echo "      OK (3 users + 10 articles + comments + likes)"
else
    echo "[2/2] users 表已有 $USER_COUNT 筆資料 → 跳過 seed（防重複）"
    echo "      想重灌 seed 請用 ./db-reset.sh"
fi

echo ""
echo "Demo accounts (password: 'password'):"
echo "  alice@example.com"
echo "  bob@example.com"
echo "  charlie@example.com"
echo ""
