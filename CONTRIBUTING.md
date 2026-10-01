# 贡献指南

感谢你考虑为 Jeepay 贡献代码 / 文档 / 测试。本指南只是一份让协作成本变低的约定，不是规矩；遇到特殊场景，优先沟通。

## 一、分支模型

| 分支 | 定位 | 谁能直推 | 生命周期 |
|---|---|---|---|
| `master` | 稳定发布分支；正式版本由 tag + GitHub Release 固定 | 任何人都不能直推，只接 merge | 永久 |
| `dev` | 日常开发集成分支；"比 master 超前半步" | 任何人都不能直推，只接 merge | 永久 |
| `feature/*` | 单个新功能 / 重构（示例 `feature/split-payment`） | 开发者自己 | 合入后可按需清理 |
| `fix/*` | 非紧急 bug 修复（示例 `fix/refund-edge-case`） | 开发者自己 | 合入后可按需清理 |
| `hotfix/*` | **紧急**生产修复（示例 `hotfix/mysql-pwd`） | 开发者自己 | 发布后同步到 `dev` |
| `release/*` | 可选的发版准备分支（示例 `release/V3.3.0`），只做版本号 / changelog / 回归 | 开发者自己 | 发布后可按需清理 |

这里的“不直推”是协作约定；实际强制约束以仓库分支保护设置为准。小版本可直接用 `dev → master` 的发布 PR，无需为了流程增加 `release/*` 分支。

## 二、开发流程

先搜索现有 Issue / PR。问题或需求应写明复现步骤、预期结果、范围和验收标准；小修复可直接在 PR 中说明，大改动先用 Issue 对齐。一个 PR 聚焦一个问题。普通功能、修复和文档贡献都基于最新 `dev`，提交到 `dev`。

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
# 发布后另开 PR 将修复同步到 dev（合并或带原始提交引用的 cherry-pick）
# 在同步 PR 上重新验证，并在原 PR / 发布说明中关联，避免后续版本丢失修复
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
4. 填写仓库 PR 模板；修复尽量附带修改前失败、修改后通过的回归测试，不能自动化时写清人工验证步骤和限制。
5. 合并前由维护者审查范围、验收标准、测试和兼容性，处理未解决的评审意见，必需 CI 全绿；不以固定多人审批数量替代有效审查。首次 fork 贡献可能需要维护者先批准 CI 运行。
6. 合并后的 `dev` / `master` push CI 仍需通过；发版前完成涉及模块的前后端联调，CI 通过不等于完成真实支付验证。

GitHub PR 模板进入默认分支 `master` 后才会自动展示。治理文件和 CI 可通过定向同步 PR 更新长期分支，但必须适配目标分支已有的测试与依赖，不能夹带未发布业务改动。启用必需检查前，先确认对应长期分支能产出同名检查。

## 五、Tag 与版本号

- 语义化版本 `MAJOR.MINOR.PATCH`，tag 名形如 `V3.2.7`。
- **PATCH**（例 `V3.2.7 → V3.2.8`）：向后兼容的问题修复；若业务代码或依赖变化，必须重建并标识对应制品，不能假定业务镜像不变。
- **MINOR**（例 `V3.2.x → V3.3.0`）：新功能 / API 扩展，业务镜像需要重打并推送 SWR / Docker Hub。
- **MAJOR**（例 `V3.x → V4.0.0`）：不兼容变更（DB schema 破坏 / 接口协议破坏）。
- 发版时同步更新：
  - `pom.xml` 的 `isys.version`
  - `version.md`
  - `upgrade.md`（追加本版本变更记录）
  - `docs/install/install.sh` 的默认 `jeepayRef`
  - `docs/install/config.sh` 的注释提示
  - `docs/deploy/shell.md` 对应文案

正式发布从已审核、检查通过的 `master` 提交创建不可随意移动的版本 tag，并创建 GitHub Release。Release 说明至少包含：

- 后端 tag / commit 与配套 [jeepay-ui](https://github.com/jeequan/jeepay-ui) tag / commit；涉及集成示例时记录 [jeepay-skills](https://github.com/jeequan/jeepay-skills) 版本
- 兼容范围、API / 签名 / 权限 / 数据库和配置变化；不能把同名版本号当成兼容性证明
- `upgrade.md` 中的升级顺序、备份、SQL / 配置迁移、回滚条件和不兼容提醒
- 实际完成的回归与联调范围、已知问题；安装工具、前端静态文件与业务镜像均应对应明确版本

## 六、测试

使用 JDK 17 和 Maven，在仓库根目录执行与 CI 一致的检查：

```bash
for t in docs/install/test_*.sh; do sh "$t" || exit 1; done
bash -n docs/install/install.sh
bash -n docs/install/uninstall.sh
mvn -B -DskipTests -T 1C clean compile
mvn -B -DskipTests -T 1C test-compile
mvn -B test
```

- `mvn -B test` 执行当前检出分支已有的单元回归。当前 `master` 包含 FastJson 兼容和消息队列配置测试；`dev` 还包含商户授权回归。治理文件同步不代表授权修复及其测试已发布到 `master`。业务修复应补充相关测试，不要用跳过测试代替通过。
- 在包含商户授权测试的 `dev` 分支，仅快速验证授权时可运行 `mvn -B -pl jeepay-merchant -am -Dtest='*AuthorizationTest' -Dsurefire.failIfNoSpecifiedTests=false test`，它不能替代完整检查。
- 部署或跨仓库接口变更需在隔离测试环境验证安装 / 升级、登录授权、相关下单 / 回调流程，并记录配套前端版本。真实渠道支付需要单独的测试环境与授权；离线单元测试不代表已经通过实付、退款或对账验收。
- CI（`.github/workflows/ci.yml`）在目标为 `dev` / `master` 的 PR 及这两个分支的 push 上运行；保留检查名称 `Shell 安装脚本断言`、`Maven 编译`，后者同时执行测试。
- 在 PR 中列出具体命令、结果以及未运行项和原因；不要提交真实商户信息、密钥、支付凭据或生产日志。

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

## 九、安全问题

不要在公开 Issue、PR、评论或群聊中披露未修复漏洞细节、利用步骤或凭据。目前本文尚未列出经确认可用的私密漏洞报告入口。维护者需确认启用 GitHub Private vulnerability reporting 或公布经核实的专用私密联系方式，再补充安全政策；在入口明确前，不要公开提交敏感材料。普通问题仍可通过 Issue 讨论。
