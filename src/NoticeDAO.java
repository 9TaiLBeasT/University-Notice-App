import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoticeDAO {

    // Add a new notice to the DB
    public void addNotice(Notice notice) {
        String sql = "INSERT INTO notices (title, content, category, is_event, event_time) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, notice.getTitle());
            stmt.setString(2, notice.getContent());
            stmt.setString(3, notice.getCategory());
            stmt.setBoolean(4, notice.isEvent());
            stmt.setTimestamp(5, notice.getEventTime());

            stmt.executeUpdate();
            System.out.println("✅ Notice added.");

        } catch (SQLException e) {
            System.err.println("❌ SQL Error in addNotice: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Fetch all notices (for admin)
    public static List<Notice> getAllNotices() {
        return getNoticesByQuery("SELECT * FROM notices ORDER BY id DESC");
    }

    // Fetch notices based on user role
    public static List<Notice> getNoticesForRole(String role) {
        String query = switch (role.toLowerCase()) {
            case "student" -> "SELECT * FROM notices WHERE category ILIKE 'student' OR category ILIKE 'general' ORDER BY id DESC";
            case "faculty" -> "SELECT * FROM notices WHERE category ILIKE 'faculty' OR category ILIKE 'general' ORDER BY id DESC";
            default -> "SELECT * FROM notices WHERE category ILIKE 'general' ORDER BY id DESC";
        };

        return getNoticesByQuery(query);
    }

    // Common method to fetch and build Notice list
    private static List<Notice> getNoticesByQuery(String sql) {
        List<Notice> notices = new ArrayList<>();

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
                notice.setEvent(rs.getBoolean("is_event"));
                notice.setEventTime(rs.getTimestamp("event_time"));

                notices.add(notice);
            }

        } catch (SQLException e) {
            System.err.println("❌ SQL Error in getNoticesByQuery: " + e.getMessage());
            e.printStackTrace();
        }

        return notices;
    }

    // Delete notice by ID
    // Delete notice by ID
    public boolean deleteNotice(int id) {
        String sql = "DELETE FROM notices WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("🗑️ Notice deleted.");
                return true;
            } else {
                System.out.println("❌ No notice found with ID: " + id);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ SQL Error in deleteNotice: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    // Update notice
    public boolean updateNotice(int id, String newTitle, String newContent, String newCategory, Boolean isEvent, Timestamp eventTime) {
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

        // These must be explicitly updated even if unchanged
        sql.append("is_event = ?, event_time = ?, ");
        params.add(isEvent != null ? isEvent : false);  // default to false if null
        params.add(eventTime); // can be null

        // Final WHERE clause
        sql.setLength(sql.length() - 2); // remove trailing comma
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
                return true;
            } else {
                System.out.println("❌ No notice found with ID: " + id);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ SQL Error in updateNotice: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}
