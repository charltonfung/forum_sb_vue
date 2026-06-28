package com.jayfengk.forum.common.constant;

/**
 * 分頁相關常數
 * ============================================================
 * 為什麼用 String 不用 int？
 *   @RequestParam(defaultValue = "...") 的 defaultValue 屬性只吃 String，
 *   即使最後 binding 到 long / int 也一樣。Spring 內部會做轉型。
 */
public final class PaginationConstants {

    private PaginationConstants() {
        // 防止實例化
    }

    /** 預設頁碼（從 1 開始，符合人類直覺；MyBatis-Plus Page 也是 1-based） */
    public static final String DEFAULT_PAGE = "1";

    /** 預設每頁筆數 */
    public static final String DEFAULT_PAGE_SIZE = "10";

    /**
     * 最大每頁筆數
     * 防 client 傳 ?pageSize=999999 把 DB 撈爆 / response 撐爆記憶體
     * 之後在 controller 用 Math.min(req, MAX_PAGE_SIZE) 卡住
     */
    public static final int MAX_PAGE_SIZE = 100;
}
