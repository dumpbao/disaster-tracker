package com.app.collector.crawler;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.time.Duration;

public abstract class BaseSeleniumCollector {

    protected WebDriver driver;
    protected JavascriptExecutor js;

    protected void open() {
        if (driver != null) return;

        ChromeOptions options = new ChromeOptions();
        
        // 1. EAGER: Tải nhanh, không chờ quảng cáo
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);

        // 2. CẤU HÌNH CHỐNG CRASH (QUAN TRỌNG)
        // Fix lỗi "DevToolsActivePort file doesn't exist"
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--remote-debugging-port=9222"); // <--- DÒNG NÀY CỨU MẠNG ÔNG ĐÂY
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        
        // 3. Ẩn danh & Tối ưu
        options.addArguments("--incognito"); 
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-gpu");
        options.addArguments("--blink-settings=imagesEnabled=false");

        try {
            driver = new ChromeDriver(options);
            
            // Timeout cứng 30s cho khởi tạo
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
            js = (JavascriptExecutor) driver;

            log("Browser opened (Crash-Fix Mode)");
        } catch (Exception e) {
            System.err.println("!!! LOI KHOI TAO CHROME: " + e.getMessage());
            // In ra để debug xem lỗi gì nếu vẫn chết
            e.printStackTrace();
        }
    }

    protected void close() {
        try {
            if (driver != null) {
                driver.quit(); 
                driver = null;
            }
        } catch (Exception ignored) {}
        log("Browser closed");
    }

    protected void waitMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected void scrollBy(int px) {
        if (js != null) {
            try {
                js.executeScript("window.scrollBy(0, arguments[0]);", px);
                waitMs(500);
            } catch (Exception e) {
                log("Scroll error: " + e.getMessage());
            }
        }
    }

    protected void log(String msg) {
        System.out.println("[SeleniumBase] " + msg);
    }
}