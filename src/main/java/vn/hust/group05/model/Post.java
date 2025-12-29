package vn.hust.group05.model;

public class Post {
    private String source;
    private String title;
    private String content;
    private String author;
    private String timestamp;
    private String platform;
    
    // Các trường phân tích
    private String sentiment;   // Positive, Negative
    private String damageType;  // Flood, Landslide...
    private String reliefType;  // Food, Medicine, Shelter... (Mới thêm)

    public Post(String title, String content, String author, String timestamp, String platform) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.timestamp = timestamp;
        this.platform = platform;
        this.sentiment = "Neutral";
        this.damageType = "None";
        this.reliefType = "None"; // Mặc định
    }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getAuthor() { return author; }
    public String getTimestamp() { return timestamp; }
    public String getPlatform() { return platform; }
    public String getSentiment() { return sentiment; }
    public String getDamageType() { return damageType; }
    public String getReliefType() { return reliefType; } // Getter mới

    public void setSentiment(String sentiment) { this.sentiment = sentiment; }
    public void setDamageType(String damageType) { this.damageType = damageType; }
    public void setReliefType(String reliefType) { this.reliefType = reliefType; } // Setter mới
    
    @Override
    public String toString() {
        return "[" + platform + "] " + title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}