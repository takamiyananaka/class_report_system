# ID数据类型变更总结

## 变更概述

按照需求，已将所有模块的ID数据类型从 `Long/BIGINT` 改为 `String/VARCHAR(50)`。

## 详细变更清单

### 1. 数据库变更

#### 1.1 完整建表脚本 (complete_schema.sql)
- 版本更新为 v6.0
- 所有表的ID字段：`BIGINT` → `VARCHAR(50)`
- 外键字段：
  - `course_schedule.teacher_no`: `BIGINT` → `VARCHAR(50)`
  - `course.teacher_no`: `BIGINT` → `VARCHAR(50)`
  - `attendance.course_id`: `BIGINT` → `VARCHAR(50)`
  - `alert.course_id`: `BIGINT` → `VARCHAR(50)`
  - `alert.attendance_id`: `BIGINT` → `VARCHAR(50)`

#### 1.2 迁移脚本 (migrate_id_to_string.sql)
- **新增文件**：支持现有数据库自动迁移
- 功能特点：
  - 使用 ALTER TABLE 修改现有表结构
  - 自动转换现有数据
  - 重建所有受影响的索引
  - 可在有数据的情况下直接运行
  - 无需手动添加字段

### 2. Java代码变更

#### 2.1 实体类 (domain包)
所有实体类的ID字段已更新：
- `Admin.java`: `Long id` → `String id`
- `Teacher.java`: `Long id` → `String id`
- `Class.java`: `Long id` → `String id`
- `Course.java`: `Long id` → `String id` + `Long teacherNo` → `String teacherNo`
- `CourseSchedule.java`: `Long id` → `String id` + `Long teacherNo` → `String teacherNo`
- `Attendance.java`: `Long id` → `String id` + `Long courseId` → `String courseId`
- `Alert.java`: `Long id` → `String id` + `Long courseId/attendanceId` → `String courseId/attendanceId`
- `ImageCapture.java`: `Long id` → `String id`

#### 2.2 VO类 (vo包)
- `AdminVO.java`: `Long id` → `String id`
- `TeacherVO.java`: `Long id` → `String id`

#### 2.3 DTO类 (dto包)
- `CourseScheduleQueryDTO.java`: `Long teacherNo` → `String teacherNo`

#### 2.4 Service接口和实现
- `AttendanceService.java`: 所有方法参数 `Long courseId` → `String courseId`
- `AttendanceServiceImpl.java`: 实现方法参数同步更新
- `CourseScheduleService.java`: `importFromExcel` 方法参数 `Long teacherNo` → `String teacherNo`
- `CourseScheduleServiceImpl.java`: 实现方法参数同步更新

#### 2.5 Controller层
更新了所有控制器中的 `@PathVariable` 和 `@RequestParam` 参数类型：

**AttendanceController.java**:
- `queryAttendanceByCourseId(Long courseId)` → `queryAttendanceByCourseId(String courseId)`
- `manualAttendance(Long courseId)` → `manualAttendance(String courseId)`
- `queryCurrentAttendance(Long courseId)` → `queryCurrentAttendance(String courseId)`
- `getAttendanceById(Long id)` → `getAttendanceById(String id)`
- `updateAttendanceById(Long id, ...)` → `updateAttendanceById(String id, ...)`
- `deleteAttendanceById(Long id)` → `deleteAttendanceById(String id)`

**CourseController.java**:
- `deleteCourse(Long id)` → `deleteCourse(String id)`
- `getCourse(Long id)` → `getCourse(String id)`
- `listCourses(Long teacherNo)` → `listCourses(String teacherNo)`
- 登录用户工号处理逻辑简化（不再需要Long.parseLong转换）

**ClassController.java**:
- `getClassById(Long id)` → `getClassById(String id)`
- `updateClassById(Long id, ...)` → `updateClassById(String id, ...)`
- `deleteClassById(Long id)` → `deleteClassById(String id)`

**CourseScheduleController.java**:
- `getCourseScheduleById(Long id)` → `getCourseScheduleById(String id)`
- `updateCourseScheduleById(Long id, ...)` → `updateCourseScheduleById(String id, ...)`
- `deleteCourseScheduleById(Long id)` → `deleteCourseScheduleById(String id)`
- `importFromExcel` 方法中的 `Long teacherNo` → `String teacherNo`
- 所有内部teacherNo处理逻辑简化

**AdminController.java**:
- `getTeacher(Long id)` → `getTeacher(String id)`
- `updateTeacher(Long id, ...)` → `updateTeacher(String id, ...)`
- `deleteTeacher(Long id)` → `deleteTeacher(String id)`

#### 2.6 工具类和过滤器
**JwtUtil.java**:
- `generateToken(Long userId, ...)` → `generateToken(String userId, ...)`
- `getUserIdFromToken(String token)`: 返回类型 `Long` → `String`
- 内部处理：不再使用 `String.valueOf()` 和 `Long.parseLong()`

**JwtAuthenticationFilter.java**:
- `Long userId` → `String userId`
- 简化了principal的处理逻辑

### 3. 文档变更

#### 3.1 数据库迁移指南 (DATABASE_MIGRATION.md)
新增完整的迁移文档，包含：
- 变更概述
- 迁移方案说明（全新安装 vs 现有数据库迁移）
- 详细执行步骤
- 故障排除指南
- 测试建议

#### 3.2 变更总结 (本文件)
完整记录所有变更内容，便于追溯和审查。

## 技术影响分析

### 优势
1. **灵活性提升**：支持各种格式的ID（数字、UUID、雪花ID等）
2. **扩展性增强**：未来可以使用更复杂的ID生成策略
3. **类型一致性**：教师工号已经是String，现在ID也统一为String
4. **代码简化**：减少了类型转换代码

### 注意事项
1. **索引性能**：VARCHAR索引略慢于BIGINT，但影响可忽略
2. **存储空间**：VARCHAR(50)占用更多空间，但可接受
3. **兼容性**：现有数据会自动转换，无需手动处理

## 编译和测试结果

✅ **编译成功**: Maven编译通过，无错误
✅ **代码审查**: 通过静态代码分析，无问题
✅ **安全扫描**: CodeQL扫描通过，无安全漏洞

## 使用说明

### 全新部署
直接使用更新后的 `complete_schema.sql` 建表即可。

### 现有系统升级
1. 备份数据库
2. 运行 `migrate_id_to_string.sql` 迁移脚本
3. 重启应用
4. 验证功能正常

详细步骤请参考 `DATABASE_MIGRATION.md`。

## 版本信息

- 数据库版本：v5.0 → v6.0
- 变更日期：2024-12-14
- 影响范围：全部数据表和相关Java代码
