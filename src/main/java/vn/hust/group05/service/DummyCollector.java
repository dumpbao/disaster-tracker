package vn.hust.group05.service;

import vn.hust.group05.model.Post;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DummyCollector implements IDataCollector {

    @Override
    public List<Post> collect(String keyword) {
        List<Post> results = new ArrayList<>();
        Random rand = new Random();
        
        String[] sentiments = {"Positive", "Negative", "Neutral"};
        String[] platforms = {"Twitter", "Facebook", "YouTube"};
        // Các loại thiệt hại theo đề bài Problem 2
        String[] damages = {"Flood", "Landslide", "House Damaged", "People Affected", "None"};

        for (int i = 1; i <= 30; i++) { // Tăng lên 30 bài cho nhiều
            String platform = platforms[rand.nextInt(platforms.length)];
            String sentiment = sentiments[rand.nextInt(sentiments.length)];
            String damage = damages[rand.nextInt(damages.length)];
            
            Post p = new Post(
                "Report #" + i + ": " + keyword + " situation update", 
                "Serious damage reported in area " + i, 
                "User" + i, 
                "2025-09-" + (10 + i%10), 
                platform
            );
            
            p.setSentiment(sentiment);
            p.setDamageType(damage); // Gán loại thiệt hại giả
            results.add(p);
        }
        
        return results;
    }
}