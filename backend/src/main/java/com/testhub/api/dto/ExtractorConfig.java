package com.testhub.api.dto;

import lombok.Data;

/** 变量提取配置，用于接口之间的依赖传参。 */
@Data
public class ExtractorConfig {

    private String name;
    /** 目前支持 JSON_PATH。 */
    private String type;
    private String expression;
    /** GLOBAL 全局共享。 */
    private String scope;
}
