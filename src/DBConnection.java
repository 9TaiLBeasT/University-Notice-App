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

                // Get connection info from environment variables with fallbacks
                String jdbcUrl = System.getenv("DATABASE_URL");
                if (jdbcUrl == null || jdbcUrl.isEmpty()) {
                    jdbcUrl = "jdbc:postgresql://aws-0-ap-south-1.pooler.supabase.com:6543/postgres";
                    System.out.println("⚠️ Using default JDBC URL: " + jdbcUrl);
                } else {
                    System.out.println("✅ Using environment JDBC URL");
                }

                String username = System.getenv("DATABASE_USERNAME");
                if (username == null || username.isEmpty()) {
                    username = "postgres.qcfslaprrbxxefmigefe";
                    System.out.println("⚠️ Using default username");
                } else {
                    System.out.println("✅ Using environment username");
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
                config.setConnectionTimeout(30000);
                config.setMaxLifetime(1800000);

                dataSource = new HikariDataSource(config);
                System.out.println("✅ Database connection pool initialized successfully");

            } catch (Exception e) {
                System.err.println("❌ Failed to initialize database connection pool: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Database initialization failed", e);
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            if (dataSource == null) {
                initDataSource();
            }
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