package com.testhub.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 统一响应结果封装。 */
@Data
@Schema(description = "统一响应结果")
public class R<T> {

    @Schema(description = "业务状态码，0 表示成功")
    private int code;

    @Schema(description = "提示信息")
    private String msg;

    @Schema(description = "响应数据")
    private T data;

    public R() {
    }

    public R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return new R<>(0, "ok", null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(0, "ok", data);
    }

    public static <T> R<T> ok(T data, String msg) {
        return new R<>(0, msg, data);
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(500, msg, null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null);
    }
}
