import java.sql.*;
import java.util.Optional;

public class UserDAO {

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("reg_number"),
                        rs.getString("department")
                );
                user.setId(rs.getString("id"));
                return Optional.of(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean insertUser(User user) {
        String sql = "INSERT INTO users (name, email, password, role, reg_number, department) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole());
            stmt.setString(5, user.getRegNumber());
            stmt.setString(6, user.getDepartment());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
