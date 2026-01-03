package com.app.collector.manager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.app.collector.crawler.Crawler;
import com.app.collector.fetcher.htmlFetcher;
import com.app.collector.model.NewsPostRaw;
import com.app.collector.parser.NewsPostParser;
import com.app.collector.scraper.Scraper;

public class collectormanager {

    private Crawler crawler;
    private htmlFetcher fetcher;
    private Scraper scraper;
    private NewsPostParser parser;
    
    public collectormanager(
            Crawler crawler,
            htmlFetcher fetcher,
            Scraper scraper,
            NewsPostParser parser
    ) {
        this.crawler = crawler;
        this.fetcher = fetcher;
        this.scraper = scraper;
        this.parser = parser;
    }

    public List<NewsPostRaw> collect(String keyword, LocalDate from, LocalDate to) {
        List<String> urls = crawler.crawl(keyword, from, to);
        
        List<NewsPostRaw> results = new ArrayList<>();
        for (String url : urls) {
            String html = fetcher.fetch(url);
            
            if (html == null) {
                continue;
            }
            
            Map<String, String> raw = scraper.scrape(html);
            
            
            NewsPostRaw post = parser.parse(raw, url, keyword);

            if (post != null) {
                results.add(post);
            }
        }
        return results;
    }
    
}

