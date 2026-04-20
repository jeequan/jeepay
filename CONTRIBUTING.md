# 贡献指南

感谢你考虑为 Jeepay 贡献代码 / 文档 / 测试。本指南只是一份让协作成本变低的约定，不是规矩；遇到特殊场景，优先沟通。

## 一、分支模型

| 分支 | 定位 | 谁能直推 | 生命周期 |
|---|---|---|---|
| `master` | 稳定分支，每个 commit 都对应或紧跟一个 release tag | 任何人都不能直推，只接 merge | 永久 |
| `dev` | 日常开发集成分支；"比 master 超前半步" | 任何人都不能直推，只接 merge | 永久 |
| `feature/*` | 单个新功能 / 重构（示例 `feature/split-payment`） | 开发者自己 | 合入 `dev` 后删 |
| `fix/*` | 非紧急 bug 修复（示例 `fix/refund-edge-case`） | 开发者自己 | 合入 `dev` 后删 |
| `hotfix/*` | **紧急**生产修复（示例 `hotfix/mysql-pwd`） | 开发者自己 | 双合到 `master` + `dev` 后删 |
| `release/*` | 发版冲刺分支（示例 `release/V3.3.0`），只做版本号 / changelog / 回归 | 开发者自己 | 打完 tag 后删 |

## 二、开发流程

### 做一个普通功能

```bash
git checkout dev && git pull
git checkout -b feature/your-topic
# ... 开发 + 本地自测 ...
git commit -m "feat(scope): 简短描述"
git push origin feature/your-topic
# 到 GitHub / Gitee 建 PR: feature/your-topic → dev
```

### 非紧急 bug 修复

与功能分支同构，换用 `fix/*` 前缀，目标分支仍然是 `dev`。

### 紧急生产修复（已发布版本上的严重问题）

```bash
git checkout master && git pull
git checkout -b hotfix/your-issue
# ... 修 + 测 ...
git commit -m "fix(scope): 简短描述"
git push origin hotfix/your-issue
# PR 合到 master；打 PATCH tag（例 V3.2.7 → V3.2.8）
# 合完 master 后，再把 master 回合到 dev，保持两边一致
```

### 发版（MINOR / MAJOR）

```bash
git checkout dev && git pull
git checkout -b release/V3.3.0
# 只改：pom.xml 的 isys.version、version.md、upgrade.md、
#       docs/install/install.sh 默认 jeepayRef、相关文档
# 发版前做回归测试（跑 docs/install/test_*.sh + 可用的话在测试机走端到端）
git commit -m "chore: 发布 V3.3.0 版本元数据"
git push origin release/V3.3.0
# PR 合到 master；打 tag V3.3.0；同步合回 dev
```

## 三、Commit 规范

```
<type>(<scope>): <简短描述>
```

- `type`：`feat` / `fix` / `docs` / `chore` / `test` / `refactor` / `perf` / `style`
- `scope`（可选）：改动的模块，例 `install` / `compose` / `payment` / `mch` / `proxy`
- `简短描述`：中文优先，祈使语气

示例：
- `fix(install): 修复 MySQL/Redis hostname 与密码错配`
- `feat(mch): 新增商户角色管理`
- `docs(deploy): 补充 HTTPS 反代拓扑示例`
- `chore: 发布 V3.3.0`

commit body（可选）写"为什么这么改 / 注意事项"，不要写"我改了什么"，那是 diff 的事。

## 四、PR 规范

1. **PR 目标分支**：功能 / 非紧急 bug → `dev`；紧急生产修复 → `master`。**不要**直接向 `master` 发新功能 PR。
2. **PR 标题**沿用 commit 前缀风格，例 `feat(mch): 新增商户角色管理`。
3. **PR body** 建议包含：
   - 做了什么 / 为什么
   - 如何测试（步骤 + 结果）
   - 是否破坏兼容（破坏请在标题加 `[BREAKING]`）
4. CI 必须全绿才会合并。

## 五、Tag 与版本号

- 语义化版本 `MAJOR.MINOR.PATCH`，tag 名形如 `V3.2.7`。
- **PATCH**（例 `V3.2.7 → V3.2.8`）：仅 bug / 文档 / 安装工具调整，业务镜像不变。
- **MINOR**（例 `V3.2.x → V3.3.0`）：新功能 / API 扩展，业务镜像需要重打并推送 SWR / Docker Hub。
- **MAJOR**（例 `V3.x → V4.0.0`）：不兼容变更（DB schema 破坏 / 接口协议破坏）。
- 发版时同步更新：
  - `pom.xml` 的 `isys.version`
  - `version.md`
  - `upgrade.md`（追加本版本变更记录）
  - `docs/install/install.sh` 的默认 `jeepayRef`
  - `docs/install/config.sh` 的注释提示
  - `docs/deploy/shell.md` 对应文案

## 六、测试

- Shell 相关改动：本地运行 `docs/install/test_*.sh`，应全部 PASS。
- Java 改动：本地 `mvn -B -DskipTests clean compile` 通过；涉及业务逻辑时补 JUnit 测试。
- 部署改动（nginx / 三个服务 yml / docker-compose）：建议在测试服务器上真实走一遍完整安装 + 登录 + 创建订单流程，而不仅看 `docker ps` 的 healthy。
- CI 工作流（`.github/workflows/ci.yml`）会在 PR 时自动跑 Shell 测试 + Maven 编译。

## 七、提交说明与注释

- 中文优先。文档、注释、commit message 默认中文。
- 注释写"为什么 / 注意事项"，避免解释"代码在做什么"（好的命名已经说明了）。
- 对外接口字段 / 协议 / 第三方 SDK 要求的命名保留英文，但补中文说明。

## 八、外部贡献者

- Fork 本仓库到自己的 GitHub / Gitee。
- 基于最新的 `dev` 分支开 `feature/*`。
- PR 目标 `dev`，不要直接到 `master`。
- 首次贡献请在 PR body 里简单介绍一下背景 / 测试情况，方便 review。

遇到不确定的地方，优先开 Issue 讨论，避免花时间做一遍大改动再发现方向不对。
