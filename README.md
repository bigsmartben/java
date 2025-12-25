# PUML - Spring Boot Multi-Module Project with Dual DataSources

Java脚手架项目 - 基于 Spring Boot 的多模块工程，支持双数据源（MySQL + PostgreSQL）和双 MyBatis 配置。

## 项目结构

```
puml-parent/
├── puml-common/          # 公共工具和共享代码
├── puml-api/             # API接口和DTO定义
├── puml-service-sample/  # 业务服务模块示例（可扩展为多个 puml-service-xxx）
└── puml-web/             # Web应用入口，统一启动和装配层
```

## 技术栈

- **JDK**: 21
- **Spring Boot**: 3.2.1 (非 Spring Cloud)
- **MyBatis**: 3.0.3 (注解方式，不使用 XML)
- **WebDB**: MySQL (用于 Web 层数据)
- **ServiceDB**: PostgreSQL (用于服务层数据)
- **构建工具**: Maven

## 核心特性

### 1. 双数据源配置

项目配置了两套独立的数据源：

#### WebDB (MySQL)
- **Bean 名称**: `webDataSource`, `webSqlSessionFactory`, `webTransactionManager`
- **Mapper 扫描路径**: `com.bsmartben.puml.web..mapper`
- **配置前缀**: `app.datasource.web.*`

#### ServiceDB (PostgreSQL)
- **Bean 名称**: `serviceDataSource`, `serviceSqlSessionFactory`, `serviceTransactionManager`
- **Mapper 扫描路径**: `com.bsmartben.puml.service..mapper`
- **配置前缀**: `app.datasource.service.*`

### 2. MyBatis Mapper 隔离

两套 MyBatis 配置通过 `@MapperScan` 严格隔离：

```java
// WebDB Configuration
@MapperScan(
    basePackages = "com.bsmartben.puml.web..mapper",
    sqlSessionFactoryRef = "webSqlSessionFactory"
)

// ServiceDB Configuration
@MapperScan(
    basePackages = "com.bsmartben.puml.service..mapper",
    sqlSessionFactoryRef = "serviceSqlSessionFactory"
)
```

Mapper 接口使用注解方式（如 `@Select`, `@Insert`, `@Update`, `@Delete`），无需 `@Mapper` 注解。

### 3. 双事务管理器

项目提供两个独立的事务管理器：

#### 使用 WebDB 事务管理器

```java
@Transactional("webTransactionManager")
public void webOperation() {
    // 使用 web mapper 操作 MySQL
}
```

#### 使用 ServiceDB 事务管理器

```java
@Transactional("serviceTransactionManager")
public void serviceOperation() {
    // 使用 service mapper 操作 PostgreSQL
}
```

**注意**: 
- 不指定事务管理器名称时，Spring 会使用默认的事务管理器（如果有配置 `@Primary`）
- 建议显式指定事务管理器名称，避免混淆
- 跨数据源事务需要分布式事务支持（如 JTA），本项目未实现

## 快速开始

### 前置要求

1. JDK 21
2. Maven 3.6+
3. MySQL 8.0+ (运行在 localhost:3306)
4. PostgreSQL 12+ (运行在 localhost:5432)

### 数据库准备

创建数据库：

```sql
-- MySQL
CREATE DATABASE puml_web CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- PostgreSQL
CREATE DATABASE puml_service;
```

### 配置数据源

编辑 `puml-web/src/main/resources/application.yml`，根据实际情况修改数据库连接信息：

```yaml
app:
  datasource:
    web:
      jdbc-url: jdbc:mysql://localhost:3306/puml_web?...
      username: root
      password: your_password
    service:
      jdbc-url: jdbc:postgresql://localhost:5432/puml_service?...
      username: postgres
      password: your_password
```

### 构建项目

```bash
# 完整编译（跳过测试）
mvn -q -DskipTests package

# 清理并编译
mvn clean package -DskipTests

# 运行测试
mvn test
```

### 启动应用

```bash
cd puml-web
mvn spring-boot:run
```

或运行打包后的 JAR：

```bash
java -jar puml-web/target/puml-web-1.0.0-SNAPSHOT.jar
```

### 验证双数据源

应用启动后，访问以下端点验证：

```bash
# 健康检查
curl http://localhost:8080/api/sample/health

# 测试 WebDB (MySQL) 连接
curl http://localhost:8080/api/sample/test-web-db

# 测试 ServiceDB (PostgreSQL) 连接
curl http://localhost:8080/api/sample/test-service-db

# 获取示例数据
curl http://localhost:8080/api/sample/data
```

## 模块说明

### puml-common
公共工具类和共享代码，可被所有模块依赖。

### puml-api
定义 API 接口和 DTO（数据传输对象）。业务模块的 Facade 接口方法签名只能使用此模块中的 DTO。

### puml-service-sample
业务服务模块示例。包含：
- **Facade**: 对外提供的业务接口（定义在 `facade` 包中）
- **Mapper**: ServiceDB 数据访问接口（使用 PostgreSQL）
- **Service**: 内部业务逻辑实现

可以按此模块结构创建更多业务模块（如 `puml-service-order`, `puml-service-user` 等）。

### puml-web
Web 应用入口和统一装配层。包含：
- **Application**: Spring Boot 启动类
- **Config**: 双数据源配置
- **Controller**: REST API 控制器
- **Mapper**: WebDB 数据访问接口（使用 MySQL）

**注意**: Web 层只能通过 Service 模块的 Facade 接口调用业务逻辑，不能直接访问 Service 层的 Mapper。

## 架构约束

1. **分层隔离**: Web 层通过 Facade + DTO 调用 Service 层，不直接访问 Service 层的 Mapper
2. **Mapper 隔离**: Web Mapper 和 Service Mapper 通过不同的 `@MapperScan` 配置绑定到不同的 SqlSessionFactory
3. **无 Dubbo 依赖**: Service 模块不引入 Dubbo 依赖，保持纯粹的业务逻辑层
4. **注解 Mapper**: 所有 Mapper 使用 MyBatis 注解（`@Select`, `@Insert`, `@Update`, `@Delete`），不使用 XML 配置

## 扩展开发

### 添加新的业务模块

1. 复制 `puml-service-sample` 目录，重命名为 `puml-service-xxx`
2. 修改模块的 `pom.xml`，更新 `artifactId`
3. 在父 `pom.xml` 的 `<modules>` 中添加新模块
4. 在 `puml-web/pom.xml` 中添加对新模块的依赖
5. 实现新模块的 Facade 接口和 Mapper

### 添加新的 Mapper

#### Web Mapper (MySQL)
在 `puml-web` 的 `com.bsmartben.puml.web..mapper` 包下创建接口：

```java
package com.bsmartben.puml.web.mapper;

import org.apache.ibatis.annotations.Select;

public interface YourWebMapper {
    @Select("SELECT 1")
    Integer test();
}
```

#### Service Mapper (PostgreSQL)
在 `puml-service-xxx` 的 `com.bsmartben.puml.service..mapper` 包下创建接口：

```java
package com.bsmartben.puml.service.xxx.mapper;

import org.apache.ibatis.annotations.Select;

public interface YourServiceMapper {
    @Select("SELECT 1")
    Integer test();
}
```

## 常见问题

### Q1: 如何确认 Mapper 绑定到正确的 SqlSessionFactory？

启动应用时，查看日志中的 MyBatis 初始化信息。每个 `@MapperScan` 配置会输出扫描的包路径和绑定的 SqlSessionFactory。

### Q2: 能否在同一个事务中操作两个数据源？

本项目的两个数据源是独立的，不支持跨数据源的 ACID 事务。如需跨数据源事务，需要引入分布式事务解决方案（如 Atomikos、Seata）。

### Q3: 为什么排除了 DataSourceAutoConfiguration？

Spring Boot 默认的 DataSource 自动配置会尝试创建单一数据源，与我们的双数据源配置冲突。通过排除自动配置，我们完全手动控制数据源的创建和配置。

### Q4: 如何添加数据库迁移工具（Flyway/Liquibase）？

可以分别为两个数据源配置独立的迁移工具实例，指定不同的 migration 脚本路径。参考 Spring Boot 官方文档的多数据源迁移配置。

## 验收标准

- ✅ `mvn -q -DskipTests package` 可通过编译
- ✅ `puml-web` 可正常启动
- ✅ Spring 容器中同时存在两套 DataSource/SqlSessionFactory/TransactionManager Bean
- ✅ Web Mapper 绑定 webSqlSessionFactory
- ✅ Service Mapper 绑定 serviceSqlSessionFactory
- ✅ 事务管理器可通过名称区分使用

## 许可证

MIT License