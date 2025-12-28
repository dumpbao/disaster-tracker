package com.app.collector.scraper;

import java.util.Map;

public interface Scraper {
    Map<String, String> scrape(String html);
}