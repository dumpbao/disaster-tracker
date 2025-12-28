package com.app.collector.scraper;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class AbstractNewsScraper implements Scraper {

    @Override
    public Map<String, String> scrape(String html) {
        Document doc = Jsoup.parse(html);

        Map<String, String> raw = new HashMap<>();
        raw.put("title", extractTitle(doc));
        raw.put("content", extractContent(doc));
        raw.put("time", extractTime(doc));

        return raw;
    }

    protected abstract String extractTitle(Document doc);
    protected abstract String extractContent(Document doc);
    protected abstract String extractTime(Document doc);
}
