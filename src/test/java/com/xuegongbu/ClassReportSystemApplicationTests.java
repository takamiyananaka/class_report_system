package com.xuegongbu;

import com.xuegongbu.mapper.AdminMapper;
import com.xuegongbu.mapper.TeacherMapper;
import com.xuegongbu.utils.RedisUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ClassReportSystemApplicationTests {

    @MockBean
    private AdminMapper adminMapper;

    @MockBean
    private TeacherMapper teacherMapper;

    @MockBean
    private RedisUtils redisUtils;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    void contextLoads() {
        // 验证Spring上下文能够正确加载
    }

}
