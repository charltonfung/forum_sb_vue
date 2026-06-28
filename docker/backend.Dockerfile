# ============================================================
# Spring Boot 後端開發用 image（dev mode）
# ============================================================
# 基底：Maven 官方 image，內建 JDK 21 + Maven
#
# 設計重點：
#   - source code 透過 volume mount 進來（host 編輯 → container 看到）
#   - Maven 套件 cache 用 named volume，避免每次重 build container 都重抓
#   - 用 mvn spring-boot:run，含 Spring DevTools 熱重載
#
# 為什麼用 maven image 而不是 eclipse-temurin?
#   maven:3.9-eclipse-temurin-21 已經包含 JDK 21 + Maven + git。
#   單純 eclipse-temurin 沒 Maven，還要自己裝。
# ============================================================
FROM maven:3.9-eclipse-temurin-21

WORKDIR /app

# 預設指令：跑 Spring Boot dev server
# 因為 source code 是 volume mount，container 啟動後 mvn 會自動：
#   1. 看 pom.xml 解析依賴
#   2. 從 Maven cache（mount 進來的）抓套件
#   3. 編譯 + 啟動 application
CMD ["mvn", "spring-boot:run"]
