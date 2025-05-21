import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String JDBC_URL = "jdbc:postgresql://aws-0-ap-south-1.pooler.supabase.com:5432/postgres?sslmode=require";
    private static final String USERNAME = "postgres.qcfslaprrbxxefmigefe";
    private static final String PASSWORD = "Ganesh123@";

    static {
        try {
            Class.forName("org.postgresql.Driver"); // Ensure driver is loaded
            System.out.println("✅ PostgreSQL JDBC Driver loaded.");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ PostgreSQL JDBC Driver not found.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        System.out.println("🔌 Connecting to DB with:");
        System.out.println("URL: " + JDBC_URL);
        System.out.println("USER: " + USERNAME);
        System.out.println("PASS: [set]");
        Connection conn = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        System.out.println("✅ Connection established.");
        return conn;
    }
}
