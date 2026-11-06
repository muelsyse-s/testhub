package com.testhub.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 通用分页查询参数。 */
@Data
@Schema(description = "分页查询参数")
public class PageQuery {

    @Schema(description = "页码，从 1 开始", example = "1")
    private long page = 1;

    @Schema(description = "每页条数", example = "10")
    private long size = 10;

    @Schema(description = "关键字模糊检索")
    private String keyword;

    public long getPage() {
        return page < 1 ? 1 : page;
    }

    public long getSize() {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 200);
    }
}
