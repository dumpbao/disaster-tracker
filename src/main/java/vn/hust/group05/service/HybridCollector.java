package vn.hust.group05.service;

import vn.hust.group05.model.Post;
import java.util.ArrayList;
import java.util.List;

public class HybridCollector implements IDataCollector {

    private IDataCollector apiCollector = new GuardianCollector();
    private IDataCollector scraperCollector = new RealCollector(); 

    @Override
    public List<Post> collect(String keyword) {
        List<Post> combinedResults = new ArrayList<>();
        
        // Xử lý từ khóa chính để tìm cho Scraper
        String[] keywords = keyword.toLowerCase().split("\\s+");
        String mainKeyword = keywords[0]; 
        if (keywords.length > 1 && mainKeyword.length() < 3) { 
            mainKeyword = keywords[1];
        }

        // 1. GỌI API THE GUARDIAN
        // SỬA ĐỔI: Tin tưởng API, lấy hết kết quả trả về, KHÔNG LỌC bằng code nữa
        System.out.println("--- STEP 1: Goi API The Guardian ---");
        List<Post> apiPosts = apiCollector.collect(keyword);
        combinedResults.addAll(apiPosts); 
        
        System.out.println("-> API lay duoc: " + apiPosts.size() + " bai (Lay tat ca).");

        // 2. GỌI SELENIUM (VNEXPRESS)
        System.out.println("--- STEP 2: Goi Selenium (Search: " + mainKeyword + ") ---");
        try {
            List<Post> scrapedPosts = scraperCollector.collect(mainKeyword);
            
            int countSelenium = 0;
            for (Post p : scrapedPosts) {
                // SỬA ĐỔI: Kiểm tra cả Tiêu đề HOẶC Nội dung
                String title = p.getTitle().toLowerCase();
                String content = p.getContent() != null ? p.getContent().toLowerCase() : "";

                if (title.contains(mainKeyword) || content.contains(mainKeyword)) {
                    combinedResults.add(p);
                    countSelenium++;
                }
            }
            System.out.println("-> Selenium lay duoc: " + countSelenium + " bai (da loc).");
            
        } catch (Exception e) {
            System.out.println("!!! Selenium gap loi: " + e.getMessage());
        }

        System.out.println("=== TONG HOP: " + combinedResults.size() + " BAI VIET ===");
        return combinedResults;
    }
}