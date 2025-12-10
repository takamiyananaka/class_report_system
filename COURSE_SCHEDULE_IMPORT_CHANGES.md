# 课表导入功能修改说明

## 变更概述

根据需求，对课表Excel导入功能进行了以下修改：

1. **移除Excel中的教师ID列**：课表导入不再需要在Excel中包含教师ID
2. **自动填充教师ID**：系统根据当前登录教师身份自动填充教师ID
3. **更新Excel模板**：下载的Excel模板不再包含教师ID列

## 修改的文件

### 1. CourseScheduleExcelDTO.java
**位置**: `src/main/java/com/xuegongbu/dto/CourseScheduleExcelDTO.java`

**变更**:
- 移除了 `teacherId` 字段
- 调整了其他字段的 `@ExcelProperty` index，从1开始递增

**修改前的Excel列顺序**:
```
课程名称(0) | 教师ID(1) | 班级名称(2) | 星期几(3) | ...
```

**修改后的Excel列顺序**:
```
课程名称(0) | 班级名称(1) | 星期几(2) | 开始时间(3) | ...
```

### 2. CourseScheduleService.java
**位置**: `src/main/java/com/xuegongbu/service/CourseScheduleService.java`

**变更**:
- `importFromExcel` 方法增加了 `Long teacherId` 参数
- 方法签名从 `importFromExcel(MultipartFile file)` 改为 `importFromExcel(MultipartFile file, Long teacherId)`

### 3. CourseScheduleServiceImpl.java
**位置**: `src/main/java/com/xuegongbu/service/impl/CourseScheduleServiceImpl.java`

**变更**:
1. 实现了新的方法签名，接收 `teacherId` 参数
2. 添加了 `teacherId` 空值校验
3. 移除了对Excel中 `teacherId` 字段的验证
4. 在创建 `CourseSchedule` 对象时，使用传入的 `teacherId` 参数而不是从Excel读取
5. 更新了 `downloadTemplate()` 方法，生成的模板示例不再包含 `teacherId`

**关键代码变更**:
```java
// 新增参数验证
if (teacherId == null) {
    throw new IllegalArgumentException("教师ID不能为空");
}

// 移除了这部分验证
// if (dto.getTeacherId() == null) {
//     errorMessages.add(String.format("第%d行：教师ID不能为空", i + 2));
//     failCount++;
//     continue;
// }

// 使用传入的teacherId而不是从Excel读取
courseSchedule.setTeacherId(teacherId); // 使用当前登录教师的ID
```

### 4. CourseScheduleController.java
**位置**: `src/main/java/com/xuegongbu/controller/CourseScheduleController.java`

**变更**:
1. 从Spring Security上下文中获取当前登录教师的ID
2. 添加了登录状态验证
3. 将教师ID传递给服务层

**关键代码**:
```java
// 获取当前登录教师的ID
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
if (authentication == null || authentication.getPrincipal() == null) {
    return Result.error("未登录或登录已过期，请重新登录");
}

Long teacherId = null;
try {
    Object principal = authentication.getPrincipal();
    if (principal instanceof Long) {
        teacherId = (Long) principal;
    } else if (principal instanceof String) {
        teacherId = Long.parseLong((String) principal);
    }
} catch (NumberFormatException e) {
    log.error("无法解析当前登录教师ID: {}", e.getMessage());
    return Result.error("无法获取当前登录用户信息");
}

if (teacherId == null) {
    return Result.error("无法获取当前登录用户信息");
}

// 调用服务层方法
Map<String, Object> result = courseScheduleService.importFromExcel(file, teacherId);
```

### 5. COURSE_SCHEDULE_IMPORT.md
**位置**: `COURSE_SCHEDULE_IMPORT.md`

**变更**:
- 更新了Excel列顺序说明，移除了教师ID列
- 更新了Excel示例，不再包含教师ID
- 更新了数据验证规则，移除了教师ID的验证
- 添加了必须登录的说明
- 更新了错误处理说明
- 添加了版本2.0.0的更新日志

## 新Excel格式

### 列顺序

| 列序号 | 列名 | 是否必填 | 数据类型 | 说明 | 示例 |
|--------|------|----------|----------|------|------|
| 1 | 课程名称 | 是 | 文本 | 课程的名称 | 高等数学 |
| 2 | 班级名称 | 是 | 文本 | 上课班级名称 | 25计算机类-1班 |
| 3 | 星期几 | 是 | 数字(1-7) | 1=周一，7=周日 | 1 |
| 4 | 开始时间 | 是 | 时间 | 课程开始时间 | 08:00 |
| 5 | 结束时间 | 是 | 时间 | 课程结束时间 | 09:40 |
| 6 | 教室 | 是 | 文本 | 上课教室 | 思学楼A101 |
| 7 | 学期 | 是 | 文本 | 学期信息 | 1 |
| 8 | 学年 | 是 | 文本 | 学年信息 | 2024-2025 |

### Excel示例
```
课程名称    | 班级名称           | 星期几 | 开始时间 | 结束时间 | 教室         | 学期 | 学年
高等数学    | 25计算机类-1班     | 1      | 08:00    | 09:40    | 思学楼A101   | 1    | 2024-2025
大学英语    | 25计算机类-1班     | 2      | 10:00    | 11:40    | 思学楼B202   | 1    | 2024-2025
数据结构    | 25计算机类-2班     | 3      | 14:00    | 15:40    | 思学楼C303   | 1    | 2024-2025
```

## 使用方式

### 前提条件
**必须先登录**: 教师必须使用自己的账号登录系统，获取有效的JWT Token

### API调用示例

```bash
curl -X POST "http://localhost:8080/courseSchedule/import" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@course_schedule.xlsx"
```

### 响应示例

**成功响应**:
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

**未登录错误**:
```json
{
  "code": 500,
  "message": "未登录或登录已过期，请重新登录",
  "data": null
}
```

## 安全性改进

### 1. 身份验证
- 必须登录才能导入课表
- 系统会验证JWT Token的有效性

### 2. 权限控制
- 教师只能导入自己的课表
- 无法通过修改Excel来导入其他教师的课表
- 防止了越权操作

### 3. 数据一致性
- 所有导入的课表记录都会自动关联到当前登录教师
- 确保了数据的一致性和准确性

## 兼容性说明

### 向后不兼容
⚠️ **重要**: 此次修改不向后兼容

- **旧版Excel文件无法直接使用**: 包含教师ID列的旧Excel文件将导致列对齐错误
- **需要重新下载模板**: 用户需要使用新的Excel模板
- **列索引已变更**: 所有列的索引位置都发生了变化

### 迁移建议
1. 通知所有用户此次变更
2. 提供新的Excel模板下载
3. 在UI上添加提示信息，说明需要使用新模板

## 测试建议

### 功能测试
1. **正常导入测试**: 使用新模板导入课表，验证数据正确性
2. **未登录测试**: 不带Token调用接口，应返回错误
3. **Token过期测试**: 使用过期Token，应返回错误
4. **模板下载测试**: 下载新模板，验证不包含教师ID列

### 安全测试
1. **越权测试**: 尝试使用其他教师的Token，验证只能导入自己的课表
2. **参数篡改测试**: 验证无法通过修改请求参数来导入其他教师的课表

### 集成测试
1. **完整流程测试**: 登录 -> 下载模板 -> 填写数据 -> 导入 -> 查询验证
2. **批量导入测试**: 导入大量数据，验证性能和事务正确性

## 维护注意事项

1. **日志记录**: 所有导入操作都会记录教师ID，便于审计
2. **错误处理**: 保持了原有的错误处理机制，只是移除了对教师ID的验证
3. **事务管理**: 导入操作仍然在事务中执行，确保数据一致性

## 版本信息

- **修改日期**: 2025-12-10
- **版本**: v2.0.0
- **修改原因**: 简化导入流程，提高安全性
- **影响范围**: 课表导入相关功能
