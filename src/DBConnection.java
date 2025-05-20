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

                // Load from environment
                String jdbcUrl = System.getenv("DATABASE_URL");
                if (jdbcUrl == null || jdbcUrl.isEmpty()) {
                    jdbcUrl = "jdbc:postgresql://aws-0-ap-south-1.pooler.supabase.com:6543/postgres";
                    System.out.println("⚠️ Using default JDBC URL: " + jdbcUrl);
                } else {
                    System.out.println("✅ Using environment JDBC URL: " + jdbcUrl);
                }

                String username = System.getenv("DATABASE_USERNAME");  // ✅ Fixed key
                if (username == null || username.isEmpty()) {
                    username = "postgres.qcfslaprrbxxefmigefe";
                    System.out.println("⚠️ Using default username: " + username);
                } else {
                    System.out.println("✅ Using environment username: " + username);
                }

                String password = System.getenv("DATABASE_PASSWORD");
                if (password == null || password.isEmpty()) {
                    password = "Ganesh123@";
                    System.out.println("⚠️ Using default password");
                } else {
                    System.out.println("✅ Using environment password");
                }

                config.setJdbcUrl(jdbcUrl);
                config.setUsername(username);
                config.setPassword(password);

                config.setMaximumPoolSize(5);
                config.setMinimumIdle(1);
                config.setIdleTimeout(60000);
                config.setConnectionTimeout(10000);
                config.setMaxLifetime(1800000);

                System.out.println("📌 Supabase URL: " + jdbcUrl);
                System.out.println("📌 Supabase Username: " + username);
                System.out.println("📌 Supabase Password: " + (password != null ? "[REDACTED]" : "null"));

                dataSource = new HikariDataSource(config);
                System.out.println("✅ Database connection pool initialized successfully");

                // Test connection
                try (Connection conn = dataSource.getConnection()) {
                    if (!conn.isClosed()) {
                        System.out.println("✅ Test DB connection succeeded");
                    }
                }

            } catch (Exception e) {
                System.err.println("❌ Failed to initialize database connection pool: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Database initialization failed", e);
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            initDataSource();
        }
        try {
            Connection conn = dataSource.getConnection();
            System.out.println("✅ Database connection obtained successfully");
            return conn;
        } catch (SQLException e) {
            System.err.println("❌ Failed to get database connection: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
