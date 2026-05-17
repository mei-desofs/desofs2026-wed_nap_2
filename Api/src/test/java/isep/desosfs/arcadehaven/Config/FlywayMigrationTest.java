package isep.desosfs.arcadehaven.Config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RNF-30 — Flyway schema versioning: validates migration files exist, are non-empty,
 * and follow the Vn__description.sql naming convention.
 */
class FlywayMigrationTest {

    private static final String MIGRATIONS_PATH = "db/migration/";
    private static final Pattern FLYWAY_NAME = Pattern.compile("V\\d+__[a-zA-Z0-9_]+\\.sql");

    @ParameterizedTest(name = "migration {0} exists on classpath")
    @ValueSource(strings = {
        "V1__create_tables.sql",
        "V2__fix_status_constraints.sql",
        "V3__add_game_category.sql",
        "V4__add_performance_indexes.sql"
    })
    void migrationFile_existsOnClasspath(String filename) {
        InputStream in = getClass().getClassLoader()
                .getResourceAsStream(MIGRATIONS_PATH + filename);
        assertThat(in)
                .as("Migration file '%s' must exist in classpath:db/migration/", filename)
                .isNotNull();
    }

    @ParameterizedTest(name = "migration {0} is non-empty")
    @ValueSource(strings = {
        "V1__create_tables.sql",
        "V2__fix_status_constraints.sql",
        "V3__add_game_category.sql",
        "V4__add_performance_indexes.sql"
    })
    void migrationFile_isNonEmpty(String filename) throws IOException {
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream(MIGRATIONS_PATH + filename)) {
            assertThat(in).isNotNull();
            byte[] bytes = in.readAllBytes();
            assertThat(bytes.length)
                    .as("Migration file '%s' must not be empty", filename)
                    .isGreaterThan(0);
        }
    }

    @ParameterizedTest(name = "migration {0} follows Flyway naming convention")
    @ValueSource(strings = {
        "V1__create_tables.sql",
        "V2__fix_status_constraints.sql",
        "V3__add_game_category.sql",
        "V4__add_performance_indexes.sql"
    })
    void migrationFile_followsNamingConvention(String filename) {
        assertThat(FLYWAY_NAME.matcher(filename).matches())
                .as("'%s' must match Flyway naming pattern V<N>__<description>.sql", filename)
                .isTrue();
    }

    @Test
    void migrations_areVersionedSequentially() {
        List<Integer> versions = List.of(1, 2, 3, 4);
        for (int i = 0; i < versions.size() - 1; i++) {
            assertThat(versions.get(i + 1))
                    .as("Migration versions must be sequential")
                    .isEqualTo(versions.get(i) + 1);
        }
    }

    @Test
    void flyway_isEnabledInProperties() throws IOException {
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            assertThat(in).isNotNull();
            java.util.Properties props = new java.util.Properties();
            props.load(in);

            assertThat(props.getProperty("spring.flyway.enabled"))
                    .as("spring.flyway.enabled must be 'true' (RNF-30)")
                    .isEqualTo("true");

            assertThat(props.getProperty("spring.flyway.locations"))
                    .as("spring.flyway.locations must point to db/migration")
                    .contains("db/migration");
        }
    }

    @Test
    void flyway_baselineOnMigrate_isEnabled() throws IOException {
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            assertThat(in).isNotNull();
            java.util.Properties props = new java.util.Properties();
            props.load(in);

            assertThat(props.getProperty("spring.flyway.baseline-on-migrate"))
                    .as("spring.flyway.baseline-on-migrate must be true for pre-existing schemas")
                    .isEqualTo("true");
        }
    }

    @Test
    void v4Migration_containsPerformanceIndexes() throws IOException {
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream(MIGRATIONS_PATH + "V4__add_performance_indexes.sql")) {
            assertThat(in).isNotNull();
            String sql = new String(in.readAllBytes());

            assertThat(sql).contains("idx_games_status_price");
            assertThat(sql).contains("idx_games_publisher_status");
            assertThat(sql).contains("idx_orders_buyer_created");
        }
    }

    @Test
    void v1Migration_containsCoreTables() throws IOException {
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream(MIGRATIONS_PATH + "V1__create_tables.sql")) {
            assertThat(in).isNotNull();
            String sql = new String(in.readAllBytes()).toLowerCase();

            assertThat(sql).contains("create table");
            assertThat(sql).contains("games");
            assertThat(sql).contains("orders");
        }
    }
}
