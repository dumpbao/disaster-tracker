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
import java.util.ArrayList;
import java.util.List;

public class RealCollector implements IDataCollector {

    @Override
    public List<Post> collect(String keyword) {
        System.out.println("Dang khoi dong Selenium de cao du lieu that...");
        
        // 1. Khởi tạo
        htmlFetcher fetcher = new SeleniumHtmlFetcher();
        Crawler crawler = new VnExpressSeleniumCrawler();
        Scraper scraper = new VnExpressNewsScraper();
        NewsPostParser parser = new NewsPostParser();

        collectormanager manager = new collectormanager(crawler, fetcher, scraper, parser);

        // 2. Thiết lập thời gian tìm kiếm
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(30); // Tìm trong 30 ngày gần nhất

        // 3. Gọi hàm collect
        List<NewsPostRaw> rawPosts = manager.collect(keyword, from, to);
        
        // 4. Chuyển đổi dữ liệu (Đoạn này đã sửa lỗi NullPointer)
        List<Post> myPosts = new ArrayList<>();
        
        for (NewsPostRaw raw : rawPosts) {
            String title = raw.getTitle();
            String content = raw.getContent();
            if (content != null && content.length() > 100) {
                content = content.substring(0, 100) + "...";
            }
            
            String author = raw.getSource();
            
            // === SỬA LỖI Ở ĐÂY ===
            // Kiểm tra xem ngày tháng có bị null không trước khi dùng
            String timestamp = "Unknown Date";
            if (raw.getPublishedTime() != null) {
                timestamp = raw.getPublishedTime().toString();
            }
            // ======================

            String platform = "VnExpress";

            Post p = new Post(title, content, author, timestamp, platform);
            
            // Giả lập phân tích sentiment cho code Scraper
            p.setSentiment("Neutral"); 
            p.setDamageType("None");
            
            myPosts.add(p);
        }
        
        System.out.println("Da lay duoc " + myPosts.size() + " bai viet that tu VnExpress!");
        return myPosts;
    }
}