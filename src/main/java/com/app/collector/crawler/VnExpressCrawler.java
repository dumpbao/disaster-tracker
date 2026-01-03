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

public class VnExpressCrawler extends BaseSeleniumCollector implements Crawler {

    private static final int MAX_RESULTS = 80;
    // URL tìm kiếm chuẩn của VnExpress (đã kiểm tra ký tự sạch)
    private static final String SEARCH_URL = "https://timkiem.vnexpress.net/?q=%s&page=%d";

    @Override
    public List<String> crawl(String keyword, LocalDate from, LocalDate to) {
        String kw = (keyword == null || keyword.isBlank()) ? "" : keyword.trim();
        Set<String> results = new LinkedHashSet<>();
        
        int maxPages = 20;
        int currentPage = 1; // VnExpress bắt đầu từ page 1

        System.out.println("=== BAT DAU CRAWL VNEXPRESS (FAST MODE - CHI LAY LINK) ===");
        open();
        
        if (driver != null) {
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(15));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        }

        try {
            while (currentPage <= maxPages && results.size() < MAX_RESULTS) {
                
                String searchUrl = String.format(SEARCH_URL, kw, currentPage);
                System.out.println(">> [PAGE " + currentPage + "] Quet Link: " + searchUrl);

                try {
                    driver.get(searchUrl);
                    // Đợi các tiêu đề bài viết hiện ra
                    new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h3.title-news a")));
                } catch (Exception e) {
                    System.out.println("   ! Timeout hoac het trang. Stop.");
                    break;
                }

                List<WebElement> elements = driver.findElements(By.cssSelector("h3.title-news a"));
                
                if (elements.isEmpty()) {
                    System.out.println("   ! Khong thay bai nao nua.");
                    break;
                }

                int countBefore = results.size();
                for (WebElement el : elements) {
                    if (results.size() >= MAX_RESULTS) break;

                    String href = el.getAttribute("href");
                    
                    if (href != null && href.contains("vnexpress.net") && href.endsWith(".html")) {
                        if (!href.contains("/video/") && !href.contains("/podcast/")) {
                            results.add(href);
                        }
                    }
                }
                
                System.out.println("   + Them duoc " + (results.size() - countBefore) + " link.");
                
                if (results.size() == countBefore) {
                    break;
                }

                currentPage++;
            }
        } finally {
            close();
        }
        
        System.out.println(">>> VNEXPRESS DONE: " + results.size() + " LINK.");
        return new ArrayList<>(results);
    }
}