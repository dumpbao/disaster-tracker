package vn.hust.group05.service;

import com.app.collector.crawler.*;
import com.app.collector.fetcher.SeleniumHtmlFetcher;
import com.app.collector.fetcher.htmlFetcher;
import com.app.collector.manager.collectormanager;
import com.app.collector.model.NewsPostRaw;
import com.app.collector.parser.NewsPostParser;
import com.app.collector.scraper.NewsScraper;
import com.app.collector.scraper.Scraper;
import vn.hust.group05.model.Post;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RealCollector implements IDataCollector {

    @Override
    public List<Post> collect(String keyword) {
        System.out.println("=== BAT DAU CAO DU LIEU ===");
        
        // Khởi tạo các thành phần
        htmlFetcher fetcher = new SeleniumHtmlFetcher(); 
        NewsPostParser parser = new NewsPostParser();
        Scraper scraper = new NewsScraper(); 

        List<Crawler> crawlers = new ArrayList<>();
        crawlers.add(new VnExpressCrawler());  
        crawlers.add(new VietnamnetCrawler());

        // Lấy dữ liệu trong 30 ngày (hoặc tùy ông chỉnh)
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(30);

        List<Post> myPosts = new ArrayList<>();

        for (Crawler crawler : crawlers) {
            String sourceName = crawler.getClass().getSimpleName().replace("Crawler", "");
            System.out.println(">> Dang cao: " + sourceName);

            try {
                collectormanager manager = new collectormanager(crawler, fetcher, scraper, parser);
                List<NewsPostRaw> rawPosts = manager.collect(keyword, from, to);
                
                System.out.println("   + Tim thay " + rawPosts.size() + " bai.");

                for (NewsPostRaw raw : rawPosts) {
                    myPosts.add(convertRawToPost(raw, sourceName));
                }

            } catch (Exception e) {
                System.err.println("Loi khi cao bao " + sourceName + ": " + e.getMessage());
            }
        }
        
        if (fetcher instanceof SeleniumHtmlFetcher) {
            ((SeleniumHtmlFetcher) fetcher).close();
        }

        System.out.println("=== TONG CONG: " + myPosts.size() + " bai viet ===");
        return myPosts;
    }

    private Post convertRawToPost(NewsPostRaw raw, String sourceName) {
        String title = raw.getTitle();
        String content = raw.getContent();
        
        // Cắt ngắn nội dung để tránh nặng RAM
        if (content != null && content.length() > 1500) {
            content = content.substring(0, 1500) + "...";
        }
        
        String url = raw.getUrl();
        String timestamp;
        
        // QUAN TRỌNG: Format hiển thị ra màn hình là dd/MM/yyyy
        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (raw.getPublishedTime() != null) {
            // Lấy thời gian chuẩn từ Parser và format lại
            timestamp = raw.getPublishedTime().format(displayFormat);
        } else {
            // Fallback: Nếu vẫn lỗi thì lấy ngày hôm nay (nhưng format đúng kiểu VN)
            timestamp = LocalDate.now().format(displayFormat);
        }
        
        return new Post(title, content, sourceName, timestamp, url);
    }
}