package vn.hust.group05.service;

import vn.hust.group05.model.Post;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DataAnalyzer {

    private GeminiService geminiService = new GeminiService();
    
    // Format ngày tháng đầu vào từ Scraper (dd/MM/yyyy)
    private static final DateTimeFormatter INPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    // Format ngày tháng đầu ra cho Biểu đồ (MM/yyyy - Gộp theo tháng)
    private static final DateTimeFormatter CHART_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");

    public void analyzeAll(List<Post> posts) {
        int total = posts.size();
        if (total == 0) return;

        // --- TÍNH TOÁN BATCH SIZE ĐỘNG ---
        // Chia tổng số bài cho 4 request để tối ưu tốc độ
        int batchSize = (int) Math.ceil((double) total / 4);
        if (batchSize < 1) batchSize = 1;

        System.out.println("=== BAT DAU PHAN TICH " + total + " BAI VIET (DYNAMIC BATCH: " + batchSize + "/REQ) ===");
        
        for (int i = 0; i < total; i += batchSize) {
            int end = Math.min(i + batchSize, total);
            List<Post> batch = posts.subList(i, end);
            
            int currentBatchNum = (i / batchSize) + 1;
            System.out.println(">> Request " + currentBatchNum + "/4: Gui tu bai " + (i+1) + " den " + end + "...");
            
            // 1. Prompt (ID Mapping Mode)
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("Analyze these ").append(batch.size()).append(" news items.\n");
            promptBuilder.append("Format per line: ID|Sentiment|DamageType|ReliefType|Location\n");
            promptBuilder.append("The 'ID' must match the number provided in [Input Data].\n\n");
            
            promptBuilder.append("STRICT RULES:\n");
            promptBuilder.append("1. Sentiment: {Positive, Negative, Neutral}\n");
            
            // Quy tắc Priority: Chỉ chọn 1 Damage duy nhất
            promptBuilder.append("2. DamageType: Choose ONLY ONE from {People, Infrastructure, Agriculture, None}.\n");
            promptBuilder.append("   - Priority: People > Infrastructure > Agriculture.\n"); 
            
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
                
                // Gắn ID [0], [1]...
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

    // --- BỘ LỌC DAMAGE ƯU TIÊN ---
    private String normalizeDamage(String raw) {
        String lower = raw.toLowerCase();
        if (lower.contains("people") || lower.contains("casualt") || lower.contains("death") || lower.contains("died")) {
            return "People";
        }
        if (lower.contains("infrastructure") || lower.contains("house") || lower.contains("bridge") || lower.contains("road") || lower.contains("roof")) {
            return "Infrastructure";
        }
        if (lower.contains("agriculture") || lower.contains("crop") || lower.contains("farm") || lower.contains("rice")) {
            return "Agriculture";
        }
        return "None";
    }

    // --- CÁC HÀM VALIDATOR KHÁC ---
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

    // --- QUAN TRỌNG: GỘP THEO THÁNG & SẮP XẾP ĐÚNG ---
    public Map<String, Integer> getTrendStats(List<Post> posts) {
        // TreeMap với Comparator chuyên dụng cho Tháng/Năm (YearMonth)
        // Giúp 12/2024 đứng trước 01/2025 (không bị lỗi sort String)
        Map<String, Integer> trends = new TreeMap<>((s1, s2) -> {
            try {
                YearMonth ym1 = YearMonth.parse(s1, CHART_DATE_FORMATTER);
                YearMonth ym2 = YearMonth.parse(s2, CHART_DATE_FORMATTER);
                return ym1.compareTo(ym2);
            } catch (Exception e) {
                return s1.compareTo(s2);
            }
        });

        for (Post p : posts) {
            String dateStr = p.getTimestamp();
            if (dateStr != null && !dateStr.isEmpty()) {
                try {
                    // 1. Parse ngày gốc (dd/MM/yyyy)
                    LocalDate date = LocalDate.parse(dateStr, INPUT_DATE_FORMATTER);
                    
                    // 2. Chuyển thành key Tháng/Năm (MM/yyyy)
                    String monthKey = date.format(CHART_DATE_FORMATTER);
                    
                    // 3. Cộng dồn vào Map
                    trends.put(monthKey, trends.getOrDefault(monthKey, 0) + 1);
                } catch (Exception e) {
                    // Bỏ qua nếu ngày lỗi
                }
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