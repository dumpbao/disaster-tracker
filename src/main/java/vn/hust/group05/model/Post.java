package vn.hust.group05.model;

public class Post {
    private String title;
    private String content;
    private String author;
    private String timestamp; // Dạng chuỗi cho đơn giản: "2025-10-20"
    private String platform;  // Nguồn: "Twitter", "Facebook", "YouTube"
    
    // Các trường dùng cho kết quả phân tích sau này
    private String sentiment; // "Positive", "Negative", "Neutral"
    private String damageType; // "Flood", "Landslide", "None"

    public Post(String title, String content, String author, String timestamp, String platform) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.timestamp = timestamp;
        this.platform = platform;
        this.sentiment = "Unknown"; // Mặc định chưa phân tích
        this.damageType = "Unknown";
    }

    // Getter và Setter (Bắt buộc để hiển thị lên bảng)
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public String getTimestamp() { return timestamp; }
    public String getPlatform() { return platform; }
    public String getSentiment() { return sentiment; }
    public String getDamageType() { return damageType; }

    public void setSentiment(String sentiment) { this.sentiment = sentiment; }
    public void setDamageType(String damageType) { this.damageType = damageType; }
    
    @Override
    public String toString() {
        return "[" + platform + "] " + title + " (" + sentiment + ")";
    }
}