#!/usr/bin/env bash
# ============================================================
# Forum（Spring Boot + Vue）一鍵啟動腳本（Mac / Linux）
# ============================================================
# 用法：
#   chmod +x setup.sh db-init.sh db-reset.sh
#   ./setup.sh
#
# 預期：機器只裝了 Docker Desktop，其他什麼都沒有
# 跑完：
#   http://localhost:5173  → Vue 前端
#   http://localhost:8090  → Spring Boot API
#   http://localhost:8026  → Mailpit Web UI
#
# 設計：
#   setup.sh = 啟動 container + 第一次 DB init（內部 call db-init.sh）
#   db-init.sh = 套用 schema + 灌 seed（之後 schema 改了單獨跑這個就好）
#   db-reset.sh = DROP 全部表重來
# ============================================================

set -e
set -o pipefail

echo ""
echo "=========================================="
echo "  Forum (Spring Boot + Vue) setup"
echo "=========================================="
echo ""

# 1. 確認 Docker 在跑
if ! docker info > /dev/null 2>&1; then
    echo "[!] Docker daemon 沒在跑，請先開啟 Docker Desktop 後重試"
    exit 1
fi

# 2. 啟動 container（第一次會 build + 下載 image，5-10 分鐘）
echo "[1/5] Building images and starting containers (first time ~5-10 min)..."
docker compose up -d --build

# 3. 等 MySQL 就緒
echo "[2/5] Waiting for MySQL to be ready..."
until docker compose exec -T db mysqladmin ping -h localhost -uroot -proot --silent > /dev/null 2>&1; do
    printf "."
    sleep 1
done
echo " ready"

# 4. 套用 DB schema + seed（用獨立腳本，跟 setup 解耦）
echo "[3/5] Initializing DB schema and seed data..."
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
bash "$SCRIPT_DIR/db-init.sh"

# 5. 等 backend Spring Boot 啟動完成
echo "[4/5] Waiting for Spring Boot to start (Maven downloads ~3-5 min first time)..."
echo "    Tip: 想看進度 → 另開一個 terminal 跑 'docker compose logs -f backend'"
until curl -fs http://localhost:8090/api/articles > /dev/null 2>&1; do
    printf "."
    sleep 3
done
echo " ready"

# 6. 等 frontend Vite 啟動完成
echo "[5/5] Waiting for Vite dev server to start (npm install ~1-2 min first time)..."
until curl -fs http://localhost:5173 > /dev/null 2>&1; do
    printf "."
    sleep 2
done
echo " ready"

echo ""
echo "=========================================="
echo "  Setup complete!"
echo "=========================================="
echo ""
echo "  Frontend  : http://localhost:5173"
echo "  Backend   : http://localhost:8090"
echo "  Mailpit   : http://localhost:8026"
echo "  MySQL     : localhost:3307 (root / root)"
echo ""
echo "  Demo accounts (password: 'password'):"
echo "    alice@example.com"
echo "    bob@example.com"
echo "    charlie@example.com"
echo ""
echo "  常用指令："
echo "    docker compose down       停止 container（DB 資料保留）"
echo "    ./db-init.sh              套用新 schema（git pull 後跑）"
echo "    ./db-reset.sh             砍掉所有表重建（destructive）"
echo ""
