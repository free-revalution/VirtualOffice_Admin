# Cloud Office Admin System

## 项目概述

Cloud Office Admin是一个基于Spring Boot开发的云办公管理系统，提供虚拟办公空间管理、用户管理、文件上传下载等功能。系统采用分层架构设计，支持多用户协作和资源共享。

## 技术栈

- **后端框架**: Spring Boot 2.7.x
- **ORM框架**: Spring Data JPA
- **安全框架**: Spring Security
- **数据库**: MySQL/PostgreSQL
- **API文档**: SpringDoc OpenAPI (Swagger 3)
- **构建工具**: Maven
- **测试框架**: JUnit 5, Mockito

## 项目结构

```
CloudOffice_Admin/
├── src/
│   ├── main/
│   │   ├── java/com/example/cloudoffice_admin/
│   │   │   ├── config/        # 配置类
│   │   │   ├── controller/    # 控制器层
│   │   │   ├── model/         # 数据模型
│   │   │   ├── repository/    # 数据访问层
│   │   │   ├── service/       # 服务层接口
│   │   │   │   └── impl/      # 服务层实现
│   │   │   └── CloudOfficeAdminApplication.java # 应用入口
│   │   └── resources/         # 资源文件
│   │       ├── application.properties # 应用配置
│   │       └── static/        # 静态资源
│   └── test/                  # 测试代码
├── pom.xml                    # Maven依赖配置
└── README.md                  # 项目说明文档
```

## 核心功能

### 1. 用户管理
- 用户注册、登录和身份验证
- 用户信息管理（创建、更新、删除、查询）
- 用户状态管理和活跃度监控

### 2. 虚拟空间管理
- 创建和管理虚拟办公空间
- 空间权限控制（公开/私有）
- 空间内区域管理
- 用户在空间中的位置和存在状态管理

### 3. 文件上传下载
- 单个文件和批量文件上传
- 文件下载和预览
- 文件管理（删除、搜索、分类）
- 文件大小限制和类型验证

## 快速开始

### 1. 环境要求
- JDK 11 或更高版本
- Maven 3.6+ 或 Gradle 7.0+
- MySQL 8.0+ 或 PostgreSQL 10+

### 2. 数据库配置

创建数据库并配置连接信息（在`application.properties`文件中）：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/cloud_office_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

### 3. 运行项目

使用Maven命令运行：

```bash
mvn spring-boot:run
```

或者打包后运行：

```bash
mvn clean package
java -jar target/cloudoffice-admin-0.0.1-SNAPSHOT.jar
```

### 4. 访问API文档

项目启动后，可以通过以下地址访问Swagger API文档：

```
http://localhost:8080/swagger-ui.html
```

或者使用OpenAPI JSON：

```
http://localhost:8080/v3/api-docs
```

## API端点

### 用户管理API
- `GET /api/admin/users` - 获取所有用户
- `GET /api/admin/users/{id}` - 获取指定用户
- `POST /api/admin/users` - 创建新用户
- `PUT /api/admin/users/{id}` - 更新用户信息
- `DELETE /api/admin/users/{id}` - 删除用户
- `GET /api/admin/users/online` - 获取在线用户列表

### 虚拟空间API
- `GET /api/v1/spaces` - 获取空间列表
- `POST /api/v1/spaces` - 创建新空间
- `GET /api/v1/spaces/public` - 获取公开空间
- `GET /api/v1/spaces/user` - 获取用户创建的空间
- `GET /api/v1/spaces/{id}/zones` - 获取空间中的区域
- `POST /api/v1/spaces/{id}/zones` - 创建区域

### 文件上传下载API
- `POST /api/files/upload` - 上传单个文件
- `POST /api/files/upload/batch` - 批量上传文件
- `GET /api/files/my-files` - 获取当前用户的文件列表
- `GET /api/files/download/{id}` - 下载文件
- `DELETE /api/files/{id}` - 删除文件
- `GET /api/files/stats` - 获取用户文件统计信息

## 安全配置

系统使用Spring Security进行身份验证和授权。默认配置要求所有API端点都需要身份验证，可以在`SecurityConfig`类中自定义安全规则。

## 测试

项目包含单元测试和集成测试，使用JUnit 5和Mockito框架。运行测试命令：

```bash
mvn test
```

## 许可证

[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## 联系方式

如有问题或建议，请联系项目维护者。
