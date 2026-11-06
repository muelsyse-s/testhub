package com.testhub.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/** 分页工具：把通用查询参数转换为 MyBatis-Plus 的分页对象。 */
public final class PageUtils {

    private PageUtils() {
    }

    public static <T> Page<T> of(PageQuery query) {
        return new Page<>(query.getPage(), query.getSize());
    }
}
