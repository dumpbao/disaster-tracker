package com.app.collector.crawler;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public abstract class BaseSeleniumCollector {

    protected WebDriver driver;
    protected JavascriptExecutor js;

    /**
     * Open browser if not opened
     */
    protected void open() {
        if (driver != null) return;


        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        
        options.addArguments(
                "--disable-blink-features=AutomationControlled",
                "--disable-notifications",
                "--disable-infobars",
                "--start-maximized",
                "--disable-gpu",
                "--disable-extensions",
                "--disable-dev-shm-usage",
                "--blink-settings=imagesEnabled=false"
        );

        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(java.time.Duration.ofSeconds(15));
        js = (JavascriptExecutor) driver;

        log("Browser opened");
    }

    /**
     * Close browser safely
     */
    protected void close() {
        try {
            if (driver != null) {
                driver.quit();
                driver = null;
            }
        } catch (Exception ignored) {}
        log("Browser closed");
    }

    /**
     * Sleep helper
     */
    protected void waitMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Scroll page vertically
     */
    protected void scrollBy(int px) {
        if (js != null) {
            js.executeScript("window.scrollBy(0, arguments[0]);", px);
            waitMs(700);
        }
    }

    protected void log(String msg) {
        System.out.println("[SeleniumBase] " + msg);
    }
}
