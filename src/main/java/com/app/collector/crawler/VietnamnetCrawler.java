package com.app.collector.crawler;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException; // Import thêm cái này
import org.openqa.selenium.WebElement;


import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class VietnamnetCrawler extends BaseSeleniumCollector implements Crawler {

    private static final int MAX_RESULTS = 20;
    private static final String SEARCH_URL = "https://vietnamnet.vn/tim-kiem-p%d?q=%s";

    @Override
    public List<String> crawl(String keyword, LocalDate from, LocalDate to) {
        String kw = (keyword == null || keyword.isBlank()) ? "" : keyword.trim();
        Set<String> results = new LinkedHashSet<>();
        
        int maxPages = 20; // Quét sâu 20 trang
        int currentPage = 0;

        System.out.println("=== BAT DAU CRAWL VIETNAMNET (ANTI-STALE MODE) ===");
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
                    // Nghỉ 1s để DOM ổn định, tránh lỗi Stale
                    Thread.sleep(1000); 
                } catch (Exception e) {
                    System.out.println("   ! Loi load trang. Skip.");
                    currentPage++;
                    continue;
                }

                List<WebElement> elements = driver.findElements(By.cssSelector(
                    ".vnn-title a, .feature-box__content a, .horizontalPost__main-title a, h3 a"
                ));
                
                if (elements.isEmpty()) {
                    System.out.println("   ! Khong tim thay the A nao.");
                    // Thử fallback selector
                    elements = driver.findElements(By.cssSelector("a[href*='.html']"));
                }

                int countBefore = results.size();
                
                // --- VÒNG LẶP AN TOÀN (TRY-CATCH STALE ELEMENT) ---
                for (WebElement el : elements) {
                    if (results.size() >= MAX_RESULTS) break;

                    try {
                        String href = el.getAttribute("href");
                        String hrefLower = href.toLowerCase();
                        
                        if (href != null && href.contains("vietnamnet.vn") && href.endsWith(".html")) {
                            if (!hrefLower.contains("/podcast") && !hrefLower.contains("/video") && 
                                !hrefLower.contains("/e-magazine") && !hrefLower.contains("/multimedia")) {
                                results.add(href);
                            }
                        }
                    } catch (StaleElementReferenceException e) {
                        // Nếu thẻ bị lỗi (do web đổi DOM), chỉ cần bỏ qua và đi tiếp
                        // Không in lỗi ra để đỡ rối mắt, coi như thẻ rác
                        continue; 
                    } catch (Exception e) {
                        // Các lỗi khác thì bỏ qua
                    }
                }
                
                System.out.println("   + Tim duoc them " + (results.size() - countBefore) + " link.");
                
                // Nếu quét trang mà không thấy bài mới -> Dừng
                if (results.size() == countBefore && countBefore > 0) {
                    // Thử quét thêm 1 trang nữa cho chắc rồi mới dừng (đề phòng trang này toàn video)
                    if (currentPage > 5) break; 
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