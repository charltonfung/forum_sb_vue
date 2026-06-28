#!/usr/bin/env bash
# ============================================================
# DB 重置（DROP 所有表 + 重建 + 重灌 seed）（Mac / Linux）
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
# 用法：./db-reset.sh
# ============================================================

set -e

DB_USER="root"
DB_PASS="root"
DB_NAME="forum"

echo "=========================================="
echo "  DB Reset (WILL DROP ALL DATA!)"
echo "=========================================="
echo ""
read -p "確定要砍掉 $DB_NAME 所有表並重建嗎？(y/N) " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "已取消"
    exit 0
fi

# 1. 確認 db container 在跑
if ! docker compose ps db | grep -q "Up"; then
    echo "[!] db container 沒在跑，請先跑 'docker compose up -d'"
    exit 1
fi

# 2. DROP 全部表（用 SET FOREIGN_KEY_CHECKS=0 暫時關掉 FK 限制，
#    不然有 FK 關係的表要按順序砍，麻煩）
echo "[1/2] Dropping all tables..."
docker compose exec -T db mysql --default-character-set=utf8mb4 -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" <<'SQL'
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS comment_likes;
DROP TABLE IF EXISTS article_likes;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS articles;
DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;
SQL
echo "      OK"

# 3. 重新跑 db-init.sh
echo "[2/2] Re-initializing..."
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
bash "$SCRIPT_DIR/db-init.sh"
