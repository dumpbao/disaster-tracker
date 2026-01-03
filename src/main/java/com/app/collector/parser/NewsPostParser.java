package com.app.collector.parser;

import com.app.collector.model.NewsPostRaw;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class NewsPostParser {

    public NewsPostRaw parse(Map<String, String> raw, String url, String keyword) {
        if (!isRelevant(raw, keyword)) {
            return null;
        }

        NewsPostRaw post = new NewsPostRaw();
        post.setUrl(url);
        post.setKeyword(keyword);
        post.setTitle(raw.get("title"));
        post.setContent(raw.get("content"));
        
        // Gọi hàm xử lý thời gian mới
        post.setPublishedTime(parseTime(raw.get("time")));

        return post;
    }
    
    private boolean isRelevant(Map<String, String> raw, String keyword) {
        String title = raw.getOrDefault("title", "").toLowerCase();
        String content = raw.getOrDefault("content", "").toLowerCase();
        if (keyword == null) return true;

        for (String token : keyword.toLowerCase().split("\\s+")) {
            if (title.contains(token) || content.contains(token)) {
                return true;
            }
        }
        return false;
    }

    // --- ĐÂY LÀ HÀM QUAN TRỌNG NHẤT ÔNG CẦN ---
    private LocalDateTime parseTime(String timeRaw) {
        if (timeRaw == null || timeRaw.isBlank()) {
            return null;
        }
        
        timeRaw = timeRaw.trim();

        // 1. Thử các định dạng có Giờ (để tương thích nếu lỡ có)
        DateTimeFormatter[] dateTimeFormats = {
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        };

        for (DateTimeFormatter f : dateTimeFormats) {
            try {
                return LocalDateTime.parse(timeRaw, f);
            } catch (DateTimeParseException ignored) { }
        }

        // 2. CHẤP NHẬN "CHỈ CÓ NGÀY" (Date Only)
        // Đây là chỗ giúp code không bị chết khi Scraper gửi về "10/10/2025"
        DateTimeFormatter[] dateFormats = {
            DateTimeFormatter.ofPattern("dd/MM/yyyy"), // Ưu tiên định dạng Việt Nam
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("d/M/yyyy")    // Chấp nhận cả 9/9/2024
        };

        for (DateTimeFormatter f : dateFormats) {
            try {
                // Parse thành Ngày
                LocalDate date = LocalDate.parse(timeRaw, f);
                // Chuyển thành LocalDateTime (tự thêm 00:00 cho đủ thủ tục) để code ông không lỗi
                return date.atStartOfDay(); 
            } catch (DateTimeParseException ignored) { }
        }

        return null; // Không cứu được
    }
}