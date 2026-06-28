# Forum

簡單的多人文章討論版 — 使用者可發文、留言、互按讚。

```
forum/
├── forum_sb/   # Spring Boot 後端（port 8090）
├── forum_vue/  # Vue 3 + Vite 前端（port 5173）
└── README.md   ← 你正在看
```

---

## 技術棧

| 層 | 工具 |
|---|---|
| 後端 | Spring Boot 3.5.0 / Java 21 |
| ORM | MyBatis-Plus 3.5.10 |
| 驗證 | Spring Security + JJWT（HS256） |
| 信件 | Spring Mail（SMTP） |
| 資料庫 | MySQL 8 |
| 前端 | Vue 3 + Vite + Vue Router + Pinia |
| UI | Element Plus |
| HTTP | Axios |
| 容器化 | Docker Compose（MySQL / Mailpit / Backend / Frontend） |

**架構**：單模組 Spring Boot 後端 + Vue 3 SPA 前端，JWT 無狀態驗證。

---

## 功能

- 註冊 / 登入 / 登出（JWT）
- 文章 CRUD（軟刪除、分頁，每頁 10 篇）
- 文章下留言（軟刪除、可刪自己的留言）
- 文章 / 留言點讚（防重複、可取消）
- 個人中心（查看自己的文章）
- 忘記密碼 Email 流程（dev 用 Mailpit 攔截）

### API 端點

| Method | Path | 說明 |
|---|---|---|
| GET | `/api/articles` | 文章列表（分頁） |
| GET | `/api/articles/:id` | 單篇文章 |
| POST | `/api/articles` | 建立文章 |
| PUT | `/api/articles/:id` | 更新文章 |
| DELETE | `/api/articles/:id` | 軟刪除文章 |
| POST | `/api/auth/register` | 註冊 |
| POST | `/api/auth/login` | 登入 |
| POST | `/api/auth/forgot-password` | 寄密碼重設信 |
| POST | `/api/auth/reset-password` | 重設密碼 |
| GET | `/api/users/me` | 取得當前使用者 |
| GET/POST | `/api/articles/:id/comments` | 留言列表 / 新增 |
| DELETE | `/api/comments/:id` | 刪除留言 |
| POST/DELETE | `/api/articles/:id/like` | 文章讚 / 取消 |
| POST/DELETE | `/api/comments/:id/like` | 留言讚 / 取消 |

---

## 一鍵啟動（推薦）

只要本機有 **Docker Desktop**，跑一個 script 就會把所有東西準備好：MySQL + Mailpit + Spring Boot 後端 + Vue 前端 + 灌好 demo 資料。

### Mac / Linux

```bash
chmod +x setup.sh db-init.sh db-reset.sh
./setup.sh
```

### Windows

```powershell
.\setup.ps1
```

第一次跑約 **5-10 分鐘**（要 build image、下載 Maven 套件、裝 npm 套件）。之後 `docker compose up -d` 秒回。

完成後：

| URL | 是什麼 |
|---|---|
| http://localhost:5173 | Vue 前端 |
| http://localhost:8090 | Spring Boot API |
| http://localhost:8026 | Mailpit Web UI（看密碼重設信） |
| localhost:3308 | MySQL（root / root，host 端 GUI 工具用） |

### Demo 帳號

| Email | 密碼 |
|---|---|
| alice@example.com | password |
| bob@example.com | password |
| charlie@example.com | password |

預設帶 10 篇文章 + 留言 + 點讚資料。

### 常用 Docker 指令

```bash
docker compose up -d              # 啟動（背景）
docker compose down               # 停止 + 移除 container（DB 資料保留）
docker compose down -v            # 連 DB volume 一起砍（資料清空）
docker compose logs -f backend    # 看 Spring Boot 即時 log
docker compose logs -f frontend   # 看 Vite 即時 log
docker compose exec backend bash  # 進到 backend container 內部
docker compose exec db mysql -uroot -proot forum    # 進 MySQL CLI
```

---

## DB 管理（schema / seed 跟 container 解耦）

容器生命週期跟 DB schema 是**分開的兩件事**：

| 指令 | 做什麼 | 何時用 |
|---|---|---|
| `docker compose up -d` | 啟動 container | 平常開機、down 之後 |
| `./setup.sh` / `.\setup.ps1` | 啟動 container + 第一次 DB init | clone 後第一次 |
| `./db-init.sh` / `.\db-init.ps1` | 套用 schema（CREATE TABLE IF NOT EXISTS）+ 若 users 表空才灌 seed | **git pull 拉到新表後** |
| `./db-reset.sh` / `.\db-reset.ps1` | DROP 全部表 → 重 init | schema 改了欄位（idempotent 沒辦法）、想還原乾淨 |
| `docker compose down -v` | 連 volume 也砍 | 想連 MySQL 系統檔都重建（極少需要） |

### 典型情境

**情境 A：你在 Windows 加了新 table，push 上去；Mac 想同步**
```bash
git pull             # 拉到新的 schema.sql
./db-init.sh         # 套用，舊資料保留、新表加進去
```

**情境 B：你改了某個欄位的型別 / 加 column**
```bash
git pull
./db-reset.sh        # 必要，CREATE TABLE IF NOT EXISTS 沒辦法套 ALTER
```

**情境 C：demo 過程亂改了一通想還原**
```bash
./db-reset.sh        # 砍表重灌 seed，10 秒回到初始狀態
```

> SQL 檔結構：
> - `forum_sb/src/main/resources/sql/schema.sql` — 純 CREATE TABLE IF NOT EXISTS
> - `forum_sb/src/main/resources/sql/seed.sql` — 純 INSERT（3 users + 10 articles + 留言 + 讚）

---

## 啟動方式 B：傳統（本機裝 JDK / Maven / Node / MySQL）

如果不想用 Docker，需要本機已裝 JDK 21+、Maven、Node 18+、MySQL 8。

### 1. 建立資料庫 + 套用 schema + 灌 seed

```bash
# 建空 DB
mysql -uroot -p -e "CREATE DATABASE forum DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 套用 schema（建表）
mysql -uroot -p forum < forum_sb/src/main/resources/sql/schema.sql

# 灌 seed 資料（3 users + 10 articles + 留言 + 讚）
mysql -uroot -p forum < forum_sb/src/main/resources/sql/seed.sql
```

### 2. 啟動後端

```bash
cd forum_sb
mvn spring-boot:run
```

如果 MySQL 的 root 密碼不是預設的，可設環境變數：

```bash
DB_PASSWORD=你的密碼 mvn spring-boot:run
```

### 3. 啟動前端

```bash
cd forum_vue
npm install
npm run dev
```

打開 `http://localhost:5173`。

### 4.（可選）啟動 Mailpit 看密碼重設信

```bash
mailpit
# 或 docker run -d -p 1025:1025 -p 8025:8025 axllent/mailpit
```

Web UI 在 http://localhost:8025。

---

## 目錄速覽

### 後端（`forum_sb/`）

```
forum_sb/src/main/
├── java/com/jayfengk/forum/
│   ├── ForumApplication.java          ← 啟動類
│   ├── config/                        ← Security / CORS / MyBatis-Plus
│   ├── security/                      ← JWT 三件套（Provider / Filter / UserDetailsService）
│   ├── common/api/                    ← Result / ResultCode 統一回應
│   ├── common/exception/              ← ApiException + GlobalExceptionHandler
│   ├── entity/                        ← User / Article / Comment / ArticleLike / CommentLike
│   ├── mapper/                        ← MyBatis-Plus BaseMapper
│   ├── dto/                           ← Request / Response DTO
│   ├── service/                       ← Service 介面
│   ├── service/impl/                  ← Service 實作
│   └── controller/                    ← Auth / Article / Comment / Like / User Controller
└── resources/
    ├── application.yml                ← 設定檔
    └── sql/init.sql                   ← 資料庫 schema + seed 資料
```

### 前端（`forum_vue/`）

```
forum_vue/src/
├── main.js               ← 進入點
├── App.vue               ← 根元件
├── router/index.js       ← 路由 + 守衛
├── stores/auth.js        ← Pinia auth store（token + user）
├── api/                  ← axios 實例 + 各 API 模組
├── layouts/MainLayout.vue ← 主畫面 layout（header + content）
├── components/           ← AppHeader / LikeButton / CommentSection
└── views/                ← 9 個頁面（Login / Register / Forgot / Reset
                              / ArticleList / Show / Create / Edit / Profile）
```

---

## 常用指令

### 後端

```bash
mvn spring-boot:run         # 啟動 dev server
mvn clean package           # 打 fat jar，輸出 target/forum-sb-0.0.1-SNAPSHOT.jar
java -jar target/forum-sb-0.0.1-SNAPSHOT.jar
mvn test                    # 跑測試
```

### 前端

```bash
npm run dev                 # 啟動 dev server（port 5173）
npm run build               # 打包到 dist/
npm run preview             # 預覽打包結果
```

---

## 之後可能加什麼

- [ ] Profile 修改（name / email / password）
- [ ] Email 驗證流程
- [ ] 留言巢狀回覆
- [ ] 標籤 / 分類
- [ ] 全文搜尋（MySQL FULLTEXT 或 Elasticsearch）
- [ ] Redis 快取列表頁
- [ ] JWT 黑名單（server-side logout）
- [ ] 2FA（TOTP）
