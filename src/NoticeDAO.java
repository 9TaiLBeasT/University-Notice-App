import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoticeDAO {

    public void addNotice(Notice notice) {
        String sql = "INSERT INTO notices (title, content, category, is_event, event_time, file_url) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, notice.getTitle());
            stmt.setString(2, notice.getContent());
            stmt.setString(3, notice.getCategory());
            stmt.setBoolean(4, notice.isEvent());
            stmt.setTimestamp(5, notice.getEventTime());
            stmt.setString(6, notice.getFileUrl()); // ✅

            stmt.executeUpdate();
            System.out.println("✅ Notice added.");

        } catch (SQLException e) {
            System.err.println("❌ SQL Error in addNotice: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<Notice> getAllNotices() {
        return getNoticesByQuery("SELECT * FROM notices ORDER BY id DESC");
    }

    public static List<Notice> getNoticesForRole(String role) {
        String query = switch (role.toLowerCase()) {
            case "student" -> "SELECT * FROM notices WHERE category ILIKE 'student' OR category ILIKE 'general' ORDER BY id DESC";
            case "faculty" -> "SELECT * FROM notices WHERE category ILIKE 'faculty' OR category ILIKE 'general' ORDER BY id DESC";
            case "admin" -> "SELECT * FROM notices ORDER BY id DESC";
            default -> "SELECT * FROM notices WHERE category ILIKE 'general' ORDER BY id DESC";
        };
        return getNoticesByQuery(query);
    }

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
                notice.setFileUrl(rs.getString("file_url")); // ✅

                notices.add(notice);
            }

        } catch (SQLException e) {
            System.err.println("❌ SQL Error in getNoticesByQuery: " + e.getMessage());
            e.printStackTrace();
        }

        return notices;
    }

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

    public boolean updateNotice(int id, String newTitle, String newContent, String newCategory, Boolean isEvent, Timestamp eventTime, String fileUrl) {
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

        sql.append("is_event = ?, event_time = ?, file_url = ?, ");
        params.add(isEvent != null ? isEvent : false); // ✅
        params.add(eventTime); // ✅
        params.add(fileUrl); // ✅

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
