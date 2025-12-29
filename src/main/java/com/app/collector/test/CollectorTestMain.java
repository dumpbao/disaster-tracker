package com.app.collector.test;

import java.time.LocalDate;
import java.util.List;

import com.app.collector.crawler.Crawler;
import com.app.collector.crawler.VnExpressSeleniumCrawler;
import com.app.collector.fetcher.SeleniumHtmlFetcher;
import com.app.collector.fetcher.htmlFetcher;
import com.app.collector.manager.collectormanager;
import com.app.collector.model.NewsPostRaw;
import com.app.collector.parser.NewsPostParser;
import com.app.collector.scraper.Scraper;
import com.app.collector.scraper.VnExpressNewsScraper;

public class CollectorTestMain {

    public static void main(String[] args) {

        htmlFetcher fetcher = new SeleniumHtmlFetcher();

        Crawler crawler = new VnExpressSeleniumCrawler();

        Scraper scraper = new VnExpressNewsScraper();

        NewsPostParser parser = new NewsPostParser();

        collectormanager manager =
            new collectormanager(crawler, fetcher, scraper, parser);
        
        String keyword = "yagi";
        LocalDate from = LocalDate.of(2024, 9, 6);
        LocalDate to   = LocalDate.of(2024, 9, 10);
        
        List<String> urls = crawler.crawl(keyword, from, to);
        System.out.println("CRAWLED URLS: " + urls.size());
        urls.forEach(System.out::println);
        
        List<NewsPostRaw> posts = manager.collect(keyword, from, to);


        for (NewsPostRaw post : posts) {
            System.out.println("TITLE: " + post.getTitle());
            System.out.println("TIME : " + post.getPublishedTime());
            System.out.println("URL  : " + post.getUrl());
            System.out.println("----------------------------------");
        }
    }
}
