package com.app.collector.scraper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewsScraper extends AbstractNewsScraper {

    // CHÌA KHÓA: Định dạng đầu ra thống nhất là dd/MM/yyyy
    // Parser của ông sẽ đọc chuỗi này và tự hiểu là 00:00 giờ
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    protected String extractTitle(Document doc) {
        Element h1 = doc.selectFirst("h1.title-detail, h1.content-detail-title, h1.vnn-title, h1");
        return (h1 != null) ? h1.text().trim() : "";
    }

    @Override
    protected String extractContent(Document doc) {
        StringBuilder content = new StringBuilder();
        // Selector bao quát cho cả VnExpress và Vietnamnet
        Elements paragraphs = doc.select(
            ".fck_detail p, #maincontent p, div.maincontent p, .content-detail p, article p, .vnn-content p"
        );
        if (!paragraphs.isEmpty()) {
            for (Element p : paragraphs) {
                // Lọc rác
                if (p.hasClass("Image") || p.attr("style").contains("display:none")) continue;
                String text = p.text().trim();
                if (!text.isEmpty()) content.append(text).append("\n");
            }
            return content.toString().trim();
        }
        return "";
    }

    @Override
    protected String extractTime(Document doc) {
        // 1. Ưu tiên Meta Tags (Dữ liệu ẩn, chính xác cao)
        String[] metaSelectors = {
            "meta[property='article:published_time']",
            "meta[itemprop='datePublished']",
            "meta[name='pubdate']"
        };

        for (String sel : metaSelectors) {
            Element meta = doc.selectFirst(sel);
            if (meta != null) {
                String isoDate = meta.attr("content"); // Vd: 2025-09-21T...
                // Chuyển từ ISO sang dd/MM/yyyy
                return convertIsoToVn(isoDate); 
            }
        }

        // 2. Nếu không có Meta, tìm Text hiển thị
        String[] textSelectors = {
            ".bread-crumb-detail__time",   // Vietnamnet
            "span.date",                   // VnExpress
            "span.time", 
            ".author-time",
            ".article-header .time"
        };

        for (String sel : textSelectors) {
            Element el = doc.selectFirst(sel);
            if (el != null) {
                // Lọc ngày từ text hỗn độn
                return extractDateFromText(el.text()); 
            }
        }

        return ""; 
    }

    // Hàm chuyển 2025-09-21 -> 21/09/2025
    private String convertIsoToVn(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        try {
            if (raw.contains("T")) raw = raw.split("T")[0];
            LocalDate date = LocalDate.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE);
            return date.format(OUTPUT_FORMAT);
        } catch (Exception e) { return ""; }
    }

    // Hàm lọc 9/9/2024 -> 09/09/2024
    private String extractDateFromText(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        // Regex bắt d/M/yyyy hoặc dd/MM/yyyy
        Pattern p = Pattern.compile("(\\d{1,2})[/-](\\d{1,2})[/-](\\d{4})");
        Matcher m = p.matcher(raw);
        if (m.find()) {
            try {
                int day = Integer.parseInt(m.group(1));
                int month = Integer.parseInt(m.group(2));
                int year = Integer.parseInt(m.group(3));
                return LocalDate.of(year, month, day).format(OUTPUT_FORMAT);
            } catch (Exception e) { return ""; }
        }
        return "";
    }
}