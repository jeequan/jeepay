# 项目结构与仓库关系

## 项目结构

```text
jeepay
├── conf                     # 系统部署所需 yml 配置
├── docker                   # Docker 相关文件
├── docs                     # 项目文档
│   ├── deploy               # 部署详细文档
│   ├── install              # 安装脚本
│   ├── script               # 启动脚本
│   └── sql                  # 初始化 SQL 文件
├── jeepay-components        # 公共组件目录
│   ├── jeepay-components-mq # MQ 组件
│   └── jeepay-components-oss# OSS 组件
├── jeepay-core              # 核心依赖模块
├── jeepay-manager           # 运营平台服务端（9217）
├── jeepay-merchant          # 商户系统服务端（9218）
├── jeepay-payment           # 支付网关（9216）
├── jeepay-service           # 业务层代码
└── jeepay-z-codegen         # MyBatis 代码生成模块
```

## 仓库关系

| 仓库 | 作用 |
|---|---|
| `jeepay` | 服务端主仓库，包含支付网关、运营平台、商户平台、核心服务 |
| `jeepay-ui` | 前端项目 |
| `jeepay-sdk-java` | Java SDK，供业务系统对接 Jeepay 接口时使用 |
