# JWT认证系统实现说明

## 概述

本次更新实现了基于Redis的JWT Token活动跟踪机制，使Token在24小时无操作后自动过期，并修复了所有接口的鉴权逻辑。

## 实现的功能

### 1. Token活动超时机制（24小时无操作后过期）

**实现原理：**
- JWT Token本身的过期时间设置为7天
- 在Redis中记录Token的最后活动时间，TTL设置为24小时
- 每次有效请求都会刷新Redis中的活动时间
- 验证Token时，同时检查JWT有效性和Redis中的活动记录
- 如果Redis中没有该Token的记录，说明已超过24小时无操作，Token失效

**涉及的文件：**
- `src/main/java/com/xuegongbu/util/JwtUtil.java`
  - 新增 `TOKEN_ACTIVITY_PREFIX` 常量：Redis键前缀
  - 新增 `ACTIVITY_TIMEOUT_HOURS` 常量：活动超时时间（24小时）
  - 新增 `JWT_MAX_LIFETIME_DAYS` 常量：JWT本身的最大有效期（7天）
  - 新增 `updateTokenActivity()` 方法：更新Token活动时间
  - 新增 `isTokenActive()` 方法：检查Token是否在活动期内
  - 新增 `invalidateToken()` 方法：使Token失效（用于登出）
  - 修改 `generateToken()` 方法：生成Token后立即在Redis中记录活动时间
  - 修改 `validateToken()` 方法：同时验证JWT和Redis活动记录

- `src/main/java/com/xuegongbu/filter/JwtAuthenticationFilter.java`
  - 修改 `doFilterInternal()` 方法：在验证成功后调用 `updateTokenActivity()` 刷新活动时间

### 2. 完善的鉴权逻辑

**问题：**
- 部分接口在登录状态失效后仍然可以访问
- 这导致获取不到登录账号的teacherNo，产生null值错误

**解决方案：**

#### 2.1 修复SecurityConfig配置
**文件：** `src/main/java/com/xuegongbu/config/SecurityConfig.java`

**修改前：**
```java
.anyRequest().permitAll()  // 所有未明确配置的请求都允许访问
```

**修改后：**
```java
.anyRequest().authenticated()  // 所有未明确配置的请求都需要认证
```

**明确配置的公开端点：**
- Swagger文档：`/doc.html`, `/v3/api-docs/**`, `/swagger-ui/**`等
- 登录接口：`/front/login`, `/admin/login`
- 模板下载：`/courseSchedule/downloadTemplate`, `/class/downloadTemplate`

#### 2.2 添加认证失败处理器
**新增文件：** `src/main/java/com/xuegongbu/config/JwtAuthenticationEntryPoint.java`

**功能：**
- 当用户未登录或Token过期时，统一返回401错误
- 错误消息：`当前登录状态已过期，请重新登录`
- 返回格式符合系统的Result格式

**集成方式：**
在SecurityConfig中添加：
```java
.exceptionHandling(exceptions -> exceptions
    .authenticationEntryPoint(jwtAuthenticationEntryPoint))
```

#### 2.3 完善过滤器排除列表
**文件：** `src/main/java/com/xuegongbu/filter/JwtAuthenticationFilter.java`

在 `EXCLUDED_PATHS` 中添加：
- `/class/downloadTemplate` - 班级模板下载

### 3. 受保护的端点列表

以下所有端点现在都需要有效的JWT Token：

**教师管理：** `/teacher/**`
- 查询教师信息
- 创建、更新、删除教师

**课程管理：** `/course/**`
- 创建、查询、更新、删除课程
- 查询教师的课程列表

**课表管理：** `/courseSchedule/**`（除了 `/downloadTemplate`）
- 导入课表
- 查询、创建、更新、删除课表

**班级管理：** `/class/**`（除了 `/downloadTemplate`）
- 导入班级
- 查询、创建、更新、删除班级

**考勤管理：** `/attendance/**`
- 查询考勤记录
- 手动考勤
- 创建、更新、删除考勤记录

**预警管理：** `/alert/**`
- 查询教师的预警记录

**管理员管理：** `/admin/**`（除了 `/admin/login`）
- 管理员相关操作

## 技术实现细节

### Redis键命名规范
```
token:activity:{完整的JWT Token}
```

### Token验证流程
1. 从请求头中提取 `Authorization: Bearer {token}`
2. 验证JWT签名和格式
3. 检查JWT本身是否过期（7天）
4. 检查Redis中是否存在该Token的活动记录
5. 如果都通过，更新Redis中的活动时间（重置为24小时）
6. 提取用户信息并设置到SecurityContext中

### Token过期场景
1. **JWT本身过期**：生成后7天未使用
2. **活动超时**：连续24小时没有任何API请求
3. **主动登出**：调用 `invalidateToken()` 删除Redis记录

## 配置参数

### application.yml
```yaml
jwt:
  secret: class-report-system-jwt-secret-key-for-token-generation-2025
  expiration: 86400000  # 仅作为配置保留，实际使用7天
```

### 代码中的常量
```java
private static final long ACTIVITY_TIMEOUT_HOURS = 24;      // Redis活动超时：24小时
private static final long JWT_MAX_LIFETIME_DAYS = 7;        // JWT最大生命周期：7天
```

## 测试

### 单元测试
- **JwtAuthenticationFilterTest**：10个测试用例
  - 测试过滤器路径匹配
  - 测试公开端点和需要认证的端点

- **JwtUtilTest**：10个测试用例
  - 测试Token生成
  - 测试从Token中提取用户信息
  - 测试Token验证（有效/无效/活动/不活动）
  - 测试活动时间更新
  - 测试Token失效

**测试结果：** 20个测试全部通过 ✓

### 代码审查
- ✓ 已通过代码审查
- ✓ 已根据审查意见优化代码

### 安全扫描
- ✓ CodeQL安全扫描：0个漏洞

## 使用场景示例

### 场景1：正常使用
1. 用户登录 → 获得Token
2. 使用Token访问API → 成功，活动时间重置为24小时
3. 20小时后再次访问 → 成功，活动时间重置为24小时
4. 继续使用...

### 场景2：长时间不活动
1. 用户登录 → 获得Token
2. 使用Token访问API → 成功
3. 25小时后尝试访问 → 失败，返回401错误："当前登录状态已过期，请重新登录"
4. 需要重新登录

### 场景3：主动登出
1. 用户调用登出接口
2. 服务器调用 `jwtUtil.invalidateToken(token)`
3. 删除Redis中的活动记录
4. Token立即失效

## 数据流图

```
客户端请求
    ↓
JwtAuthenticationFilter（过滤器）
    ↓
检查是否在EXCLUDED_PATHS中？
    ├─ 是 → 跳过JWT验证
    └─ 否 → 继续
        ↓
    提取Authorization头
        ↓
    JwtUtil.validateToken()
        ├─ 验证JWT签名
        ├─ 检查JWT过期时间
        └─ 检查Redis活动记录
            ↓
        验证成功？
            ├─ 是 → updateTokenActivity() → 继续请求
            └─ 否 → 返回401
```

## 影响范围

### 前端需要的改动
1. 所有API请求都必须携带有效的JWT Token
2. 处理401错误，提示用户重新登录
3. 建议在401错误时清除本地存储的Token并跳转到登录页

### 后端改动
- ✓ JWT工具类
- ✓ 认证过滤器
- ✓ Security配置
- ✓ 新增认证失败处理器
- ✓ 单元测试

## 向后兼容性

- ✓ 原有的登录接口不受影响
- ✓ Token生成和验证的接口保持一致
- ✓ 原有通过认证的请求继续正常工作
- ⚠️ 之前可以匿名访问的接口现在需要认证（这是修复Bug的预期行为）

## 监控建议

建议监控以下指标：
1. Redis中Token活动记录的数量（活跃用户数）
2. Token过期导致的401错误频率
3. Redis内存使用情况

## 总结

本次实现完整解决了问题描述中的两个需求：
1. ✅ Token在24小时无操作后自动过期
2. ✅ 所有接口都需要有效认证，登录状态失效时阻止访问并返回明确错误消息
