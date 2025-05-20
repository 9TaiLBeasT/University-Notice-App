import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBConnection {
    private static HikariDataSource dataSource;

    private static void initDataSource() {
        if (dataSource == null) {
            try {
                System.out.println("🔌 Initializing database connection pool...");

                HikariConfig config = new HikariConfig();

                String jdbcUrl = System.getenv("DATABASE_URL");
                String username = System.getenv("DATABASE_USER");
                String password = System.getenv("DATABASE_PASSWORD");

                if (jdbcUrl == null || username == null || password == null) {
                    throw new RuntimeException("Missing DB environment variables");
                }

                config.setJdbcUrl(jdbcUrl);
                config.setUsername(username);
                config.setPassword(password);

                config.setMaximumPoolSize(5);
                config.setMinimumIdle(1);
                config.setIdleTimeout(60000);
                config.setConnectionTimeout(30000);
                config.setMaxLifetime(1800000);

                dataSource = new HikariDataSource(config);
                System.out.println("✅ Database pool initialized");
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("❌ DB initialization failed: " + e.getMessage());
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            initDataSource();
        }
        return dataSource.getConnection();
    }
}
