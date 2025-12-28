package vn.hust.group05.service;

import com.app.collector.crawler.Crawler;
import com.app.collector.crawler.VnExpressSeleniumCrawler; // Đảm bảo bạn đã copy file này
import com.app.collector.fetcher.SeleniumHtmlFetcher;     // Đảm bảo bạn đã copy file này
import com.app.collector.fetcher.htmlFetcher;
import com.app.collector.manager.collectormanager;
import com.app.collector.model.NewsPostRaw;
import com.app.collector.parser.NewsPostParser;
import com.app.collector.scraper.Scraper;
import com.app.collector.scraper.VnExpressNewsScraper;    // Đảm bảo bạn đã copy file này

import vn.hust.group05.model.Post;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RealCollector implements IDataCollector {

    @Override
    public List<Post> collect(String keyword) {
        System.out.println("Dang khoi dong Selenium de cao du lieu that...");
        
        // 1. Khởi tạo các thành phần của nhóm Data Collector
        // (Lưu ý: Nếu báo đỏ dòng nào là do bạn chưa copy file class tương ứng của bạn kia vào)
        htmlFetcher fetcher = new SeleniumHtmlFetcher();
        Crawler crawler = new VnExpressSeleniumCrawler();
        Scraper scraper = new VnExpressNewsScraper();
        NewsPostParser parser = new NewsPostParser();

        collectormanager manager = new collectormanager(crawler, fetcher, scraper, parser);

        // 2. Thiết lập thời gian tìm kiếm (Ví dụ: tìm trong 3 ngày gần nhất)
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(3);

        // 3. Gọi hàm collect của bạn kia
        List<NewsPostRaw> rawPosts = manager.collect(keyword, from, to);
        
        // 4. Chuyển đổi (Convert) từ NewsPostRaw sang Post của giao diện
        List<Post> myPosts = new ArrayList<>();
        
        for (NewsPostRaw raw : rawPosts) {
            // Mapping dữ liệu:
            String title = raw.getTitle();
            String content = raw.getContent();
            if (content != null && content.length() > 100) {
                content = content.substring(0, 100) + "..."; // Cắt ngắn nội dung cho đẹp bảng
            }
            String author = raw.getSource(); // Lấy nguồn làm tác giả
            String timestamp = raw.getPublishedTime().toString(); // Chuyển ngày tháng sang chuỗi
            String platform = "VnExpress"; // Vì code bạn kia là VnExpressCrawler

            Post p = new Post(title, content, author, timestamp, platform);
            
            // Xử lý sơ bộ (sau này có thể nâng cấp AI phân tích sentiment ở đây)
            p.setSentiment("Neutral"); 
            p.setDamageType("Unknown");
            
            myPosts.add(p);
        }
        
        System.out.println("Da lay duoc " + myPosts.size() + " bai viet that!");
        return myPosts;
    }
}