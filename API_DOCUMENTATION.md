# API接口文档

## 基础信息

- **Base URL**: `http://localhost:8080/api`
- **认证方式**: JWT Token (除登录接口外，其他接口都需要在Header中携带Token)
- **Header格式**: `Authorization: Bearer {token}`

## 认证相关接口

### 1. 管理员登录

**接口**: `POST /admin/auth/login`

**请求体**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
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

### 2. 教师登录

**接口**: `POST /teacher/auth/login`

**请求体**:
```json
{
  "username": "teacher001",
  "password": "teacher123"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
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
      "identity": 1,
      "status": 1
    }
  }
}
```

## 教师管理接口

### 3. 根据ID查询教师

**接口**: `GET /teacher/{id}`

**路径参数**:
- `id`: 教师工号

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "teacher001",
    "realName": "张老师",
    "teacherNo": "T001",
    "phone": "13900139000",
    "email": "teacher@example.com",
    "department": "计算机学院",
    "identity": 1,
    "status": 1,
    "createTime": "2024-01-01T00:00:00",
    "updateTime": "2024-01-01T00:00:00"
  }
}
```

## 课程管理接口

### 4. Excel导入课程

**接口**: `POST /course/import`

**请求方式**: multipart/form-data

**请求参数**:
- `file`: Excel文件 (必填)

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "count": 10,
    "message": "成功导入10条课程数据"
  }
}
```

详细说明请参考: [Excel导入指南](./EXCEL_IMPORT_GUIDE.md)

### 5. 根据ID查询课程

**接口**: `GET /course/{id}`

**路径参数**:
- `id`: 课程ID

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "courseName": "高等数学",
    "courseCode": "MATH101",
    "teacherNo": 1,
    "classroom": "A101",
    "courseTime": "周一 1-2节",
    "courseDate": "2024-09-01",
    "startTime": "08:00:00",
    "endTime": "10:00:00",
    "weekDay": 1,
    "expectedCount": 50,
    "semester": "2024-2025-1",
    "status": 2,
    "remark": "重点课程",
    "createTime": "2024-01-01T00:00:00",
    "updateTime": "2024-01-01T00:00:00",
    "isDeleted": 0
  }
}
```

### 6. 查询所有课程

**接口**: `GET /course/list`

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "courseName": "高等数学",
      "courseCode": "MATH101",
      "teacherNo": 1,
      ...
    },
    {
      "id": 2,
      "courseName": "大学英语",
      "courseCode": "ENG101",
      "teacherNo": 2,
      ...
    }
  ]
}
```

## 课表管理接口

### 7. 创建课表

**接口**: `POST /course-schedule`

**请求体**:
```json
{
  "courseName": "高等数学",
  "teacherNo": 1,
  "className": "计算机2024-1班",
  "weekday": 1,
  "startTime": "08:00:00",
  "endTime": "10:00:00",
  "classroom": "A101",
  "semester": "2024-2025-1",
  "schoolYear": "2024-2025"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "courseName": "高等数学",
    "teacherNo": 1,
    "className": "计算机2024-1班",
    "weekday": 1,
    "startTime": "08:00:00",
    "endTime": "10:00:00",
    "classroom": "A101",
    "semester": "2024-2025-1",
    "schoolYear": "2024-2025",
    "createTime": "2024-01-01T00:00:00",
    "updateTime": "2024-01-01T00:00:00"
  }
}
```

### 8. 根据ID查询课表

**接口**: `GET /course-schedule/{id}`

**路径参数**:
- `id`: 课表ID

### 9. 查询课表列表（分页）

**接口**: `GET /course-schedule`

**查询参数**:
- `courseName`: 课程名称（可选）
- `teacherNo`: 教师工号（可选）
- `className`: 班级名称（可选）
- `weekday`: 星期几（可选，1-7）
- `semester`: 学期（可选）
- `schoolYear`: 学年（可选）
- `page`: 页码（默认1）
- `size`: 每页数量（默认10）

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [...],
    "total": 100,
    "page": 1,
    "size": 10,
    "pages": 10
  }
}
```

### 10. 根据教师工号查询课表

**接口**: `GET /course-schedule/teacher/{teacherNo}`

**路径参数**:
- `teacherNo`: 教师工号

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "courseName": "高等数学",
      "teacherNo": 1,
      "className": "计算机2024-1班",
      "weekday": 1,
      ...
    }
  ]
}
```

### 11. 根据班级名称查询课表

**接口**: `GET /course-schedule/class/{className}`

**路径参数**:
- `className`: 班级名称

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "courseName": "高等数学",
      "teacherNo": 1,
      "className": "计算机2024-1班",
      "weekday": 1,
      ...
    }
  ]
}
```

### 12. 更新课表

**接口**: `PUT /course-schedule/{id}`

**路径参数**:
- `id`: 课表ID

**请求体**: 同创建课表

### 13. 删除课表

**接口**: `DELETE /course-schedule/{id}`

**路径参数**:
- `id`: 课表ID

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（未登录或token过期） |
| 403 | 禁止访问（无权限） |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 使用示例

### Curl示例

```bash
# 管理员登录
curl -X POST "http://localhost:8080/api/admin/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 查询教师信息（需要token）
curl -X GET "http://localhost:8080/api/teacher/1" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Excel导入课程
curl -X POST "http://localhost:8080/api/course/import" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@courses.xlsx"
```

### Postman示例

1. **设置环境变量**
   - 创建环境变量 `base_url`: `http://localhost:8080/api`
   - 创建环境变量 `token`: 登录后获取的token

2. **配置Headers**
   - Key: `Authorization`
   - Value: `Bearer {{token}}`

3. **发送请求**
   - 选择对应的HTTP方法
   - 输入完整URL或使用 `{{base_url}}/path`
   - 对于JSON请求，在Body中选择raw -> JSON
   - 对于文件上传，在Body中选择form-data

## 技术栈

- Spring Boot 3.5.7
- MyBatis-Plus 3.5.3.1
- Spring Security + JWT
- Apache POI (Excel处理)
- MySQL 8.x
- Redis
