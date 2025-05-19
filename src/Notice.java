import java.sql.Timestamp;

public class Notice {
    private int id;
    private String title;
    private String content;
    private String category;
    private Timestamp createdAt;
    private boolean isEvent;
    private Timestamp eventTime;

    public Notice(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

    // New constructor for event-based notices
    public Notice(String title, String content, String category, boolean isEvent, String eventDateTime) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.isEvent = isEvent;

        try {
            if (eventDateTime != null && !eventDateTime.trim().isEmpty()) {
                // Convert ISO 8601 (e.g., "2025-05-25T10:00:00") to "2025-05-25 10:00:00"
                String formatted = eventDateTime.replace("T", " ");
                this.eventTime = Timestamp.valueOf(formatted);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("⚠️ Invalid event datetime format: " + eventDateTime);
        }
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getCategory() { return category; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isEvent() { return isEvent; }
    public void setEvent(boolean event) { isEvent = event; }

    public Timestamp getEventTime() { return eventTime; }
    public void setEventTime(Timestamp eventTime) { this.eventTime = eventTime; }
}
