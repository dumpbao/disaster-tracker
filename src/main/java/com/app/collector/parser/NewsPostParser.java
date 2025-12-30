package com.app.collector.parser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.app.collector.model.NewsPostRaw;

public class NewsPostParser {

    public NewsPostRaw parse(
            Map<String, String> raw,
            String url,
            String keyword
    ) {
        if (!isRelevant(raw, keyword)) {
            return null;
        }

        NewsPostRaw post = new NewsPostRaw();
        post.setUrl(url);
        post.setKeyword(keyword);
        post.setTitle(raw.get("title"));
        post.setContent(raw.get("content"));
        // Gọi hàm xử lý thời gian đã nâng cấp
        post.setPublishedTime(parseTime(raw.get("time")));

        return post;
    }
    
    private boolean isRelevant(Map<String, String> raw, String keyword) {
        String title = raw.getOrDefault("title", "").toLowerCase();
        String content = raw.getOrDefault("content", "").toLowerCase();

        // Tách từ khóa để tìm kĩ hơn
        if (keyword != null) {
            for (String token : keyword.toLowerCase().split("\\s+")) {
                if (title.contains(token) || content.contains(token)) {
                    return true;
                }
            }
        }
        return false;
    }

    private LocalDateTime parseTime(String timeRaw) {
        if (timeRaw == null || timeRaw.isBlank()) {
            return null;
        }

        // --- XỬ LÝ RIÊNG CHO VNEXPRESS ---
        // Input: "Chủ nhật, 13/4/2025, 11:43 (GMT+7)"
        // Logic: Dùng Regex tìm đoạn ngày/tháng/năm và giờ:phút
        try {
            // Regex tìm: (số/số/số) dấu_phẩy (số:số)
            Pattern pattern = Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{4}),\\s*(\\d{1,2}:\\d{1,2})");
            Matcher matcher = pattern.matcher(timeRaw);

            if (matcher.find()) {
                // Lấy ra phần ngày (group 1) và giờ (group 2) nối lại -> "13/4/2025 11:43"
                String cleanTime = matcher.group(1) + " " + matcher.group(2);
                
                // Dùng format d/M/yyyy (d và M viết thường 1 chữ để chấp nhận cả 1/4 lẫn 13/4)
                DateTimeFormatter vneFormatter = DateTimeFormatter.ofPattern("d/M/yyyy HH:mm");
                return LocalDateTime.parse(cleanTime, vneFormatter);
            }
        } catch (Exception e) {
            // Nếu lỗi thì bỏ qua, chạy xuống các format dự phòng bên dưới
            System.out.println("Loi parse date VnExpress: " + e.getMessage());
        }
        // ---------------------------------

        // Các định dạng dự phòng khác (cho các trang web khác nếu có)
        DateTimeFormatter[] formats = new DateTimeFormatter[] {
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        };

        for (DateTimeFormatter f : formats) {
            try {
                return LocalDateTime.parse(timeRaw, f);
            } catch (DateTimeParseException ignored) {
                // Thử format tiếp theo
            }
        }

        return null;
    }
}