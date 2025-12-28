package com.app.collector.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.app.collector.model.NewsPostRaw;

import java.time.LocalDateTime;
import java.util.Map;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
	    post.setPublishedTime(parseTime(raw.get("time")));

	    return post;
	}
	
	private boolean isRelevant(Map<String, String> raw, String keyword) {

	    String title = raw.getOrDefault("title", "").toLowerCase();
	    String content = raw.getOrDefault("content", "").toLowerCase();

	    for (String token : keyword.toLowerCase().split("\\s+")) {
	        if (title.contains(token) || content.contains(token)) {
	            return true;
	        }
	    }
	    return false;
	}

	
    private LocalDateTime parseTime(String timeRaw) {

        if (timeRaw == null || timeRaw.isBlank()) {
            return null;
        }

        DateTimeFormatter[] formats = new DateTimeFormatter[] {
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        };

        for (DateTimeFormatter f : formats) {
            try {
                return LocalDateTime.parse(timeRaw, f);
            } catch (DateTimeParseException ignored) {

            }
        }

        return null;
    }

}
