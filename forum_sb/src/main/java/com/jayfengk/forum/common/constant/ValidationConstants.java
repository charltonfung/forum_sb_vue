package com.jayfengk.forum.common.constant;

/**
 * Bean Validation 用的長度限制常數
 * ============================================================
 * 為什麼抽常數而不是直接寫數字？
 *   1. 同一個值散落在多個 DTO / message / DB schema，改動容易漏
 *   2. 給 code reviewer 看一眼就知道商業規則（255 是 email 長度上限）
 *   3. 之後要做 i18n / 動態 messages 時，這層可以平移
 *
 * 為什麼用 public static final int？
 *   @Size(max = X) 註解的 X 必須是「編譯期常數」（compile-time constant），
 *   只有 primitive + String 的 public static final 符合。
 *
 * 為什麼 final class + private constructor？
 *   utility class 不該被實例化、不該被繼承 — 避免有人寫 `new ValidationConstants()`。
 */
public final class ValidationConstants {

    private ValidationConstants() {
        // 防止實例化
    }

    // ============================================================
    // 文章
    // ============================================================
    /** 文章標題最大長度（對齊 DB articles.title VARCHAR(255)） */
    public static final int ARTICLE_TITLE_MAX = 255;

    /** 文章內文最小長度 */
    public static final int ARTICLE_CONTENT_MIN = 10;

    // ============================================================
    // 留言
    // ============================================================
    /** 單則留言最大長度 */
    public static final int COMMENT_CONTENT_MAX = 2000;

    // ============================================================
    // 使用者
    // ============================================================
    /** 顯示名稱最大長度（對齊 DB users.name VARCHAR(255)） */
    public static final int USER_NAME_MAX = 255;

    /** Email 最大長度（對齊 DB users.email VARCHAR(255)） */
    public static final int USER_EMAIL_MAX = 255;

    /** 密碼最小長度 */
    public static final int PASSWORD_MIN = 8;

    /** 密碼最大長度（防超長 input 攻擊 BCrypt） */
    public static final int PASSWORD_MAX = 255;
}
