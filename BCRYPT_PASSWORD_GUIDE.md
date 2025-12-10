# BCrypt Password Guide

## Overview

This project uses **BCrypt** for password encryption throughout the entire system. All passwords stored in the database are encrypted using BCrypt hashing algorithm.

## Important Notes

⚠️ **REMEMBER**: All passwords in the database are BCrypt encrypted. When adding new users or updating passwords, you must use BCrypt to encrypt the password before storing it.

## BCrypt Configuration

### Password Encoder Bean

The BCrypt password encoder is configured in `SecurityConfig.java`:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### Usage in Login

The login logic in `TeacherServiceImpl.java` uses BCrypt to verify passwords:

```java
// BCrypt密码加密器 - 用于验证数据库中BCrypt加密的密码
@Autowired
private PasswordEncoder passwordEncoder;

// 验证密码 - 使用BCrypt验证明文密码与数据库中的加密密码
if (!passwordEncoder.matches(loginRequest.getPassword(), teacher.getPassword())) {
    throw new BusinessException("用户名或密码错误");
}
```

## Default Test Credentials

### Teacher Account
- **Username**: `teacher001`
- **Password**: `123456`
- **BCrypt Hash**: `$2a$10$IlQZy.G6fQqbVZ1dYtFW7.5VHVHEGG2Js1eH/ULU1kUxfd9E2.1kO`

## How to Generate BCrypt Hash

### Using Python
```python
import bcrypt

password = "your_password"
pwd_bytes = password.encode('utf-8')
salt = bcrypt.gensalt(rounds=10, prefix=b'2a')
hashed = bcrypt.hashpw(pwd_bytes, salt)
print(hashed.decode('utf-8'))
```

### Using Java
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String plainPassword = "your_password";
String hashedPassword = encoder.encode(plainPassword);
System.out.println(hashedPassword);
```

### Using Online Tool
You can use online BCrypt generators, but make sure to:
- Use BCrypt rounds: 10
- Verify the hash works with your implementation before storing it

## Verifying a Password

### Using Python
```python
import bcrypt

password = "123456"
hash_from_db = "$2a$10$IlQZy.G6fQqbVZ1dYtFW7.5VHVHEGG2Js1eH/ULU1kUxfd9E2.1kO"

pwd_bytes = password.encode('utf-8')
hash_bytes = hash_from_db.encode('utf-8')

matches = bcrypt.checkpw(pwd_bytes, hash_bytes)
print(f"Password matches: {matches}")
```

### Using Java
```java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
boolean matches = encoder.matches("123456", "$2a$10$IlQZy.G6fQqbVZ1dYtFW7.5VHVHEGG2Js1eH/ULU1kUxfd9E2.1kO");
System.out.println("Password matches: " + matches);
```

## Database Schema

All password fields in the database are defined as:
```sql
password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）'
```

The VARCHAR(255) length is sufficient for BCrypt hashes, which are typically 60 characters long but can vary slightly.

## Important Security Notes

1. **Never store plain text passwords** - Always use BCrypt encryption
2. **BCrypt is one-way** - You cannot decrypt a BCrypt hash back to the original password
3. **BCrypt includes salt** - Each hash is unique even for the same password
4. **BCrypt is slow by design** - This makes brute-force attacks more difficult
5. **Verify passwords using matches()** - Don't try to compare hashes directly

## Troubleshooting

### Login returns "用户名或密码错误" (Username or password incorrect)

1. Verify the password hash in the database is correct
2. Test the hash using the verification methods above
3. Ensure the BCrypt PasswordEncoder bean is properly configured
4. Check that you're using the correct plain text password

### Database errors during login

1. Ensure all required columns exist in teacher table:
   - last_login_time
   - last_login_ip
   - is_deleted
   - remark
2. Run migration_v4.sql if upgrading from an older schema

## References

- [Spring Security BCrypt Documentation](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html#authentication-password-storage-bcrypt)
- [BCrypt Algorithm](https://en.wikipedia.org/wiki/Bcrypt)
