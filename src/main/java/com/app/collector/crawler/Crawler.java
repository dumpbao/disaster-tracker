package com.app.collector.crawler;

import java.time.LocalDate;
import java.util.List;

public interface Crawler {
    List<String> crawl(String keyword, LocalDate from, LocalDate to);
}
