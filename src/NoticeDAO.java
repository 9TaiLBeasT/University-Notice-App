import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoticeDAO {

    public void addNotice(Notice notice) {
        String sql = "INSERT INTO notices (title, content, category) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, notice.getTitle());
            stmt.setString(2, notice.getContent());
            stmt.setString(3, notice.getCategory());
            stmt.executeUpdate();

            System.out.println("✅ Notice added.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Notice> getAllNotices() throws SQLException {
        List<Notice> notices = new ArrayList<>();
        String sql = "SELECT * FROM notices ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Notice notice = new Notice(
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("category")
                );
                notice.setId(rs.getInt("id"));
                notice.setCreatedAt(rs.getTimestamp("created_at"));
                notices.add(notice);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notices;
    }

    public void deleteNotice(int id) {
        String sql = "DELETE FROM notices WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("🗑️ Notice deleted.");
            } else {
                System.out.println("❌ No notice found with ID: " + id);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateNotice(int id, String newTitle, String newContent, String newCategory) {
        StringBuilder sql = new StringBuilder("UPDATE notices SET ");
        List<Object> params = new ArrayList<>();

        if (newTitle != null) {
            sql.append("title = ?, ");
            params.add(newTitle);
        }
        if (newContent != null) {
            sql.append("content = ?, ");
            params.add(newContent);
        }
        if (newCategory != null) {
            sql.append("category = ?, ");
            params.add(newCategory);
        }

        if (params.isEmpty()) {
            System.out.println("❌ Nothing to update.");
            return;
        }

        sql.setLength(sql.length() - 2); // remove last comma
        sql.append(" WHERE id = ?");
        params.add(id);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Notice updated.");
            } else {
                System.out.println("❌ No notice found with ID: " + id);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
