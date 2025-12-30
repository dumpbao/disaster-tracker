package vn.hust.group05.model;

public class Post {
    private String title;
    private String content;
    private String source;
    private String timestamp;
    private String url;
    private String location; 
    // Các biến phân tích
    private String sentiment;
    private String damageType;
    private String reliefType;

    // Cập nhật Constructor thêm URL
    public Post(String title, String content, String source, String timestamp, String url) {
        this.title = title;
        this.content = content;
        this.source = source;
        this.timestamp = timestamp;
        this.url = url;
    }

    // Getter & Setter
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getSource() { return source; }
    public String getTimestamp() { return timestamp; }
    public String getUrl() { return url; } // Getter cho URL

    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }

    public String getDamageType() { return damageType; }
    public void setDamageType(String damageType) { this.damageType = damageType; }

    public String getReliefType() { return reliefType; }
    public void setReliefType(String reliefType) { this.reliefType = reliefType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}