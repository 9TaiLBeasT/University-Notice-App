import java.sql.Timestamp;

public class Notice {
    private int id;
    private String title;
    private String content;
    private String category;
    private Timestamp createdAt;

    public Notice(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getCategory() { return category; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
