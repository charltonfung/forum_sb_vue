# Forum

簡單的多人文章討論版 — 使用者可發文、留言、互按讚。

```
forum/
├── forum_sb/   # Spring Boot 後端（port 8090）
└── forum_vue/  # Vue 3 + Vite 前端（port 5173）
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

**架構**：單模組 Spring Boot 後端 + Vue 3 SPA 前端，JWT 無狀態驗證。

---

## 功能

- 註冊 / 登入 / 登出（JWT）
- 文章 CRUD（軟刪除、分頁，每頁 10 篇）
- 首頁標題模糊搜尋（debounce 300ms 邊打邊查）
- 「我的文章」入口（nav bar 用戶下拉，列出自己發過的文）
- 文章下留言（軟刪除、可刪自己的留言）
- 文章 / 留言點讚（防重複、可取消）
- 個人資料管理（改名 / 改密碼 / 改 email）
  - 改密碼要驗舊密碼、改 email 寄驗證信到新 email、改完後舊 JWT 自動失效
- 忘記密碼 Email 流程（dev 用 Mailpit 攔截）

### API 端點

| Method | Path | 說明 |
|---|---|---|
| GET | `/api/articles?q=&userId=` | 文章列表（分頁，`q` 模糊搜尋標題、`userId` 只看某使用者的文章） |
| GET | `/api/articles/:id` | 單篇文章 |
| POST | `/api/articles` | 建立文章 |
| PUT | `/api/articles/:id` | 更新文章 |
| DELETE | `/api/articles/:id` | 軟刪除文章 |
| POST | `/api/auth/register` | 註冊 |
| POST | `/api/auth/login` | 登入 |
| POST | `/api/auth/forgot-password` | 寄密碼重設信 |
| POST | `/api/auth/reset-password` | 重設密碼 |
| GET | `/api/users/me` | 取得當前使用者 |
| PUT | `/api/users/me` | 更新個人資料（名稱） |
| POST | `/api/users/me/password` | 變更密碼（驗舊密碼，舊 JWT 失效） |
| POST | `/api/users/me/email-change` | 申請變更 email（寄驗證信到新 email） |
| POST | `/api/users/verify-email-change` | 驗證 email 變更（信件連結，免登入） |
| GET/POST | `/api/articles/:id/comments` | 留言列表 / 新增 |
| DELETE | `/api/comments/:id` | 刪除留言 |
| POST/DELETE | `/api/articles/:id/like` | 文章讚 / 取消 |
| POST/DELETE | `/api/comments/:id/like` | 留言讚 / 取消 |

---

## 一鍵啟動

只要本機有 **Docker Desktop**，跑一個 script 就會把所有東西準備好：MySQL + Mailpit + Spring Boot 後端 + Vue 前端 + 灌好 demo 資料。

```bash
./setup.sh
```

Windows 用 Git Bash 跑同一行。

第一次跑約 **5-10 分鐘**（要 build image、下載 Maven 套件、裝 npm 套件）。

---

## 常用 Docker 指令

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

## 資料庫操作

容器生命週期跟 DB schema 是分開的兩件事 — `docker compose` 管 container，schema / seed 用以下兩支 script 管：

### DB init

```bash
./db-init.sh
```

套用 `schema.sql`（CREATE TABLE IF NOT EXISTS，重複跑安全）+ 若 `users` 表為空才灌 `seed.sql`。

### DB reset

```bash
./db-reset.sh
```

DROP 全部表 → 重新跑 db-init。

> SQL 檔在 [`forum_sb/src/main/resources/sql/`](forum_sb/src/main/resources/sql/)：`schema.sql`（建表）+ `seed.sql`（demo 資料）。

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
    └── sql/
        ├── schema.sql                 ← 純 CREATE TABLE IF NOT EXISTS（idempotent）
        ├── seed.sql                   ← 純 INSERT（3 users + 10 articles + 留言 + 讚）
        └── migrations/                ← V1__/V2__/V3__ 補丁，給已建表的 DB 用
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
└── views/                ← 10 個頁面（Login / Register / Forgot / Reset / VerifyEmailChange
                              / ArticleList / Show / Create / Edit / Profile）
```

---