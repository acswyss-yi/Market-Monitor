# Market Monitor

实时金融市场监控与价格预警系统。支持加密货币与美股行情追踪，当价格满足预设条件时，自动通过邮件或钉钉发送告警通知。

**Demo：** http://8.149.245.12:5173/

## 功能特性

- **实时行情拉取**：定时拉取加密货币（Gate.io）与美股（Alpha Vantage）价格，结果缓存至内存
- **价格预警规则**：支持配置多条规则，指定资产、触发方向（高于/低于）、阈值与冷却期
- **多渠道通知**：支持邮件与钉钉 Webhook，通知异步执行，不阻塞主调度流程
- **完整 REST API**：规则 CRUD、启用/禁用切换、行情查询，附带 Swagger UI 文档

## 技术栈

- Java 21 + Spring Boot 3.4
- MyBatis + H2
- Caffeine Cache
- Spring Scheduler / Async
- Log4j2
- Docker

## 快速启动

### 前置要求

- JDK 21+
- Maven 3.9+
- Gate.io API Key（加密货币行情）
- Alpha Vantage API Key（美股行情）

### 本地运行

```bash
# 克隆项目
git clone <repo-url>
cd market-monitor

# 配置环境变量
export GATE_API_KEY=your_gate_api_key
export ALPHA_VANTAGE_API_KEY=your_alpha_vantage_api_key

# 可选：邮件通知配置
export MAIL_HOST=smtp.example.com
export MAIL_PORT=465
export MAIL_USERNAME=your@email.com
export MAIL_PASSWORD=your_password

# 启动
mvn spring-boot:run
```

服务默认运行在 `http://localhost:8090`。

### Docker 运行

```bash
docker build -t market-monitor .
docker run -d \
  -p 8090:8090 \
  -e GATE_API_KEY=your_gate_api_key \
  -e ALPHA_VANTAGE_API_KEY=your_alpha_vantage_api_key \
  --name market-monitor \
  market-monitor
```

## API 文档

启动后访问 `http://localhost:8090/swagger-ui.html`

### 主要接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/alert-rules` | 获取所有预警规则 |
| POST | `/api/v1/alert-rules` | 创建预警规则 |
| PUT | `/api/v1/alert-rules/{id}` | 更新预警规则 |
| PATCH | `/api/v1/alert-rules/{id}/toggle` | 启用/禁用规则 |
| DELETE | `/api/v1/alert-rules/{id}` | 删除预警规则 |
| GET | `/api/v1/market-data/all` | 获取全部缓存行情 |
| GET | `/api/v1/market-data` | 查询指定资产价格 |

### 创建预警规则示例

```json
POST /api/v1/alert-rules
{
  "name": "BTC 突破 10 万",
  "assetType": "CRYPTO",
  "symbol": "bitcoin",
  "conditionType": "ABOVE",
  "thresholdPrice": 100000,
  "notifyChannel": "DINGTALK",
  "notifyTarget": "https://oapi.dingtalk.com/robot/send?access_token=xxx",
  "cooldownMinutes": 60
}
```

**AssetType**：`CRYPTO` | `STOCK`

**ConditionType**：`ABOVE`（高于阈值触发）| `BELOW`（低于阈值触发）

**NotifyChannel**：`EMAIL` | `DINGTALK`

## 配置说明

主要配置项位于 `src/main/resources/application.yml`：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `scheduler.crypto.interval-ms` | 60000 | 加密货币拉取间隔（毫秒） |
| `scheduler.stock.interval-ms` | 15000 | 美股拉取间隔（毫秒） |
| `cache.price.ttl-minutes` | 2 | 价格缓存 TTL（分钟） |
| `server.port` | 8090 | 服务端口 |

## 项目结构

```
src/main/java/com/market/monitor/
├── controller/        # REST 接口
├── service/           # 业务逻辑（规则管理、预警评估）
├── scheduler/         # 定时拉取（加密货币、美股）
├── client/            # 外部 API 调用（Gate.io、Alpha Vantage）
├── notification/      # 通知服务（邮件、钉钉）
├── cache/             # Caffeine 价格缓存
├── model/             # 领域模型
├── dto/               # 请求/响应对象
├── mapper/            # MyBatis Mapper
├── enums/             # 枚举（AssetType、ConditionType、NotifyChannel）
└── config/            # Spring 配置
```

## 前端

前端项目位于 [market-monitor-ui](../market-monitor-ui)，基于 React + Ant Design 构建。

