import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBConnection {
    private static final HikariDataSource ds;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://aws-0-ap-south-1.pooler.supabase.com:6543/postgres");
        config.setUsername("postgres.qcfslaprrbxxefmigefe");
        config.setPassword("Ganesh123@");
        config.setMaximumPoolSize(5); // Safe for Supabase free tier
        config.setMinimumIdle(1);
        config.setIdleTimeout(30000); // 30 seconds
        config.setMaxLifetime(60000); // 1 minute
        ds = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
