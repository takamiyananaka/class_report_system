# API 实现文档

本文档详细说明了新实现的API接口。

## 1. 教师课程管理接口 (Teacher Course Management)

### 1.1 创建课程
- **URL**: `POST /api/course/add`
- **描述**: 教师创建新课程
- **请求体**: Course对象（JSON格式）
- **响应**: Result<Course>
- **Swagger注解**: ✅

### 1.2 更新课程
- **URL**: `PUT /api/course/update`
- **描述**: 教师更新课程信息
- **请求体**: Course对象（JSON格式）
- **响应**: Result<String>
- **Swagger注解**: ✅

### 1.3 删除课程
- **URL**: `DELETE /api/course/delete/{id}`
- **描述**: 教师删除课程
- **路径参数**: id - 课程ID
- **响应**: Result<String>
- **Swagger注解**: ✅

### 1.4 查询课程详情
- **URL**: `GET /api/course/get/{id}`
- **描述**: 根据课程ID查询课程详情
- **路径参数**: id - 课程ID
- **响应**: Result<Course>
- **Swagger注解**: ✅

### 1.5 查询教师的所有课程
- **URL**: `GET /api/course/list`
- **描述**: 查询当前登录教师的所有课程
- **查询参数**: teacherNo (可选) - 教师工号
- **响应**: Result<List<Course>>
- **Swagger注解**: ✅

## 2. 课表模板下载接口 (Excel Template Download)

### 2.1 下载课表导入模板
- **URL**: `GET /api/courseSchedule/downloadTemplate`
- **描述**: 下载Excel格式的课表导入模板文件
- **响应**: Excel文件下载
- **文件名**: 课表导入模板.xlsx
- **模板格式**:
  - 课程名称 (必填)
  - 教师工号 (必填)
  - 班级名称 (必填)
  - 星期几 1-7 (必填)
  - 开始时间 (必填, 格式: HH:mm)
  - 结束时间 (必填, 格式: HH:mm)
  - 教室 (必填)
  - 学期 (必填)
  - 学年 (必填)
- **Swagger注解**: ✅

## 3. 管理员认证接口 (Admin Authentication)

### 3.1 管理员登录
- **URL**: `POST /api/admin/login`
- **描述**: 管理员通过用户名和密码登录系统，返回JWT token
- **请求体**: LoginRequest
  ```json
  {
    "username": "admin",
    "password": "admin123"
  }
  ```
- **响应**: Result<LoginResponse>
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
        "role": "admin"
      }
    }
  }
  ```
- **功能**: BCrypt密码验证、JWT token生成、更新最后登录时间
- **Swagger注解**: ✅

### 3.2 管理员登出
- **URL**: `POST /api/admin/logout`
- **描述**: 管理员退出登录，清除认证上下文
- **响应**: Result<String>
- **功能**: 清除Spring Security上下文
- **Swagger注解**: ✅

## 4. 管理员管理教师接口 (Admin Teacher Management)

### 4.1 查询所有教师
- **URL**: `GET /api/admin/teachers`
- **描述**: 管理员查询所有教师列表
- **响应**: Result<List<TeacherVO>>
- **权限**: 需要管理员登录
- **Swagger注解**: ✅

### 4.2 根据ID查询教师
- **URL**: `GET /api/admin/teachers/{id}`
- **描述**: 管理员根据教师工号查询教师详情
- **路径参数**: id - 教师工号
- **响应**: Result<TeacherVO>
- **权限**: 需要管理员登录
- **Swagger注解**: ✅

### 4.3 创建教师
- **URL**: `POST /api/admin/teachers`
- **描述**: 管理员创建新教师
- **请求体**: TeacherRequest
  ```json
  {
    "username": "teacher001",
    "password": "123456",
    "realName": "张老师",
    "teacherNo": "T001",
    "phone": "13900139000",
    "email": "teacher@example.com",
    "department": "计算机学院",
    "status": 1
  }
  ```
- **响应**: Result<String>
- **功能**: 
  - 检查用户名和工号唯一性
  - BCrypt密码加密
  - 默认密码为"123456"（如果未提供）
  - 默认状态为1（启用）
- **权限**: 需要管理员登录
- **Swagger注解**: ✅

### 4.4 更新教师
- **URL**: `PUT /api/admin/teachers/{id}`
- **描述**: 管理员更新教师信息
- **路径参数**: id - 教师工号
- **请求体**: TeacherRequest
- **响应**: Result<String>
- **功能**: 
  - 检查用户名和工号唯一性（排除当前教师）
  - 可选更新密码（提供时才更新）
  - BCrypt密码加密
- **权限**: 需要管理员登录
- **Swagger注解**: ✅

### 4.5 删除教师
- **URL**: `DELETE /api/admin/teachers/{id}`
- **描述**: 管理员删除教师
- **路径参数**: id - 教师工号
- **响应**: Result<String>
- **功能**: 逻辑删除（使用MyBatis-Plus的@TableLogic）
- **权限**: 需要管理员登录
- **Swagger注解**: ✅

## 安全配置更新

### SecurityConfig更新
- 允许 `/admin/login` 接口无需认证访问
- `/admin/**` 其他接口需要认证
- 继续使用JWT过滤器验证token

## 代码特性

1. **统一返回格式**: 所有接口都使用 `Result<T>` 包装返回结果
2. **日志记录**: 所有接口都有详细的日志记录（使用@Slf4j）
3. **参数验证**: 使用@Valid和Jakarta Validation注解
4. **异常处理**: 使用BusinessException抛出业务异常
5. **密码加密**: 使用BCrypt加密存储和验证密码
6. **JWT认证**: 使用JWT token进行用户认证
7. **Knife4j文档**: 所有接口都有@Api和@ApiOperation注解
8. **MyBatis-Plus**: 使用MyBatis-Plus简化数据库操作
9. **逻辑删除**: 使用@TableLogic实现软删除
10. **自动填充**: 使用@TableField配置创建时间和更新时间自动填充

## 访问API文档

启动应用后，可以通过以下URL访问Knife4j文档：

- **Knife4j UI**: http://localhost:8080/api/doc.html
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api/v3/api-docs

## 测试建议

1. 先测试管理员登录接口，获取token
2. 使用token测试管理员管理教师的CRUD接口
3. 使用教师账号登录，测试课程管理接口
4. 测试课表模板下载接口
5. 验证所有接口都能在Knife4j中正常显示和调用

## 数据库要求

确保数据库中已创建以下表：
- admin (管理员表)
- teacher (教师表)
- course (课程表)
- course_schedule (课表表)

参考 `src/main/resources/db/schema.sql` 进行数据库初始化。
