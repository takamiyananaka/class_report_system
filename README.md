# 学工部课程考勤系统 - Class Report System

完整的后端项目，实现管理员和教师的双角色登录认证功能。

## 技术栈

- **Spring Boot** 3.5.7
- **MyBatis** 3.0.5
- **MySQL** 8.x
- **Redis**
- **Spring Security + JWT**
- **Lombok**
- **Druid** 数据库连接池
- **Hutool** 工具库

## 项目结构

```
src/main/java/com/xuegongbu/
├── ClassReportSystemApplication.java       # 启动类
├── config/                                  # 配置类
│   ├── SecurityConfig.java                 # Spring Security配置
│   ├── RedisConfig.java                    # Redis配置
│   ├── CorsConfig.java                     # 跨域配置
│   └── MyBatisConfig.java                  # MyBatis配置
├── controller/                              # 控制器
│   ├── admin/                              # 管理员端
│   │   └── AdminAuthController.java        # 管理员登录API
│   └── teacher/                            # 教师端
│       └── TeacherAuthController.java      # 教师登录API
├── entity/                                  # 实体类
│   ├── Admin.java                          # 管理员实体
│   └── Teacher.java                        # 教师实体
├── mapper/                                  # MyBatis Mapper接口
│   ├── AdminMapper.java
│   └── TeacherMapper.java
├── service/                                 # 服务层
│   ├── AdminService.java
│   ├── TeacherService.java
│   ├── AuthService.java                    # 认证服务
│   └── impl/                               # 实现类
│       ├── AdminServiceImpl.java
│       ├── TeacherServiceImpl.java
│       └── AuthServiceImpl.java
├── security/                                # 安全相关
│   ├── JwtAuthenticationFilter.java        # JWT过滤器
│   ├── JwtTokenProvider.java               # JWT工具类
│   ├── UserDetailsServiceImpl.java         # 用户详情服务
│   └── SecurityUser.java                   # Security用户封装
├── dto/                                     # 数据传输对象
│   ├── LoginRequest.java                   # 登录请求DTO
│   └── LoginResponse.java                  # 登录响应DTO
├── vo/                                      # 视图对象
│   ├── AdminVO.java                        # 管理员视图
│   └── TeacherVO.java                      # 教师视图
├── common/                                  # 通用类
│   ├── Result.java                         # 统一返回结果
│   ├── ResultCode.java                     # 返回码枚举
│   ├── Constants.java                      # 常量类
│   └── exception/                          # 异常处理
│       ├── GlobalExceptionHandler.java     # 全局异常处理器
│       └── BusinessException.java          # 业务异常
└── utils/                                   # 工具类
    └── RedisUtils.java                     # Redis工具类

src/main/resources/
├── mapper/                                  # MyBatis XML映射文件
│   ├── AdminMapper.xml
│   └── TeacherMapper.xml
├── db/                                      # 数据库脚本
│   └── schema.sql                          # 建表脚本
├── application.yml                          # 主配置文件
└── application-dev.yml                      # 开发环境配置
```

## 快速开始

### 1. 环境准备

确保已安装以下软件：
- JDK 17+
- MySQL 8.x
- Redis
- Maven 3.6+

### 2. 数据库初始化

执行数据库脚本：

```bash
mysql -u root -p < src/main/resources/db/schema.sql
```

或者在MySQL客户端中执行 `src/main/resources/db/schema.sql` 文件。

脚本会自动：
- 创建 `class_report` 数据库
- 创建 `tb_admin` 和 `tb_teacher` 表
- 插入默认管理员账号：用户名 `admin`，密码 `admin123`
- 插入测试教师账号：用户名 `teacher001`，密码 `teacher123`

### 3. 启动Redis

```bash
redis-server
```

### 4. 修改配置

如果需要修改数据库和Redis连接信息，请编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/class_report?...
      username: root
      password: root
  
  data:
    redis:
      host: localhost
      port: 6379
      password: 
```

### 5. 启动应用

```bash
./mvnw spring-boot:run
```

或者使用IDE运行 `ClassReportSystemApplication` 类。

应用启动后访问：http://localhost:8080/api

## API 接口

### 管理员登录

**请求：**
```http
POST http://localhost:8080/api/admin/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "phone": "13800138000",
      "email": "admin@example.com",
      "status": 1
    }
  }
}
```

### 教师登录

**请求：**
```http
POST http://localhost:8080/api/teacher/auth/login
Content-Type: application/json

{
  "username": "teacher001",
  "password": "teacher123"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "teacher001",
      "realName": "张老师",
      "teacherNo": "T001",
      "phone": "13900139000",
      "email": "teacher@example.com",
      "department": "计算机学院",
      "status": 1
    }
  }
}
```

### 使用Token访问受保护接口

在请求头中添加：
```
Authorization: Bearer {token}
```

## 监控

访问Druid数据源监控：http://localhost:8080/druid

- 用户名：`admin`
- 密码：`admin`

## 核心功能

### 1. JWT认证
- Token生成和验证
- Token过期时间：24小时
- 支持多角色（管理员、教师）

### 2. Redis缓存
- 用户信息缓存
- Token缓存
- 自动过期管理

### 3. Spring Security
- BCrypt密码加密
- 基于角色的访问控制
- JWT过滤器
- 白名单配置

### 4. MyBatis
- XML方式编写SQL
- ResultMap映射
- 驼峰命名转换

### 5. 统一返回格式
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 6. 全局异常处理
- 业务异常捕获
- 统一错误码
- 错误日志记录

## 默认账号

### 管理员
- 用户名：`admin`
- 密码：`admin123`

### 教师
- 用户名：`teacher001`
- 密码：`teacher123`

## 测试

运行测试：
```bash
./mvnw test
```

## 开发指南

### 添加新接口

1. 在对应的Controller包下创建新的Controller
2. 使用 `@RestController` 和 `@RequestMapping` 注解
3. 返回值使用 `Result<T>` 包装

### 添加新实体

1. 在 `entity` 包下创建实体类
2. 使用 `@Data` 注解
3. 在 `mapper` 包下创建Mapper接口
4. 在 `resources/mapper` 下创建XML映射文件

### 权限控制

在方法上使用 `@PreAuthorize` 注解：
```java
@PreAuthorize("hasRole('ADMIN')")
public Result<?> adminOnlyMethod() {
    // ...
}
```

## 注意事项

1. JWT secret 使用固定值便于测试，生产环境请修改
2. Redis 默认不设置密码，生产环境请配置
3. 数据库连接信息使用默认本地配置
4. 日志级别设置为 debug 便于调试
5. 所有文件使用 UTF-8 编码

## 许可证

MIT License
