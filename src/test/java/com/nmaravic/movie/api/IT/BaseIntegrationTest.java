package com.nmaravic.movie.api.IT;
;
import com.nmaravic.movie.api.database.repository.MovieImageRepository;
import com.nmaravic.movie.api.database.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    private static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");
    private static final SimpleGrantedAuthority USER = new SimpleGrantedAuthority("ROLE_USER");

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected MovieRepository movieRepository;

    @Autowired
    protected MovieImageRepository movieImageRepository;

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("movie_test")
            .withUsername("test")
            .withPassword("test");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    static MockHttpServletRequestBuilder withAdmin(MockHttpServletRequestBuilder builder) {
        return builder.with(jwt().authorities(ADMIN));
    }

    static MockMultipartHttpServletRequestBuilder withAdmin(MockMultipartHttpServletRequestBuilder builder) {
        return builder.with(jwt().authorities(ADMIN));
    }

    static MockHttpServletRequestBuilder withUser(MockHttpServletRequestBuilder builder) {
        return builder.with(jwt().authorities(USER));
    }
}