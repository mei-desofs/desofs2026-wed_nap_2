package isep.desosfs.arcadehaven.Config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RNF-24 — verifies that application.properties configures the Tomcat thread pool
 * and HikariCP connection pool to support 100+ concurrent users.
 */
class ConcurrencyConfigTest {

    private static Properties props;

    @BeforeAll
    static void loadProperties() throws IOException {
        props = new Properties();
        try (InputStream in = ConcurrencyConfigTest.class
                .getClassLoader().getResourceAsStream("application.properties")) {
            assertThat(in).as("application.properties must be on classpath").isNotNull();
            props.load(in);
        }
    }

    @Test
    void tomcatMaxThreads_isAtLeast100() {
        int maxThreads = Integer.parseInt(props.getProperty("server.tomcat.threads.max"));
        assertThat(maxThreads)
                .as("server.tomcat.threads.max must be >= 100 (RNF-24)")
                .isGreaterThanOrEqualTo(100);
    }

    @Test
    void tomcatMinSpareThreads_isPositive() {
        int minSpare = Integer.parseInt(props.getProperty("server.tomcat.threads.min-spare"));
        assertThat(minSpare)
                .as("server.tomcat.threads.min-spare must be > 0")
                .isGreaterThan(0);
    }

    @Test
    void tomcatConnectionTimeout_isConfigured() {
        String timeout = props.getProperty("server.tomcat.connection-timeout");
        assertThat(timeout)
                .as("server.tomcat.connection-timeout must be set")
                .isNotNull()
                .isNotEmpty();
        assertThat(Integer.parseInt(timeout))
                .as("connection-timeout must be positive")
                .isGreaterThan(0);
    }

    @Test
    void hikariMaxPoolSize_isAtLeast10() {
        int poolSize = Integer.parseInt(props.getProperty("spring.datasource.hikari.maximum-pool-size"));
        assertThat(poolSize)
                .as("spring.datasource.hikari.maximum-pool-size must be >= 10 (RNF-24)")
                .isGreaterThanOrEqualTo(10);
    }

    @Test
    void hikariMinIdle_isPositive() {
        int minIdle = Integer.parseInt(props.getProperty("spring.datasource.hikari.minimum-idle"));
        assertThat(minIdle)
                .as("spring.datasource.hikari.minimum-idle must be > 0")
                .isGreaterThan(0);
    }

    @Test
    void hikariConnectionTimeout_isConfigured() {
        String timeout = props.getProperty("spring.datasource.hikari.connection-timeout");
        assertThat(timeout)
                .as("spring.datasource.hikari.connection-timeout must be set")
                .isNotNull();
        assertThat(Long.parseLong(timeout))
                .as("hikari connection-timeout must be positive")
                .isGreaterThan(0);
    }

    @Test
    void hikariIdleTimeout_isConfigured() {
        String timeout = props.getProperty("spring.datasource.hikari.idle-timeout");
        assertThat(timeout).isNotNull();
        assertThat(Long.parseLong(timeout)).isGreaterThan(0);
    }

    @Test
    void hikariMaxLifetime_exceedsIdleTimeout() {
        long idle = Long.parseLong(props.getProperty("spring.datasource.hikari.idle-timeout"));
        long maxLife = Long.parseLong(props.getProperty("spring.datasource.hikari.max-lifetime"));
        assertThat(maxLife)
                .as("max-lifetime must exceed idle-timeout to avoid premature eviction")
                .isGreaterThan(idle);
    }
}
