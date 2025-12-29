package vn.hust.group05.service;

import vn.hust.group05.model.Post;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataAnalyzer {

    // 1. Thống kê cảm xúc (Cho Pie Chart)
    public Map<String, Integer> analyzeSentiment(List<Post> posts) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("Positive", 0);
        stats.put("Negative", 0);
        stats.put("Neutral", 0);

        for (Post p : posts) {
            String sentiment = p.getSentiment();
            if (sentiment != null) {
                stats.put(sentiment, stats.getOrDefault(sentiment, 0) + 1);
            }
        }
        return stats;
    }

    // 2. Thống kê loại thiệt hại (Cho Bar Chart)
    public Map<String, Integer> analyzeDamageType(List<Post> posts) {
        Map<String, Integer> stats = new HashMap<>();
        
        for (Post p : posts) {
            String damage = p.getDamageType();
            // Chỉ thống kê những cái có thiệt hại cụ thể (bỏ qua "None")
            if (damage != null && !damage.equals("None")) {
                stats.put(damage, stats.getOrDefault(damage, 0) + 1);
            }
        }
        return stats;
    }
    
    // 3. Thống kê Cứu trợ theo Cảm xúc (Cho Stacked Bar Chart)
    // Cấu trúc trả về: Map<Loại cứu trợ, Map<Cảm xúc, Số lượng>>
    public Map<String, Map<String, Integer>> analyzeReliefAndSentiment(List<Post> posts) {
        Map<String, Map<String, Integer>> result = new HashMap<>();

        for (Post p : posts) {
            String relief = p.getReliefType();
            // Bỏ qua nếu không phải tin cứu trợ
            if (relief == null || "None".equals(relief)) continue; 

            // Tạo map con nếu chưa có
            result.putIfAbsent(relief, new HashMap<>());
            
            // Lấy map con ra để cộng dồn cảm xúc
            Map<String, Integer> sentimentMap = result.get(relief);
            String sentiment = p.getSentiment() == null ? "Neutral" : p.getSentiment();
            
            sentimentMap.put(sentiment, sentimentMap.getOrDefault(sentiment, 0) + 1);
        }
        return result;
    }

    // 4. Tìm nguồn tin phổ biến (Optional)
    public String getTopSource(List<Post> posts) {
        Map<String, Integer> sources = new HashMap<>();
        String topSource = "N/A";
        int maxCount = 0;
        
        for (Post p : posts) {
            String src = p.getSource();
            if (src == null) continue;
            
            int count = sources.getOrDefault(src, 0) + 1;
            sources.put(src, count);
            
            if (count > maxCount) {
                maxCount = count;
                topSource = src;
            }
        }
        return topSource;
    }
}