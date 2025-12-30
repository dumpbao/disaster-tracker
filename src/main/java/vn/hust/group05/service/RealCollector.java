package vn.hust.group05.service;

import com.app.collector.crawler.Crawler;
import com.app.collector.crawler.VnExpressSeleniumCrawler;
import com.app.collector.fetcher.SeleniumHtmlFetcher;
import com.app.collector.fetcher.htmlFetcher;
import com.app.collector.manager.collectormanager;
import com.app.collector.model.NewsPostRaw;
import com.app.collector.parser.NewsPostParser;
import com.app.collector.scraper.Scraper;
import com.app.collector.scraper.VnExpressNewsScraper;
import vn.hust.group05.model.Post;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RealCollector implements IDataCollector {

    @Override
    public List<Post> collect(String keyword) {
        System.out.println("Dang khoi dong Selenium (VnExpress)...");
        
        htmlFetcher fetcher = new SeleniumHtmlFetcher();
        Crawler crawler = new VnExpressSeleniumCrawler();
        Scraper scraper = new VnExpressNewsScraper();
        NewsPostParser parser = new NewsPostParser();

        collectormanager manager = new collectormanager(crawler, fetcher, scraper, parser);

        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(30); // Tìm trong 30 ngày

        List<NewsPostRaw> rawPosts = new ArrayList<>();
        try {
            rawPosts = manager.collect(keyword, from, to);
        } catch (Exception e) {
            System.out.println("Loi khi scrape: " + e.getMessage());
        } finally {
            if (fetcher instanceof SeleniumHtmlFetcher) {
                ((SeleniumHtmlFetcher) fetcher).close();
            }
        }
        
        List<Post> myPosts = new ArrayList<>();
        
        for (NewsPostRaw raw : rawPosts) {
            String title = raw.getTitle();
            String content = raw.getContent();
            if (content != null && content.length() > 100) {
                content = content.substring(0, 100) + "...";
            }
            String source = "VnExpress"; 
            String url = raw.getUrl(); // Lấy URL từ crawler

            String timestamp;
            if (raw.getPublishedTime() != null) {
                timestamp = raw.getPublishedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else {
                timestamp = LocalDate.now().toString();
            }

            // Truyền URL vào constructor mới
            Post p = new Post(title, content, source, timestamp, url);
            myPosts.add(p);
        }
        
        System.out.println("Da lay duoc " + myPosts.size() + " bai tu VnExpress.");
        return myPosts;
    }
}