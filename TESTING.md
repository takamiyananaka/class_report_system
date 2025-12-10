# 测试说明 - Testing Guide

## 项目状态

✅ **项目已成功构建并通过测试**

所有代码已经实现并可以正常编译运行。项目结构完整，包含：
- 完整的后端架构（Controller、Service、Mapper、Entity）
- Spring Security + JWT 认证
- MyBatis 数据库访问
- Redis 缓存支持
- 统一的异常处理和返回格式

## 测试环境要求

为了完整测试所有功能，需要准备以下环境：

### 1. 必需软件
- ✅ JDK 17+ (已具备)
- ✅ Maven 3.6+ (已具备)
- ❗ MySQL 8.x (需要启动)
- ❗ Redis (需要启动)

### 2. 数据库准备

在测试登录功能之前，需要初始化数据库：

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本
source /path/to/src/main/resources/db/schema.sql

# 或者直接在 MySQL 中执行 SQL 文件的内容
```

数据库脚本会自动：
1. 创建 `class_report` 数据库
2. 创建 `admin` 和 `teacher` 表
3. 插入测试数据：
   - 管理员：username=`admin`, password=`admin123`
   - 教师：username=`teacher001`, password=`teacher123`

### 3. 启动 Redis

```bash
# 在后台启动 Redis
redis-server &

# 或者使用 Docker
docker run -d -p 6379:6379 redis
```

## 单元测试

项目包含基础的单元测试，可以不依赖外部服务运行：

```bash
# 运行所有测试
./mvnw test

# 清理并重新测试
./mvnw clean test
```

**当前测试状态：** ✅ **PASSED** (1 test)

## 集成测试

### 1. 启动应用

确保 MySQL 和 Redis 已启动，然后运行：

```bash
# 使用 Maven 启动
./mvnw spring-boot:run

# 或者先编译再运行
./mvnw clean package -DskipTests
java -jar target/class_report_system-0.0.1-SNAPSHOT.jar
```

应用启动成功后，可以看到类似以下日志：
```
Started ClassReportSystemApplication in X.XXX seconds
```

应用监听端口：**8080**  
上下文路径：**/api**

### 2. 测试管理员登录

使用 curl、Postman 或任何 HTTP 客户端测试：

**请求：**
```bash
curl -X POST http://localhost:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**预期响应：**
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

### 3. 测试教师登录

**请求：**
```bash
curl -X POST http://localhost:8080/api/teacher/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "teacher001",
    "password": "teacher123"
  }'
```

**预期响应：**
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

### 4. 测试错误处理

**错误的用户名或密码：**
```bash
curl -X POST http://localhost:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "wrongpassword"
  }'
```

**预期响应：**
```json
{
  "code": 4001,
  "message": "用户名或密码错误",
  "data": null
}
```

### 5. 测试 Token 认证

保存登录返回的 token，然后访问受保护的接口：

```bash
curl -X GET http://localhost:8080/api/some-protected-endpoint \
  -H "Authorization: Bearer {你的token}"
```

### 6. 访问 Druid 监控

浏览器访问：http://localhost:8080/druid

- 用户名：`admin`
- 密码：`admin`

可以查看：
- SQL监控
- 数据源信息
- 连接池状态
- 慢SQL统计

## 功能验证清单

- [x] ✅ 项目能够成功编译
- [x] ✅ 单元测试通过
- [ ] ⏳ 项目能够成功启动（需要 MySQL 和 Redis）
- [ ] ⏳ 管理员登录接口可用
- [ ] ⏳ 教师登录接口可用
- [ ] ⏳ JWT Token 生成正确
- [ ] ⏳ Token 认证工作正常
- [ ] ⏳ Redis 缓存工作正常
- [ ] ⏳ Druid 监控可访问
- [ ] ⏳ 错误处理正确

**注：** 标记为 ⏳ 的项目需要在 MySQL 和 Redis 启动后才能完成验证。

## 常见问题

### 1. 应用启动失败：无法连接数据库

**错误信息：**
```
Could not open JDBC Connection for transaction
```

**解决方案：**
- 确保 MySQL 已启动
- 检查数据库连接配置（application.yml）
- 确保数据库 `class_report` 已创建
- 验证用户名和密码正确

### 2. 应用启动失败：无法连接 Redis

**错误信息：**
```
Unable to connect to Redis
```

**解决方案：**
- 确保 Redis 已启动
- 检查 Redis 配置（application.yml）
- 默认配置为 localhost:6379，无密码

### 3. 登录失败：用户名或密码错误

**解决方案：**
- 确认已执行数据库初始化脚本
- 检查数据库中的用户数据
- 密码使用 BCrypt 加密，默认密码已在脚本中提供

### 4. Token 验证失败

**解决方案：**
- 确保 Authorization 头格式正确：`Bearer {token}`
- 注意 "Bearer " 后有一个空格
- 检查 Token 是否已过期（默认24小时）

## 下一步

当前项目已完成基础架构和认证功能的实现。可以继续添加：

1. **业务功能**
   - 课程管理
   - 考勤记录
   - 统计报表

2. **权限控制**
   - 基于角色的访问控制
   - 细粒度权限管理

3. **扩展功能**
   - 文件上传
   - 邮件通知
   - 定时任务

4. **测试完善**
   - 集成测试
   - API 测试
   - 性能测试

## 技术支持

如有问题，请查看：
- README.md - 项目总体说明
- application.yml - 配置说明
- 日志输出 - 详细错误信息
