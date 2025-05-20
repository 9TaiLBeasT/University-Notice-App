import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBConnection {
    private static HikariDataSource dataSource;

    private static void initDataSource() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:postgresql://aws-0-ap-south-1.pooler.supabase.com:6543/postgres");
            config.setUsername("postgres.qcfslaprrbxxefmigefe");
            config.setPassword("Ganesh123@");

            config.setMaximumPoolSize(1);
            config.setMinimumIdle(0);
            config.setIdleTimeout(60000);
            config.setConnectionTimeout(30000);
            config.setMaxLifetime(1800000);

            dataSource = new HikariDataSource(config);
        }
    }

    public static Connection getConnection() throws SQLException {
        initDataSource();
        return dataSource.getConnection();
    }
}
