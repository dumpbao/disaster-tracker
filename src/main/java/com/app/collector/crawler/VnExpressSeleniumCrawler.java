package com.app.collector.crawler;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Search-based Selenium crawler for VnExpress
 * Uses https://timkiem.vnexpress.net
 */
public class VnExpressSeleniumCrawler extends BaseSeleniumCollector implements Crawler {

    private static final int MAX_RESULTS = 50;

    private static final String SEARCH_URL =
            "https://timkiem.vnexpress.net/?q=%s";

    @Override
    public List<String> crawl(String keyword, LocalDate from, LocalDate to) {

        String kw = (keyword == null || keyword.isBlank())
                ? ""
                : keyword.trim();

        Set<String> results = new LinkedHashSet<>();
        int maxPages = 5;
        int currentPage = 1;

        open();

        try {
            String searchUrl = String.format(SEARCH_URL, kw);
            driver.get(searchUrl);
            waitMs(3000);

            while (currentPage <= maxPages && results.size() < MAX_RESULTS) {

                System.out.println("=== SEARCH PAGE " + currentPage + " ===");

                List<WebElement> links = driver.findElements(
                        By.cssSelector("a[href^='https://vnexpress.net/']")
                );

                for (WebElement el : links) {
                    if (results.size() >= MAX_RESULTS) break;

                    String href = el.getAttribute("href");
                    if (href == null || href.isBlank()) continue;

                    if (!href.matches("https://vnexpress\\.net/.+-\\d+\\.html")) {
                        continue;
                    }

                    if (results.add(href)) {
                        System.out.println("[FOUND] " + href);
                    }
                }

                List<WebElement> nextButtons = driver.findElements(
                        By.cssSelector("a.pagination__next, a[aria-label='Next']")
                );

                if (nextButtons.isEmpty()) {
                    System.out.println("No next page. Stop.");
                    break;
                }

                try {
                    nextButtons.get(0).click();
                    waitMs(2500);
                    currentPage++;
                } catch (Exception e) {
                    System.out.println("Cannot click next page. Stop.");
                    break;
                }
            }

        } finally {
            close();
        }

        return new ArrayList<>(results);
    }

}
