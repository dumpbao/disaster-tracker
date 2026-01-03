package vn.hust.group05.service;

import vn.hust.group05.model.Post;
import java.util.*;

public class DataAnalyzer {

    private GeminiService geminiService = new GeminiService();
    private static final int BATCH_SIZE = 50; 

    public void analyzeAll(List<Post> posts) {
        int total = posts.size();
        System.out.println("=== BAT DAU PHAN TICH " + total + " BAI VIET (SINGLE DAMAGE MODE) ===");
        
        for (int i = 0; i < total; i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, total);
            List<Post> batch = posts.subList(i, end);
            
            System.out.println(">> Batch " + ((i/BATCH_SIZE)+1) + ": Gui tu bai " + (i+1) + " den " + end + "...");
            
            // 1. Prompt
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("Analyze these ").append(batch.size()).append(" news items.\n");
            promptBuilder.append("Format per line: ID|Sentiment|DamageType|ReliefType|Location\n");
            promptBuilder.append("The 'ID' must match the number provided in [Input Data].\n\n");
            
            promptBuilder.append("STRICT RULES:\n");
            promptBuilder.append("1. Sentiment: {Positive, Negative, Neutral}\n");
            
            // --- QUAN TRỌNG: Quy tắc chọn 1 Damage duy nhất ---
            promptBuilder.append("2. DamageType: Choose ONLY ONE from {People, Infrastructure, Agriculture, None}.\n");
            promptBuilder.append("   - Priority if multiple damages mentioned: People > Infrastructure > Agriculture.\n"); 
            promptBuilder.append("   - Example: If text says '3 people died and bridge collapsed', choose 'People'.\n");
            
            promptBuilder.append("3. ReliefType: {Money, Goods, Forces, None}\n");
            promptBuilder.append("4. Location: Specific Province or Unknown\n");
            promptBuilder.append("5. Analyze Damage and Relief INDEPENDENTLY.\n\n");
            
            promptBuilder.append("INPUT DATA:\n");

            for (int j = 0; j < batch.size(); j++) {
                Post p = batch.get(j);
                String contentShort = p.getContent() != null && p.getContent().length() > 800 
                                      ? p.getContent().substring(0, 800) 
                                      : p.getContent();
                contentShort = contentShort.replace("\n", " ").trim();
                
                promptBuilder.append("[").append(j).append("] Title: ").append(p.getTitle())
                             .append(" | Content: ").append(contentShort).append("\n");
            }

            // 2. Gọi Gemini
            String resultBlock = geminiService.askGemini(promptBuilder.toString());
            
            // 3. Xử lý kết quả (ID Mapping)
            Map<Integer, String[]> resultMap = new HashMap<>();
            String[] rawLines = resultBlock.split("\n");
            
            for (String line : rawLines) {
                line = line.trim();
                if (line.length() < 5 || !line.contains("|")) continue;
                try {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 5) {
                        int id = Integer.parseInt(parts[0].trim());
                        resultMap.put(id, parts);
                    }
                } catch (Exception e) {}
            }

            // 4. Map vào Batch
            int countSuccess = 0;
            for (int j = 0; j < batch.size(); j++) {
                Post p = batch.get(j);
                if (resultMap.containsKey(j)) {
                    String[] parts = resultMap.get(j);
                    try {
                        p.setSentiment(normalizeSentiment(parts[1].trim()));
                        
                        // --- ĐÂY: Hàm này đảm bảo chỉ ra 1 Damage duy nhất ---
                        p.setDamageType(normalizeDamage(parts[2].trim()));
                        
                        p.setReliefType(normalizeRelief(parts[3].trim()));
                        p.setLocation(cleanValue(parts[4].trim()));
                        countSuccess++;
                    } catch (Exception e) { setDefault(p); }
                } else {
                    setDefault(p);
                }
            }
            System.out.println("   -> Da map: " + countSuccess + "/" + batch.size());
            
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
        }
        System.out.println("=== HOAN TAT PHAN TICH ===");
    }

    // --- BỘ LỌC DAMAGE CHỈ LẤY 1 CÁI (PRIORITY MODE) ---
    private String normalizeDamage(String raw) {
        String lower = raw.toLowerCase();
        
        // Thứ tự kiểm tra này chính là thứ tự ưu tiên (Priority)
        // Nếu bài viết có chữ "people" -> Lấy luôn People (kệ các cái khác)
        if (lower.contains("people") || lower.contains("casualt") || lower.contains("death") || lower.contains("died")) {
            return "People";
        }
        // Nếu không có người chết, mới xét đến hạ tầng
        if (lower.contains("infrastructure") || lower.contains("house") || lower.contains("bridge") || lower.contains("road")) {
            return "Infrastructure";
        }
        // Cuối cùng mới đến nông nghiệp
        if (lower.contains("agriculture") || lower.contains("crop") || lower.contains("farm")) {
            return "Agriculture";
        }
        
        return "None";
    }

    // Các hàm khác giữ nguyên
    private String normalizeSentiment(String raw) {
        String lower = raw.toLowerCase();
        if (lower.contains("positive")) return "Positive";
        if (lower.contains("negative")) return "Negative";
        return "Neutral";
    }

    private String normalizeRelief(String raw) {
        String lower = raw.toLowerCase();
        if (lower.contains("money")) return "Money";
        if (lower.contains("good")) return "Goods";
        if (lower.contains("force") || lower.contains("army") || lower.contains("police")) return "Forces";
        return "None";
    }

    private String cleanValue(String input) {
        if (input == null) return "Unknown";
        if (input.contains(";")) return input.split(";")[0].trim();
        if (input.contains(",")) return input.split(",")[0].trim();
        return input;
    }

    private void setDefault(Post p) {
        p.setSentiment("Neutral");
        p.setDamageType("None");
        p.setReliefType("None");
        p.setLocation("Unknown");
    }

    // --- THỐNG KÊ (LOGIC CŨ) ---
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
            if (location != null && !isInvalid(location)) {
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

    private boolean isInvalid(String s) {
        return s.equalsIgnoreCase("Unknown") || s.equalsIgnoreCase("None") || s.contains("Unknown");
    }
}