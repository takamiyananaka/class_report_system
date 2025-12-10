# 课表Excel导入功能文档

## 功能概述

本系统提供了课表(CourseSchedule)的Excel批量导入功能，通过上传Excel文件快速批量创建课表数据。

## API接口信息

### 接口地址
```
POST /api/courseSchedule/import
```

### 请求方式
`multipart/form-data`

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | Excel文件（.xlsx或.xls格式） |

**注意**: 教师ID将自动从当前登录用户获取，无需在Excel中提供。

## Excel文件格式要求

### 文件格式
- 支持 `.xlsx` 和 `.xls` 格式
- 第一行必须是表头（会被跳过）
- 从第二行开始是实际数据

### 列顺序和说明

| 列序号 | 列名 | 是否必填 | 数据类型 | 说明 | 示例 |
|--------|------|----------|----------|------|------|
| 1 | 课程名称 | 是 | 文本 | 课程的名称 | 高等数学 |
| 2 | 班级名称 | 是 | 文本 | 上课班级名称 | 25计算机类-1班 |
| 3 | 星期几 | 是 | 数字(1-7) | 1=周一，7=周日 | 1 |
| 4 | 开始时间 | 是 | 时间 | 课程开始时间(支持HH:mm、HH:mm:ss、H:mm、H:mm:ss格式) | 08:00 或 8:00 |
| 5 | 结束时间 | 是 | 时间 | 课程结束时间(支持HH:mm、HH:mm:ss、H:mm、H:mm:ss格式) | 10:00 或 10:00:00 |
| 6 | 教室 | 是 | 文本 | 上课教室 | 思学楼A101 |
| 7 | 学期 | 是 | 文本 | 学期信息 | 1 |
| 8 | 学年 | 是 | 文本 | 学年信息 | 2024-2025 |

**注意**: 教师ID已从Excel格式中移除，系统会自动使用当前登录教师的ID。

### Excel示例

```
课程名称    | 班级名称           | 星期几 | 开始时间 | 结束时间 | 教室         | 学期 | 学年
高等数学    | 25计算机类-1班     | 1      | 08:00    | 09:40    | 思学楼A101   | 1    | 2024-2025
大学英语    | 25计算机类-1班     | 2      | 10:00    | 11:40    | 思学楼B202   | 1    | 2024-2025
数据结构    | 25计算机类-2班     | 3      | 14:00    | 15:40    | 思学楼C303   | 1    | 2024-2025
```

## 使用方法

### 使用curl命令

```bash
curl -X POST "http://localhost:8080/api/courseSchedule/import" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@course_schedule.xlsx"
```

### 使用Postman

1. 选择 `POST` 方法
2. 输入URL: `http://localhost:8080/api/courseSchedule/import`
3. 在 `Body` 选项卡中选择 `form-data`
4. 添加字段:
   - Key: `file` (类型选择 `File`)
   - Value: 选择你的Excel文件
5. 点击 `Send`

### 使用JavaScript (Fetch API)

```javascript
const fileInput = document.querySelector('input[type="file"]');
const formData = new FormData();
formData.append('file', fileInput.files[0]);

fetch('http://localhost:8080/api/courseSchedule/import', {
  method: 'POST',
  body: formData
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));
```

## 响应格式

### 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "successCount": 3,
    "failCount": 0,
    "totalCount": 3,
    "message": "成功导入3条课表数据，失败0条"
  }
}
```

### 部分成功响应（有数据验证失败）

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "successCount": 2,
    "failCount": 1,
    "totalCount": 3,
    "message": "成功导入2条课表数据，失败1条",
    "errors": [
      "第3行：教师ID不能为空"
    ]
  }
}
```

### 失败响应

```json
{
  "code": 500,
  "message": "导入失败: 文件格式不正确，只支持.xlsx或.xls格式",
  "data": null
}
```

## 数据验证规则

系统会对每行数据进行以下验证：

1. **课程名称**: 不能为空，最大长度100字符
2. **班级名称**: 不能为空，最大长度100字符
3. **星期几**: 必须是1-7之间的整数
4. **开始时间**: 不能为空，格式必须为HH:mm或HH:mm:ss
5. **结束时间**: 不能为空，格式必须为HH:mm或HH:mm:ss
6. **教室**: 不能为空，最大长度100字符
7. **学期**: 不能为空，最大长度50字符
8. **学年**: 不能为空，最大长度20字符

**注意**: 教师ID将自动从当前登录用户获取，无需在Excel中提供。

如果某行数据不符合验证规则，该行会被跳过，继续处理其他行，最终在响应中返回错误详情。

## 注意事项

1. **必须登录**: 导入课表前必须使用教师账号登录，系统会自动使用当前登录教师的ID
2. **教师ID自动填充**: 教师ID不再需要在Excel中提供，系统会根据当前登录用户自动填充
3. **时间格式**: 支持多种时间格式：`HH:mm`、`HH:mm:ss`、`H:mm`、`H:mm:ss`，如 `08:00`、`08:00:00`、`8:00`、`8:00:00`
4. **事务处理**: 导入操作在数据库事务中执行，如果出现数据库错误会全部回滚
5. **批量处理**: 支持批量导入，建议每次不超过1000条数据
6. **编码格式**: Excel文件建议使用UTF-8编码，确保中文正确显示
7. **文件大小**: 建议单个文件不超过10MB

## 常见错误及解决方案

| 错误信息 | 原因 | 解决方案 |
|----------|------|----------|
| 未登录或登录已过期 | 没有登录或Token过期 | 使用教师账号登录 |
| 教师ID不能为空 | 无法获取当前登录用户信息 | 确保使用有效的登录Token |
| 文件不能为空 | 未上传文件 | 确保选择了Excel文件 |
| 文件格式不正确 | 文件不是.xlsx或.xls格式 | 使用Excel格式的文件 |
| Excel文件中没有数据 | Excel只有表头没有数据行 | 在Excel中添加数据行 |
| 第X行：课程名称不能为空 | 该行课程名称列为空 | 填写课程名称 |
| 第X行：星期几必须是1-7之间的数字 | 星期几不在1-7范围内 | 填写1-7之间的数字 |
| 第X行：时间格式不正确 | 时间格式不符合支持的格式 | 使用HH:mm、HH:mm:ss、H:mm或H:mm:ss格式 |
| Could not open JDBC Connection | 数据库连接失败 | 检查数据库配置和连接 |

## 技术实现

- **解析库**: 使用阿里巴巴的 EasyExcel (版本4.0.3) 进行Excel解析
- **持久化**: 使用 MyBatis-Plus 进行批量插入
- **事务管理**: 使用 Spring 的 `@Transactional` 注解确保数据一致性
- **异常处理**: 完整的异常捕获和日志记录
- **数据验证**: 严格的字段验证和错误提示

## 代码示例

### Python示例（使用requests库）

```python
import requests

url = 'http://localhost:8080/api/courseSchedule/import'
files = {'file': open('course_schedule.xlsx', 'rb')}

response = requests.post(url, files=files)
print(response.json())
```

### Java示例（使用RestTemplate）

```java
RestTemplate restTemplate = new RestTemplate();
HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.MULTIPART_FORM_DATA);

MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
body.add("file", new FileSystemResource("course_schedule.xlsx"));

HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

ResponseEntity<Map> response = restTemplate.postForEntity(
    "http://localhost:8080/api/courseSchedule/import",
    requestEntity,
    Map.class
);

System.out.println(response.getBody());
```

## 更新日志

### v2.0.0 (2025-12-10)
- **重要变更**: 移除Excel中的教师ID列
- 教师ID现在自动从当前登录用户获取
- 简化了Excel模板格式
- 增强了安全性（教师只能导入自己的课表）

### v1.0.0 (2025-12-09)
- 初始版本
- 实现Excel批量导入课表功能
- 支持完整的数据验证和错误提示
- 事务性批量插入数据
