package com.jayfengk.forum.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;
import java.util.function.Function;

/**
 * 分頁回應結構
 * ============================================================
 * 把 MyBatis-Plus 的 IPage<T> 包成前端友善的格式：
 *   { items, total, page, pageSize, totalPages }
 */
@Data
public class PageResult<T> {
    private List<T> items;
    private long total;
    private long page;
    private long pageSize;
    private long totalPages;

    /** 從 MyBatis-Plus 的 IPage + 轉換函式生成 PageResult */
    public static <E, T> PageResult<T> from(IPage<E> page, Function<E, T> mapper) {
        PageResult<T> result = new PageResult<>();
        result.setItems(page.getRecords().stream().map(mapper).toList());
        result.setTotal(page.getTotal());
        result.setPage(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotalPages(page.getPages());
        return result;
    }
}
