package com.lune;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 端到端集成冒烟测试：拉起真实 MySQL 8.0 + Redis 7 容器，
 * 用 lune.sql 初始化 schema，验证 Spring 上下文启动、健康检查、
 * 公开接口、登录鉴权全链路。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class LuneIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("lune")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("sql/lune.sql");

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureContainers(DynamicPropertyRegistry registry) {
        // MySQL 8 caching_sha2_password 在非 SSL 连接下需要这两个参数
        registry.add("spring.datasource.url",
                () -> MYSQL.getJdbcUrl() + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai");
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointIsUp() throws Exception {
        mockMvc.perform(get("/api/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void publicArticlesEndpointReturnsOk() throws Exception {
        mockMvc.perform(get("/api/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void loginAndAdminEndpointsFlow() throws Exception {
        // 1. DataInitializer 首次启动创建的 admin 账号登录成功，返回 JWT
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"account\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();

        String token = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.token");

        // 2. 带 token 访问 admin 接口成功
        mockMvc.perform(get("/api/admin/articles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 3. 无 token 访问 admin 接口被拒绝（401/403 由 Spring Security 决定）
        mockMvc.perform(get("/api/admin/articles"))
                .andExpect(status().is4xxClientError());
    }
}
