# HelpCountryside 助农商城

一个面向乡村农产品与乡村旅游的社区电商后端，基于 **Spring Boot + MyBatis-Plus + MySQL + Redis** 实现。
除常规的商品、购物车、订单链路外，还包含社区动态（图文 / 视频）、关注粉丝关系、WebSocket 实时消息推送，
以及基于 Redis 的登录态、点赞收藏和订单超时自动关闭。

> 前端为独立仓库的移动端页面，本仓库只包含服务端代码。

---

## 功能模块

| 模块 | 主要能力 |
| --- | --- |
| 登录鉴权 | 手机号 + 短信验证码登录（新用户自动注册）、Token 会话、支付密码设置 |
| 个人主页 | 资料修改、头像上传、关注 / 取关、关注列表与粉丝列表 |
| 商品 | 发布（草稿 → 上架）、图片与详情图上传、分页浏览、按分类检索、批量上下架 |
| 购物车 | 加购、列表、单条删除、清空 |
| 订单 | 下单、支付、取消、发货、确认收货、订单详情、物流信息 |
| 社区 | 图文 / 视频动态发布、点赞、收藏、评论、关注流、热门动态 |
| 消息 | WebSocket 按用户推送订单状态通知；Redis Stream 长轮询拉取系统通知 |
| 工具 | 腾讯云 COS 上传、阿里云短信、第三方植物识别接口 |

## 技术栈

| 分类 | 选型 |
| --- | --- |
| 语言 / 构建 | Java 8、Maven |
| 框架 | Spring Boot 2.7.12、Spring MVC、Spring WebSocket |
| 持久层 | MyBatis-Plus 3.5.2、MySQL 8.0 |
| 缓存 / 中间件 | Redis、Redisson（分布式锁）、Lettuce 连接池 |
| 三方服务 | 腾讯云 COS（对象存储）、阿里云短信、聚合数据（植物识别、物流） |
| 工具库 | Hutool、Lombok、Commons Lang3、Commons IO |

## 系统设计要点

**1. 无状态登录态**
验证码写入 `login:code:{phone}`，有效期 5 分钟。登录成功后生成 8 位随机 Token，
用户信息以 Hash 结构存入 `login:token:{token}` 并设置过期时间，前端通过 `Authorization` 头携带。
服务端不保存 Session，可直接水平扩容。

**2. 两级拦截器**
`TokensInterception`（order 0）解析 Token 并把用户放入 `ThreadLocal`，
`LoginInterception`（order 1）校验登录态，未登录直接返回 401。
业务层通过 `UserLocal.getUser()` 取当前用户，请求结束时在 `afterCompletion` 中清理 `ThreadLocal`，避免线程复用串数据。

**3. 点赞 / 收藏不落库**
点赞、收藏关系用 Redis Set 维护，并且双向记录：
`blog:liked:{blogId}` 记录谁点过，`user:liked:blog:{userId}` 记录某人点过哪些，
列表页一次 `SMEMBERS` 就能判断「我是否点过」，避免每次查询都去 join 关系表。

**4. 订单超时自动关闭**
下单时把订单号写入 Redis ZSet（score = 超时时间戳），
定时任务每秒扫描到期订单并关闭，扫描过程用 **Redisson 分布式锁** 加锁，保证多实例部署时不会重复处理。

**5. 消息推送**
订单状态变更通过 WebSocket（`/imserver/{userId}`）主动推送给对应用户；
系统通知走 Redis Stream 消费组做长轮询，单次请求最长阻塞 10 秒后返回，避免长时间占用 Tomcat 线程。

**6. 统一响应与异常**
业务接口统一返回 `Result{success, errorMsg, data}`；
全局异常处理 `CommonExceptionAdvice` 把参数校验、绑定异常、未知异常统一转换成对应 HTTP 状态码与响应体。

## 目录结构

```
src/main/java/com/eng
├── HelpCountrysideApplication.java   启动类
├── config/                           配置类（MVC、MyBatis-Plus、Redisson、WebSocket、COS）
├── controller/                       接口层，按业务域拆分
├── service/                          业务接口
│   └── impl/                         业务实现
├── mapper/                           MyBatis-Plus Mapper
├── domain/
│   ├── po/                           数据库实体
│   ├── dto/                          请求参数
│   └── vo/                           响应视图
└── util/                             拦截器、统一响应、异常、短信、COS、WebSocket 等
src/main/resources
├── application.yaml                  主配置（敏感项全部走环境变量）
└── application-dev.yaml.example      本地配置模板
docs/schema.sql                       数据库建表脚本
```

## 快速开始

### 1. 环境要求

- JDK 8+
- Maven 3.6+
- MySQL 8.0
- Redis 5.0+

### 2. 初始化数据库

```bash
mysql -uroot -p < docs/schema.sql
```

### 3. 配置本地参数

```bash
cp src/main/resources/application-dev.yaml.example src/main/resources/application-dev.yaml
```

按自己的环境填写 MySQL、Redis 以及腾讯云 / 阿里云的密钥。
`application-dev.yaml` 已被 `.gitignore` 忽略，不会提交到仓库；
也可以不建这个文件，直接用环境变量覆盖（见下方配置说明）。

### 4. 启动

```bash
mvn spring-boot:run
# 或
mvn clean package -DskipTests && java -jar target/help-countryside-1.0.0.jar
```

服务默认端口 `8085`。

### 5. 验证

```bash
# 发送验证码
curl -X POST "http://localhost:8085/user/code?phone=13800138000"

# 登录（验证码从 Redis 或短信中获取）
curl -X POST http://localhost:8085/user/login \
     -H "Content-Type: application/json" \
     -d '{"phone":"13800138000","code":"123456","city":"杭州"}'

# 携带返回的 token 访问需要登录的接口
curl http://localhost:8085/me -H "Authorization: {token}"
```

## 配置说明

`application.yaml` 中所有敏感配置都使用 `${环境变量:默认值}` 形式，部署时用环境变量注入即可：

| 环境变量 | 说明 | 默认值 |
| --- | --- | --- |
| `SERVER_PORT` | 服务端口 | `8085` |
| `MYSQL_HOST` / `MYSQL_PORT` | 数据库地址 | `localhost` / `3306` |
| `MYSQL_DATABASE` | 数据库名 | `helpcountryside` |
| `MYSQL_USERNAME` / `MYSQL_PASSWORD` | 数据库账号 | `root` / 空 |
| `REDIS_HOST` / `REDIS_PORT` | Redis 地址 | `127.0.0.1` / `6379` |
| `REDIS_PASSWORD` | Redis 密码 | 空 |
| `TENCENT_COS_SECRET_ID` / `TENCENT_COS_SECRET_KEY` | 腾讯云 COS 密钥 | 空 |
| `TENCENT_COS_BUCKET_NAME` / `TENCENT_COS_REGION` | COS 存储桶与地域 | 空 / `ap-shanghai` |
| `ALIYUN_SMS_ACCESS_KEY_ID` / `ALIYUN_SMS_ACCESS_KEY_SECRET` | 阿里云短信密钥 | 空 |
| `ALIYUN_SMS_SIGN_NAME` / `ALIYUN_SMS_TEMPLATE_CODE` | 短信签名与模板 | 空 |
| `PLANT_API_APP_CODE` | 植物识别接口 AppCode | 空 |

## 接口概览

| 前缀 | 说明 |
| --- | --- |
| `/user` | 验证码、登录、支付密码 |
| `/me` | 当前用户信息、余额、通知、植物识别 |
| `/own` | 个人主页、头像、关注与粉丝 |
| `/item`、`/update` | 商品浏览、发布、编辑 |
| `/cart` | 购物车 |
| `/order`、`/pay`、`/location` | 下单、支付、物流 |
| `/address` | 收货地址 |
| `/blog`、`/comments`、`/video` | 社区动态、评论、视频 |
| `/imserver/{userId}` | WebSocket 连接地址 |

## 开发约定

- 包名全小写，类名大驼峰，方法名小驼峰；常量统一放在 `util/SysConstants`、`util/RedisConstants`。
- 分层职责：Controller 只做参数接收与转发，业务逻辑写在 Service，SQL 由 MyBatis-Plus 或 Mapper 负责。
- 统一使用 `Result` 返回，异常交给 `CommonExceptionAdvice` 处理，不在 Controller 里 try-catch。
- 所有密钥通过配置注入，禁止硬编码到代码里。
- 提交信息使用「模块: 变更说明」格式，例如 `order: 修复超时订单重复关闭`。

## 已知问题与后续计划

- 服务之间存在循环依赖（已用 `@Lazy` 与 `allow-circular-references` 兜底），后续应按领域拆分模块、改为事件驱动。
- 返回体 `Result` 与异常处理使用的 `R` 尚未完全统一，计划收敛为一套。
- 商品、订单相关查询尚未加本地缓存，热点商品可引入 Caffeine + Redis 二级缓存。
- 缺少接口文档，计划接入 SpringDoc / Knife4j 生成在线文档。
- 单元测试覆盖不足，目前仅覆盖密码加密校验，后续补齐 Service 层用例。
