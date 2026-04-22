# 域名 + HTTPS 反代

jeepay 三个平台的公网接入需要在 19216 / 19217 / 19218 前面再架一层 nginx + SSL。本文给出可直接抄的模板。

## 代码层面已就绪

- 内置 `nginx.conf` 三个 server 都已补 `X-Forwarded-Proto` / `X-Forwarded-Port` / `proxy_http_version 1.1` / WebSocket 头 / 长超时。
- Spring Boot 已开启 `server.forward-headers-strategy: framework`。

外层反代只要照下面模板写，回跳 URL / WebSocket / 微信支付 H5 redirect 都会自动拼对。

## 推荐拓扑：三个子域名

| 外部域名 | 用途 | 内部回源 |
|---|---|---|
| `admin.example.com` | 运营平台 | `http://127.0.0.1:19217` |
| `mch.example.com` | 商户平台 | `http://127.0.0.1:19218` |
| `pay.example.com` | 支付网关 + 收银台 | `http://127.0.0.1:19216` |

## 外层 nginx 模板

```nginx
server {
    listen 443 ssl http2;
    server_name pay.example.com;
    ssl_certificate     /etc/ssl/jeepay/pay.crt;
    ssl_certificate_key /etc/ssl/jeepay/pay.key;

    location / {
        proxy_pass http://127.0.0.1:19216;
        proxy_http_version 1.1;                         # 必须，否则 WS 会被隐式关闭

        proxy_set_header Host              $host;
        proxy_set_header X-Real-IP         $remote_addr;
        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;     # 必须，否则 Spring Boot 拼 http:// 回调
        proxy_set_header X-Forwarded-Port  $server_port;

        # WebSocket（商户端支付测试 / 收银台订单推送）
        proxy_set_header Upgrade    $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_read_timeout  3600s;                      # 默认 60s，长连接会静默断
        proxy_send_timeout  3600s;
    }
}
# admin / mch 域名同构，仅 proxy_pass 换为 19217 / 19218
```

## 快速申请 Let's Encrypt 证书

```bash
# 宿主机（不是容器内）
yum install -y nginx certbot python3-certbot-nginx   # Ubuntu: apt-get -y install ...
certbot --nginx -d admin.example.com -d mch.example.com -d pay.example.com \
  --agree-tos -m you@example.com --redirect
```

`certbot --nginx` 会自动给你上面写的 server 块加 `listen 443 ssl` + cert 路径 + HTTP→HTTPS 301。

## 第三方支付平台回调 URL

去微信 / 支付宝 / 云闪付后台，异步通知 / 回跳 URL 必须填**公网域名**，不要填内网 IP：

```
https://pay.example.com/api/pay/notify/...
https://pay.example.com/api/anon/paySuccess?...
```

## 验证

```bash
# 握手 101 + Spring Boot 能识别 https
curl -s -I https://admin.example.com/api/anon/auth/vercode?t=$(date +%s) | head -3
# 收银台
curl -s -o /dev/null -w "%{http_code}\n" https://pay.example.com/cashier/index.html
```

访问 `https://admin.example.com` 登录、`https://mch.example.com` 发起支付测试，浏览器 DevTools Network 里 WS 连接应该能持续收到订单状态推送。

## 防火墙

- 公网必须开：`80` `443`
- 公网建议关：`19216` / `19217` / `19218`（已通过 80/443 反代提供）

## 不同拓扑的取舍

- **单域名 + 路径前缀**（比如 `https://www.example.com/admin/`）：需要同步改前端 `publicPath` + Spring Boot `context-path`，工作量大，不推荐新手。
- **只对外暴露收银台**：SaaS / 电商最常用，公网只放 `pay.example.com`，运营 / 商户平台留内网。防火墙只开一个子域对应的 80/443。
