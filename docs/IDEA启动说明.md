# 在 IDEA 中运行 TestHub（给老师 demo 用）

> 目标：**两次点击**把前后端跑起来，浏览器打开 http://127.0.0.1:5173 即可演示。

---

## 一、第一次打开项目（只做一次）

1. **File → Open**，选择目录 `D:\PROJECT\2026.11课设\testhub`（选**整个 testhub**，不要只选 backend）。
2. IDEA 会提示发现 Maven 项目：右侧会出现 `pom.xml`，点 **Load / 加载 Maven 项目**。
   若没有提示，用 **Maven 面板 → + → 选择 `backend\pom.xml`**。
3. **File → Project Structure → Project**：
   - SDK 选 **21**（没有就 Add SDK → 选 `D:\Program Files\Java\jdk-21.0.11`）
   - Language level 选 **21**
4. **Settings → Build → Compiler → Annotation Processors**：勾选 **Enable annotation processing**
   （项目用了 Lombok，不勾会报 `getXxx() 找不到符号`）。新版 IDEA 一般已内置 Lombok 插件。
5. **Settings → Editor → File Encodings**：三处都设 **UTF-8**（含 "Project Encoding" 与 "Properties Files"），
   否则中文注释和日志会乱码。

---

## 二、启动后端（Spring Boot）

> **社区版（Community）注意**：IDEA 社区版不支持 Spring Boot 运行配置，但完全不影响使用——
> 直接打开 `TestHubApplication.java`，点 `main` 方法左侧绿色三角运行即可（它会以普通 Java 应用启动），
> 效果一样。下面的方式 A 需要旗舰版（Ultimate）。

**方式 A（旗舰版，一键）**：右上角运行配置下拉框里选 **`TestHub 启动演示`**，点绿色三角。
它会同时拉起后端与前端（`.run` 目录下已内置三个运行配置）。

**方式 B（手动，任何版本）**：
1. 打开 `backend/src/main/java/com/testhub/TestHubApplication.java`
2. 点类名或 `main` 方法左侧绿色三角 → **Run 'TestHubApplication'**
3. 控制台出现 `Started TestHubApplication in x.x seconds` 即启动成功

后端默认使用 **H2 内存库**（`application.yml` 里 `spring.profiles.active: h2`），
启动时自动建表并注入演示数据，**不需要装 MySQL**。

- 服务地址：http://127.0.0.1:8080
- 接口文档：http://127.0.0.1:8080/swagger-ui.html
- H2 控制台：http://127.0.0.1:8080/h2-console（JDBC URL `jdbc:h2:mem:testhub`，用户 `sa`，密码空）

---

## 三、启动前端（Vue 3 + Vite）

**方式 A**：运行配置里选 **`TestHub 前端`** 直接运行。

**方式 B**：IDEA 底部 **Terminal**（`Alt+F12`）里执行：

```powershell
cd frontend
npm install     # 第一次需要，之后可跳过（本机已装好）
npm run dev
```

看到 `VITE ready` 与 `Local: http://127.0.0.1:5173/` 即成功。
Vite 已配置把 `/api` 代理到 `127.0.0.1:8080`，前端不需要额外配置跨域。

---

## 四、打开浏览器演示

访问 **http://127.0.0.1:5173**

| 账号 | 密码 | 角色 |
|---|---|---|
| **wtb** | 123456 | 系统管理员（权限最全，推荐演示用） |
| zhangsan | 123456 | 测试经理 |
| lisi | 123456 | 测试工程师 |
| wangwu | 123456 | 开发工程师 |

---

## 五、给老师演示的 5 分钟路线

1. **登录** → 进入「质量总览」：四个指标卡片 + 14 天执行趋势 + 缺陷分布环形图
2. **测试点管理**：展示"需求 → 测试点"的拆解，以及每个测试点覆盖的用例数
3. **测试用例**：点「新建用例」演示分步录入，再点「复制」演示用例派生
4. **测试计划 → 计划详情**：进度条、执行人、最近执行结果；点「执行」登记一条 PASS
5. **缺陷管理**：演示状态流转（待处理 → 修复中 → 已解决）
6. **接口脚本 → 执行**："平台自身登录-正向用例" 点执行，右侧抽屉展示 3 条断言全过、
   耗时、提取到的 token —— 这是系统的技术亮点
7. **统计报表**：模块覆盖柱状图、缺陷状态环形图、成员工作量

> 亮点话术：测试点作为需求与用例之间的中间层实现"可追溯"；接口脚本零代码实现
> "登录取 token → 下单取单号"的依赖串联。

---

## 六、常见问题

| 现象 | 原因与处理 |
|---|---|
| `Port 8080 was already in use` | 有旧进程占用。PowerShell 执行：`Get-NetTCPConnection -LocalPort 8080 \| ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }` |
| `Port 5173 ... in use` | Vite 会自动换到 5174，注意看终端提示的端口 |
| 页面能开但接口 401 / 报错 | 后端没起或起在别的端口；看后端控制台是否有 `Started TestHubApplication` |
| 编译报 `找不到符号 getXxx()` | 没开 Lombok 注解处理，见上文第一步第 4 条 |
| 中文乱码 | File Encodings 未设 UTF-8 |
| Maven 依赖下载很慢 | 已在 `~/.m2` 缓存过，一般不用重下；换网络时可配置阿里云镜像 |

---

## 七、跑测试（可选，加分项）

IDEA 里右键 `backend/src/test/java` → **Run 'All Tests'**，或在 Terminal：

```powershell
cd backend
mvn test
```

共 7 个测试：变量替换 4 个（含 `$`、反斜杠、重复占位符边界）+ 认证鉴权 3 个（登录成功/密码错误/未带令牌 401）。
