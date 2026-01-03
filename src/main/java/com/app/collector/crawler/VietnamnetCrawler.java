package com.app.collector.crawler;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class VietnamnetCrawler extends BaseSeleniumCollector implements Crawler {

    private static final int MAX_RESULTS = 50;
    // URL tìm kiếm
    private static final String SEARCH_URL = "https://vietnamnet.vn/tim-kiem-p%d?q=%s";

    @Override
    public List<String> crawl(String keyword, LocalDate from, LocalDate to) {
        String kw = (keyword == null || keyword.isBlank()) ? "" : keyword.trim();
        Set<String> results = new LinkedHashSet<>();
        
        int maxPages = 20; // Quét 5 trang đầu thôi cho nhanh
        int currentPage = 0;

        System.out.println("=== BAT DAU CRAWL VIETNAMNET (FAST MODE) ===");
        open();
        
        if (driver != null) {
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20));
        }

        try {
            while (currentPage < maxPages && results.size() < MAX_RESULTS) {
                
                String searchUrl = String.format(SEARCH_URL, currentPage, kw);
                System.out.println(">> [PAGE " + currentPage + "] Quet Link: " + searchUrl);

                try {
                    driver.get(searchUrl);
                    // Đợi 1 chút cho JS load
                    new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                } catch (Exception e) {
                    System.out.println("   ! Skip page do timeout.");
                    currentPage++;
                    continue;
                }

                // --- SỬA CSS SELECTOR BAO QUÁT HƠN ---
                // Tìm tất cả thẻ A nằm trong các thẻ tiêu đề (h3, div title...)
                List<WebElement> elements = driver.findElements(By.cssSelector(
                    ".vnn-title a, .feature-box__content a, .horizontalPost__main-title a, h3 a"
                ));
                
                if (elements.isEmpty()) {
                    System.out.println("   ! Khong tim thay the A nao hop le.");
                    // Thử fallback selector nếu CSS trên hỏng
                    elements = driver.findElements(By.cssSelector("a[href*='.html']"));
                }

                int countBefore = results.size();
                for (WebElement el : elements) {
                    if (results.size() >= MAX_RESULTS) break;

                    String href = el.getAttribute("href");
                    
                    // Lọc link chuẩn bài viết
                    if (href != null && href.contains("vietnamnet.vn") && href.endsWith(".html")) {
                        // Bỏ qua các chuyên mục không phải tin tức
                        if (!href.contains("/podcast") && !href.contains("/video") && 
                            !href.contains("/e-magazine") && !href.contains("/multimedia") &&
                            !href.contains("/su-kien/")) {
                            results.add(href);
                        }
                    }
                }
                
                System.out.println("   + Tim duoc them " + (results.size() - countBefore) + " link.");
                
                // Nếu quét trang mà không thấy bài mới -> Dừng
                if (results.size() == countBefore && countBefore > 0) {
                    break;
                }

                currentPage++;
            }
        } finally {
            close();
        }
        
        System.out.println(">>> VIETNAMNET DONE: " + results.size() + " LINK.");
        return new ArrayList<>(results);
    }
}