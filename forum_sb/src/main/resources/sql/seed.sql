-- ============================================================
-- Forum demo seed 資料
-- ============================================================
-- 3 個 demo 帳號 + 10 篇文章 + 留言 + 讚
-- 全部帳號密碼都是「password」（BCrypt strength=10 hash）
--
-- 假設：schema.sql 已經跑過（表已建好）+ 表是空的
-- 重複跑會撞 PRIMARY KEY / UNIQUE constraint，是預期行為（防誤灌重複資料）。
-- 想重灌請用 db-reset.sh（會先 DROP 全部表）。
-- ============================================================

-- 3 個使用者（email_verified_at 設好，避免未來加 verified middleware 卡住）
INSERT INTO users (name, email, password, email_verified_at) VALUES
('Alice',   'alice@example.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NOW()),
('Bob',     'bob@example.com',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NOW()),
('Charlie', 'charlie@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NOW());

-- 10 篇文章，分散 3 位作者，時間從 10 天前到現在
INSERT INTO articles (title, content, state, user_id, created_at) VALUES
('歡迎來到 Forum',                  '這是一個自由暢談的小社群。\n\n你可以發表文章、留言互動、按讚支持喜歡的內容。希望大家在這裡找到有趣的話題與朋友！', 'published', 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
('今天學了 Docker compose',         '整個下午都在搞 Docker，一開始覺得超複雜，後來發現只要把 service 用 docker-compose.yml 寫清楚，docker compose up 就一鍵起來，超方便！\n\n之後再也不用裝一堆東西到本機了。', 'published', 1, DATE_SUB(NOW(), INTERVAL 9 DAY)),
('推薦一家好吃的拉麵店',             '前幾天在中山站附近吃了一家拉麵，湯頭超濃郁，麵條 Q 彈，叉燒入口即化。\n價格也不貴，一碗 280 元，CP 值超高，大家有機會可以去試試！', 'published', 2, DATE_SUB(NOW(), INTERVAL 8 DAY)),
('Vim 為什麼這麼難學',              '用了一個禮拜 Vim 還是沒辦法上手，常常按錯鍵直接弄壞東西。\n\n有沒有大神可以推薦一下入門教學？光是要記 hjkl 就花我半天，更別提那些 :wq、:q!、yy、dd...', 'published', 2, DATE_SUB(NOW(), INTERVAL 7 DAY)),
('週末爬了陽明山',                  '天氣不錯，跟朋友去陽明山走走，路線是擎天崗 → 冷水坑。\n\n芒草季很美，記得帶水跟防曬，山上溫差比想像中大。回程吃了山下的金山鴨肉，完美結束一天。', 'published', 3, DATE_SUB(NOW(), INTERVAL 6 DAY)),
('為什麼工程師都喜歡用黑色背景',     '感覺所有 IDE / terminal 都預設黑底白字，到底為什麼？\n\n後來查了一下，主要是：\n1. 長時間看眼睛比較不累\n2. OLED 螢幕省電\n3. 看起來比較專業 lol', 'published', 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
('第一次嘗試 Spring Boot + Vue',   '之前都只寫純後端 + 模板引擎，最近想試試前後端分離。\n\nVue 3 + Pinia + Vue Router 配 REST API 真的好寫，前後端各自獨立 deploy 也方便很多。學習曲線比想像中平緩！', 'published', 3, DATE_SUB(NOW(), INTERVAL 4 DAY)),
('咖啡因戒斷的痛苦',                '決定戒咖啡一個禮拜，結果頭痛了整整 3 天，超級無法工作。\n\n看來真的對咖啡因有依賴...有人有成功戒掉的經驗嗎？是直接戒還是慢慢減量？', 'published', 2, DATE_SUB(NOW(), INTERVAL 3 DAY)),
('推薦的開發工具清單',              '整理一下我目前在用的工具：\n\n- 編輯器：VS Code\n- Terminal：Windows Terminal + PowerShell\n- DB 工具：DBeaver（免費、跨平台）\n- API 測試：Postman / Thunder Client\n- 容器：Docker Desktop\n\n大家用什麼？', 'published', 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('貓主子又生氣了',                  '只是稍微離開沙發 5 分鐘去廚房，回來就被她翻肚噴氣 + 甩尾，到底是哪裡得罪她了？\n\n養貓的各位，你們家主子也這麼難伺候嗎？', 'published', 3, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 留言（分散在各篇文章下，每篇 1-3 則）
INSERT INTO comments (article_id, user_id, content) VALUES
(1, 2, '推一個！'),
(1, 3, '感謝分享，很實用'),
(2, 2, 'Docker 真的香，回不去了'),
(2, 3, '有沒有推薦的 Docker 入門教學？'),
(3, 1, '哈哈中山站那家我也吃過，叉燒真的不錯'),
(3, 3, '收藏起來'),
(4, 1, 'Vim 的學習曲線是垂直的，撐過去就會回不去 lol'),
(4, 3, '推薦 vimtutor，內建的教學最快'),
(5, 1, '陽明山這季節真的很美'),
(5, 2, '金山鴨肉 +1'),
(6, 2, '我也是黑底愛好者，看久白底真的眼睛痛'),
(7, 1, '同感！前後端分離後 deploy 流程乾淨多了'),
(7, 2, 'Pinia 比 Vuex 好用太多'),
(8, 1, '我成功戒掉了，建議慢慢減量比較不會頭痛'),
(8, 3, '我已經放棄戒了，繼續喝 ☕'),
(9, 2, 'DBeaver +1 真的好用'),
(9, 3, '我都用 TablePlus，付費但介面好看'),
(10, 1, '哈哈貓就是這樣，主子的心思難猜'),
(10, 2, '+1 我家的也常常莫名其妙翻肚');

-- 文章讚
INSERT INTO article_likes (article_id, user_id) VALUES
(1, 2), (1, 3),
(2, 2), (2, 3),
(3, 1),
(4, 1), (4, 3),
(5, 1), (5, 2),
(6, 2),
(7, 1), (7, 2),
(8, 1),
(9, 2), (9, 3),
(10, 1), (10, 2);

-- 留言讚
INSERT INTO comment_likes (comment_id, user_id) VALUES
(1, 1), (1, 3),
(3, 1),
(5, 2),
(7, 2), (7, 3),
(9, 1), (9, 2),
(11, 1),
(12, 2),
(14, 1),
(16, 3),
(18, 3);
