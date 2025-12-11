# New API Endpoints Documentation

This document describes the new API endpoints added in this PR.

## CourseSchedule CRUD Operations (moved from Course)

### Create CourseSchedule
- **Endpoint**: `POST /courseSchedule/add`
- **Description**: Teachers create new course schedules. Teacher number is automatically obtained from login context, ID is auto-generated using snowflake algorithm.
- **Request Body**: CourseSchedule object with required fields:
  - courseName (required)
  - className (required)
  - weekday (required, 1-7)
  - startTime (required)
  - endTime (required)
  - classroom (required)
  - semester (required)
  - schoolYear (required)
- **Response**: Created CourseSchedule object

### Update CourseSchedule
- **Endpoint**: `PUT /courseSchedule/update`
- **Description**: Teachers update course schedules. Uses course name and class name to identify the schedule. Only allowed to update their own schedules.
- **Request Body**: CourseSchedule object with required fields:
  - courseName (required, used for identification)
  - className (required, used for identification)
  - Other fields to update (weekday, startTime, endTime, classroom, semester, schoolYear, etc.)
- **Authorization**: Automatically checks that the schedule belongs to the current teacher based on teacherNo from JWT
- **Response**: Success message

### Delete CourseSchedule
- **Endpoint**: `DELETE /courseSchedule/delete`
- **Description**: Teachers delete course schedules. Uses course name and class name to identify the schedule. Only allowed to delete their own schedules.
- **Query Parameters**:
  - courseName (required) - Course name
  - className (required) - Class name
- **Authorization**: Automatically checks that the schedule belongs to the current teacher
- **Response**: Success message

### Get CourseSchedule Detail
- **Endpoint**: `GET /courseSchedule/get`
- **Description**: Query course schedule details. Uses course name and class name to identify the schedule. Only allowed to view own schedules.
- **Query Parameters**:
  - courseName (required) - Course name
  - className (required) - Class name
- **Authorization**: Automatically checks that the schedule belongs to the current teacher
- **Response**: CourseSchedule object

## Teacher Management Operations

### Multi-condition Query Teachers
- **Endpoint**: `GET /teacher/query`
- **Description**: Query teachers with multiple optional conditions. Returns all teachers if no conditions provided.
- **Query Parameters** (all optional):
  - teacherNo - Teacher number (exact match)
  - department - Department name (exact match)
  - realName - Real name (fuzzy search supported)
  - phone - Phone number (exact match)
  - pageNum - Page number (default: 1)
  - pageSize - Page size (default: 10)
- **Response**: Paginated list of Teacher objects (passwords removed)

### Get Teacher by Teacher Number
- **Endpoint**: `GET /teacher/getByTeacherNo/{teacherNo}`
- **Description**: Query teacher details by teacher number
- **Path Parameter**: teacherNo - Teacher number
- **Response**: Teacher object (password removed)

### Create Teacher
- **Endpoint**: `POST /teacher/add`
- **Description**: Create new teacher. ID is auto-generated using snowflake algorithm.
- **Request Body**: Teacher object with required fields:
  - username (required)
  - password (required, will be encrypted using BCrypt)
  - realName (required)
  - teacherNo (required, must be unique)
- **Response**: Created Teacher object (password removed)

### Update Teacher
- **Endpoint**: `PUT /teacher/update`
- **Description**: Update teacher information by teacher number
- **Request Body**: Teacher object with teacherNo and fields to update
- **Notes**: If password is provided, it will be encrypted. If password is not provided, it won't be updated.
- **Response**: Success message

### Delete Teacher
- **Endpoint**: `DELETE /teacher/delete/{teacherNo}`
- **Description**: Delete teacher by teacher number (logical deletion)
- **Path Parameter**: teacherNo - Teacher number
- **Response**: Success message

## Class Management Operations

### Excel Import Classes
- **Endpoint**: `POST /class/import`
- **Description**: Batch import classes from Excel file
- **Request**: multipart/form-data with file parameter
- **Excel Format**: First row is header, columns:
  1. 班级名称 (Class Name)
  2. 辅导员工号 (Teacher Number)
  3. 班级人数 (Student Count)
- **Response**: Import result with success/fail counts and error messages

### Query Classes
- **Endpoint**: `GET /class/query`
- **Description**: Query classes with multiple optional conditions. Returns all classes if no conditions provided.
- **Query Parameters** (all optional):
  - className - Class name (fuzzy search supported)
  - teacherNo - Teacher number (exact match)
  - pageNum - Page number (default: 1)
  - pageSize - Page size (default: 10)
- **Response**: Paginated list of Class objects

### Create Class
- **Endpoint**: `POST /class/add`
- **Description**: Create new class. ID is auto-generated using snowflake algorithm.
- **Request Body**: Class object with required fields:
  - class_name (required)
  - teacher_no (required)
  - count (required, must be > 0)
- **Response**: Created Class object

### Update Class
- **Endpoint**: `PUT /class/update`
- **Description**: Update class information by class name
- **Request Body**: Class object with required field:
  - class_name (required, used for identification)
  - Other fields to update (teacher_no, count, etc.)
- **Response**: Success message

### Delete Class
- **Endpoint**: `DELETE /class/delete`
- **Description**: Delete class by class name (logical deletion)
- **Query Parameter**: 
  - className (required) - Class name
- **Response**: Success message

### Get Class Detail
- **Endpoint**: `GET /class/get`
- **Description**: Query class details by class name
- **Query Parameter**: 
  - className (required) - Class name
- **Response**: Class object

### Download Class Import Template
- **Endpoint**: `GET /class/downloadTemplate`
- **Description**: Download Excel template for class import
- **Response**: Excel file with sample data

## Authentication Notes

All endpoints (except downloadTemplate) require JWT token authentication:
- Header: `Authorization: Bearer {token}`
- Token is obtained from login endpoints
- Token contains the teacher's ID which is used for authorization checks

## Field Naming Notes

1. **Teacher Number Types**:
   - `Teacher.teacherNo` is a String (e.g., "T001")
   - `CourseSchedule.teacherNo` is a Long (stores Teacher.id)
   - `Course.teacherNo` is a Long (stores Teacher.id)
   - This is part of the existing database schema design

2. **Class Entity Fields**:
   - Uses snake_case: class_name, teacher_no, create_time, update_time, is_delete
   - This matches the database column naming convention

3. **ID Generation**:
   - All entities use snowflake algorithm (IdType.ASSIGN_ID)
   - IDs are automatically generated by MyBatis-Plus
   - **IDs are NOT used in API operations** - all CRUD operations use natural identifiers:
     - CourseSchedule: identified by courseName + className + teacherNo
     - Class: identified by className
     - Teacher: identified by teacherNo
