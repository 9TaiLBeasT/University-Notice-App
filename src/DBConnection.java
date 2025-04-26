import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Either use URL with embedded credentials:
    private static final String URL = "jdbc:postgresql://aws-0-ap-south-1.pooler.supabase.com:6543/postgres?user=postgres.qcfslaprrbxxefmigefe&password=Ganesh123@";
    private static final String USER = "postgres.qcfslaprrbxxefmigefe";
    private static final String PASSWORD = "Ganesh123@";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}