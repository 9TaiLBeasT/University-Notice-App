import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBConnection {
    private static HikariDataSource dataSource;

    private static void initDataSource() {
        if (dataSource == null) {
            try {
                System.out.println("🔌 Initializing HikariCP connection pool...");

                // Read values from environment
                String jdbcUrl = System.getenv("DATABASE_URL");
                String username = System.getenv("DATABASE_USERNAME");
                String password = System.getenv("DATABASE_PASSWORD");

                // Fallbacks for local dev
                if (jdbcUrl == null || jdbcUrl.isEmpty()) {
                    jdbcUrl = "jdbc:postgresql://aws-0-ap-south-1.pooler.supabase.com:5432/postgres?sslmode=require";
                    System.out.println("⚠️ Using fallback JDBC URL");
                }
                if (username == null || username.isEmpty()) {
                    username = "postgres.qcfslaprrbxxefmigefe";
                    System.out.println("⚠️ Using fallback username");
                }
                if (password == null || password.isEmpty()) {
                    password = "Ganesh123@";
                    System.out.println("⚠️ Using fallback password");
                }

                System.out.println("📌 JDBC URL: " + jdbcUrl);
                System.out.println("📌 Username: " + username);
                System.out.println("📌 Password: [set]");

                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(jdbcUrl);
                config.setUsername(username);
                config.setPassword(password);

                // Recommended HikariCP settings
                config.setDriverClassName("org.postgresql.Driver");
                config.setMaximumPoolSize(5);
                config.setMinimumIdle(1);
                config.setIdleTimeout(60000);         // 60s
                config.setConnectionTimeout(10000);    // 10s
                config.setMaxLifetime(1800000);        // 30 min
                config.setPoolName("NoticeAppHikariPool");

                // Optional: validate connection
                config.setInitializationFailTimeout(5000); // fail early
                config.setConnectionTestQuery("SELECT 1");

                dataSource = new HikariDataSource(config);
                System.out.println("✅ HikariCP pool initialized");

                // Eager connection test
                try (Connection conn = dataSource.getConnection()) {
                    if (!conn.isClosed()) {
                        System.out.println("✅ Test connection succeeded (HikariCP)");
                    }
                }

            } catch (Exception e) {
                System.err.println("❌ HikariCP initialization failed: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to initialize HikariCP", e);
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            initDataSource();
        }
        Connection conn = dataSource.getConnection();
        System.out.println("✅ Connection obtained via HikariCP");
        return conn;
    }
}
