package com.app.collector.fetcher;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class SeleniumHtmlFetcher implements htmlFetcher {

    private WebDriver driver;

    public SeleniumHtmlFetcher() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--start-maximized");

        this.driver = new ChromeDriver(options);
    }

    @Override
    public String fetch(String url) {
        try {
            driver.get(url);
            Thread.sleep(1500);
            return driver.getPageSource();
        } catch (Exception e) {
            return null;
        }
    }

    public void close() {
        if (driver != null) driver.quit();
    }
}
