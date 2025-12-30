package vn.hust.group05.service;

import vn.hust.group05.model.Post;
import java.util.*;

public class DataAnalyzer {

    private GeminiService geminiService = new GeminiService();
    
    // Gộp 5 bài vào 1 request
    private static final int BATCH_SIZE = 5; 

    public void analyzeAll(List<Post> posts) {
        int total = posts.size();
        
        // Duyệt từng nhóm 5 bài
        for (int i = 0; i < total; i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, total);
            List<Post> batch = posts.subList(i, end);
            
            System.out.println("Dang xu ly lo: " + (i+1) + " den " + end + "...");

            // 1. Xây dựng Prompt gộp (Batch Prompt)
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("Analyze these ").append(batch.size()).append(" news items about storm/flood in Vietnam.\n");
            // YÊU CẦU QUAN TRỌNG: Dùng dấu ;;; để ngăn cách các bài
            promptBuilder.append("Return the result as a single string. Separate each article's analysis with ';;;'.\n");
            promptBuilder.append("Format for each article: Sentiment|DamageType|ReliefType|Location\n");
            promptBuilder.append("Rules:\n");
            promptBuilder.append("- Sentiment: Positive, Negative, Neutral\n");
            promptBuilder.append("- DamageType: People, Infrastructure, Agriculture, None\n");
            promptBuilder.append("- ReliefType: Money, Goods, Forces, None\n");
            promptBuilder.append("- Location: Province/City name or Unknown\n");
            promptBuilder.append("DO NOT put numbers like [1] or [2]. Just the data.\n");
            promptBuilder.append("News list:\n");

            for (int j = 0; j < batch.size(); j++) {
                Post p = batch.get(j);
                String contentShort = p.getContent().length() > 200 ? p.getContent().substring(0, 200) : p.getContent();
                promptBuilder.append("Article ").append(j + 1).append(": Title: ").append(p.getTitle())
                             .append(" | Content: ").append(contentShort).append("\n");
            }

            // 2. Gọi Gemini
            String resultBlock = geminiService.askGemini(promptBuilder.toString());
            
            // 3. Tách kết quả bằng dấu ;;; (Chuẩn hơn dùng xuống dòng)
            // Kể cả Gemini viết liền: Negative|...|Hanoi;;;Positive|...
            String[] rawResults = resultBlock.split(";;;");

            // 4. Map kết quả ngược lại vào Post
            for (int j = 0; j < batch.size(); j++) {
                // Nếu số lượng kết quả trả về ít hơn số bài gửi đi -> Lấy default
                if (j >= rawResults.length) {
                    setDefault(batch.get(j));
                    continue;
                }

                String line = rawResults[j].trim();
                // Xử lý trường hợp dòng rỗng do split thừa
                if (line.isEmpty() && j + 1 < rawResults.length) {
                    line = rawResults[j+1].trim(); // Thử lấy dòng tiếp
                }

                try {
                    // Format: Sentiment|Damage|Relief|Location
                    String[] parts = line.split("\\|");
                    if (parts.length >= 4) {
                        Post p = batch.get(j);
                        p.setSentiment(parts[0].trim());
                        p.setDamageType(parts[1].trim());
                        p.setReliefType(parts[2].trim());
                        p.setLocation(parts[3].trim());
                    } else {
                        // Nếu format sai
                        setDefault(batch.get(j));
                    }
                } catch (Exception e) {
                    System.err.println("Loi parse: " + line);
                    setDefault(batch.get(j));
                }
            }

            // Nghỉ 2 giây giữa các lô
            try { Thread.sleep(2000); } catch (InterruptedException e) {}
        }
    }

    // Hàm set giá trị mặc định khi lỗi
    private void setDefault(Post p) {
        if (p.getSentiment() == null) {
            p.setSentiment("Neutral");
            p.setDamageType("None");
            p.setReliefType("None");
            p.setLocation("Unknown");
        }
    }

    // ==========================================================
    // CÁC HÀM THỐNG KÊ (GIỮ NGUYÊN)
    // ==========================================================
    public Map<String, Integer> getTrendStats(List<Post> posts) {
        Map<String, Integer> trends = new TreeMap<>();
        for (Post p : posts) {
            String date = p.getTimestamp();
            if (date != null && date.length() >= 10) {
                String day = date.substring(0, 10);
                trends.put(day, trends.getOrDefault(day, 0) + 1);
            }
        }
        return trends;
    }

    public Map<String, Integer> getLocationStats(List<Post> posts) {
        Map<String, Integer> locs = new HashMap<>();
        for (Post p : posts) {
            String location = p.getLocation();
            if (location != null && !location.equalsIgnoreCase("Unknown") && !location.equalsIgnoreCase("None")) {
                locs.put(location, locs.getOrDefault(location, 0) + 1);
            }
        }
        return locs;
    }

    public Map<String, Integer> getSentimentStats(List<Post> posts) {
        Map<String, Integer> stats = new HashMap<>();
        for (Post p : posts) {
            String s = p.getSentiment();
            if (s != null) stats.put(s, stats.getOrDefault(s, 0) + 1);
        }
        return stats;
    }

    public Map<String, Integer> getDamageStats(List<Post> posts) {
        Map<String, Integer> stats = new HashMap<>();
        for (Post p : posts) {
            String d = p.getDamageType();
            if (d != null && !d.equalsIgnoreCase("None")) {
                stats.put(d, stats.getOrDefault(d, 0) + 1);
            }
        }
        return stats;
    }

    public Map<String, Map<String, Integer>> getReliefStats(List<Post> posts) {
        Map<String, Map<String, Integer>> result = new HashMap<>();
        for (Post p : posts) {
            String relief = p.getReliefType();
            if (relief == null || relief.equalsIgnoreCase("None")) continue; 
            result.putIfAbsent(relief, new HashMap<>());
            String sentiment = p.getSentiment();
            if (sentiment == null) sentiment = "Neutral";
            result.get(relief).put(sentiment, result.get(relief).getOrDefault(sentiment, 0) + 1);
        }
        return result;
    }
}