-- ============================================================
-- TestHub 测试资产管理系统 数据库定义
-- 通用建表脚本（兼容 MySQL 8.0 / H2 MODE=MySQL）
-- 字符集：utf8mb4 / 引擎：InnoDB
-- ============================================================

-- 1. 系统用户表
CREATE TABLE sys_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(100) NOT NULL,
    real_name   VARCHAR(50)  NOT NULL,
    email       VARCHAR(100),
    phone       VARCHAR(20),
    status      TINYINT      NOT NULL DEFAULT 1,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    created_at  DATETIME,
    updated_at  DATETIME,
    CONSTRAINT uk_sys_user_username UNIQUE (username)
);

-- 2. 角色表
CREATE TABLE sys_role (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(30)  NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(200),
    deleted     TINYINT      NOT NULL DEFAULT 0,
    created_at  DATETIME,
    updated_at  DATETIME,
    CONSTRAINT uk_sys_role_code UNIQUE (code)
);

-- 3. 用户角色关联表
CREATE TABLE sys_user_role (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL
);

-- 4. 测试项目表
CREATE TABLE project (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(30)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    owner_id    BIGINT,
    owner_name  VARCHAR(50),
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    deleted     TINYINT      NOT NULL DEFAULT 0,
    created_at  DATETIME,
    updated_at  DATETIME,
    CONSTRAINT uk_project_code UNIQUE (code)
);

-- 5. 测试点表
CREATE TABLE test_point (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id  BIGINT       NOT NULL,
    module      VARCHAR(50)  NOT NULL,
    point_no    VARCHAR(40)  NOT NULL,
    title       VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    priority    VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM',
    risk_level  VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM',
    status      VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    source_type VARCHAR(20)  NOT NULL DEFAULT 'REQUIREMENT',
    creator_id  BIGINT,
    creator_name VARCHAR(50),
    deleted     TINYINT      NOT NULL DEFAULT 0,
    created_at  DATETIME,
    updated_at  DATETIME
);

-- 6. 测试用例表
CREATE TABLE test_case (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id    BIGINT       NOT NULL,
    test_point_id BIGINT,
    case_no       VARCHAR(40)  NOT NULL,
    title         VARCHAR(200) NOT NULL,
    precondition  VARCHAR(1000),
    steps         TEXT,
    expected_result TEXT,
    priority      VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM',
    case_type     VARCHAR(20)  NOT NULL DEFAULT 'FUNCTION',
    design_method VARCHAR(20),
    status        VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    auto_flag     TINYINT      NOT NULL DEFAULT 0,
    creator_id    BIGINT,
    creator_name  VARCHAR(50),
    deleted       TINYINT      NOT NULL DEFAULT 0,
    created_at    DATETIME,
    updated_at    DATETIME
);

-- 7. 测试计划表
CREATE TABLE test_plan (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id  BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    start_date  DATE,
    end_date    DATE,
    status      VARCHAR(20)  NOT NULL DEFAULT 'PLANNING',
    owner_id    BIGINT,
    owner_name  VARCHAR(50),
    deleted     TINYINT      NOT NULL DEFAULT 0,
    created_at  DATETIME,
    updated_at  DATETIME
);

-- 8. 计划用例关联表
CREATE TABLE plan_case (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id     BIGINT NOT NULL,
    case_id     BIGINT NOT NULL,
    assignee_id BIGINT,
    assignee_name VARCHAR(50),
    sort_no     INT NOT NULL DEFAULT 0,
    deleted     TINYINT NOT NULL DEFAULT 0,
    created_at  DATETIME,
    updated_at  DATETIME
);

-- 9. 测试执行记录表
CREATE TABLE test_execution (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id    BIGINT,
    plan_id       BIGINT NOT NULL,
    case_id       BIGINT NOT NULL,
    executor_id   BIGINT,
    executor_name VARCHAR(50),
    result        VARCHAR(20) NOT NULL,
    actual_result VARCHAR(1000),
    duration_ms   BIGINT,
    executed_at   DATETIME,
    remark        VARCHAR(500),
    deleted       TINYINT NOT NULL DEFAULT 0,
    created_at    DATETIME,
    updated_at    DATETIME
);

-- 10. 缺陷表
CREATE TABLE defect (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id   BIGINT      NOT NULL,
    case_id      BIGINT,
    execution_id BIGINT,
    defect_no    VARCHAR(40) NOT NULL,
    title        VARCHAR(200) NOT NULL,
    description  VARCHAR(2000),
    severity     VARCHAR(20) NOT NULL DEFAULT 'MAJOR',
    priority     VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    status       VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    assignee_id  BIGINT,
    assignee_name VARCHAR(50),
    reporter_id  BIGINT,
    reporter_name VARCHAR(50),
    resolved_at  DATETIME,
    deleted      TINYINT NOT NULL DEFAULT 0,
    created_at   DATETIME,
    updated_at   DATETIME
);

-- 11. 接口定义表
CREATE TABLE api_info (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id  BIGINT       NOT NULL,
    module      VARCHAR(50)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    method      VARCHAR(10)  NOT NULL DEFAULT 'GET',
    path        VARCHAR(200) NOT NULL,
    headers     VARCHAR(1000),
    params      VARCHAR(1000),
    body_type   VARCHAR(20)  NOT NULL DEFAULT 'JSON',
    body        VARCHAR(2000),
    description VARCHAR(500),
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    deleted     TINYINT      NOT NULL DEFAULT 0,
    created_at  DATETIME,
    updated_at  DATETIME
);

-- 12. 接口脚本表
CREATE TABLE api_script (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id      BIGINT       NOT NULL,
    api_id          BIGINT,
    name            VARCHAR(100) NOT NULL,
    script_type     VARCHAR(20)  NOT NULL DEFAULT 'POSITIVE',
    env_id          BIGINT,
    request_config  TEXT,
    assertions      TEXT,
    extractors      TEXT,
    variables       VARCHAR(1000),
    enabled         TINYINT      NOT NULL DEFAULT 1,
    last_run_status VARCHAR(20),
    last_run_at     DATETIME,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    created_at      DATETIME,
    updated_at      DATETIME
);

-- 13. 测试环境表
CREATE TABLE env_info (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT       NOT NULL,
    name       VARCHAR(50)  NOT NULL,
    base_url   VARCHAR(200) NOT NULL,
    headers    VARCHAR(1000),
    variables  VARCHAR(1000),
    enabled    TINYINT      NOT NULL DEFAULT 1,
    deleted    TINYINT      NOT NULL DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME
);

-- 14. 接口脚本执行日志表
CREATE TABLE api_run_log (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    script_id     BIGINT,
    project_id    BIGINT,
    env_id        BIGINT,
    script_name   VARCHAR(100),
    status        VARCHAR(20) NOT NULL,
    status_code   INT,
    duration_ms   BIGINT,
    request_url   VARCHAR(500),
    response_body TEXT,
    fail_message  VARCHAR(1000),
    executed_at   DATETIME,
    deleted       TINYINT NOT NULL DEFAULT 0,
    created_at    DATETIME,
    updated_at    DATETIME
);

-- 索引
CREATE INDEX idx_test_point_project ON test_point (project_id);
CREATE INDEX idx_test_case_project ON test_case (project_id);
CREATE INDEX idx_test_case_point ON test_case (test_point_id);
CREATE INDEX idx_plan_case_plan ON plan_case (plan_id);
CREATE INDEX idx_execution_plan ON test_execution (plan_id);
CREATE INDEX idx_execution_case ON test_execution (case_id);
CREATE INDEX idx_defect_project ON defect (project_id);
CREATE INDEX idx_api_info_project ON api_info (project_id);
CREATE INDEX idx_api_script_project ON api_script (project_id);
CREATE INDEX idx_run_log_script ON api_run_log (script_id);
CREATE INDEX idx_user_role_user ON sys_user_role (user_id);
