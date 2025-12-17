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
        String[] damages = {"Flood", "Landslide", "House Damaged", "None"};
        // Danh mục cứu trợ (Problem 3)
        String[] reliefs = {"Food", "Medicine", "Shelter", "Rescue Team", "Money", "None"};

        for (int i = 1; i <= 50; i++) { // Tăng lên 50 bài
            String platform = platforms[rand.nextInt(platforms.length)];
            String sentiment = sentiments[rand.nextInt(sentiments.length)];
            String damage = damages[rand.nextInt(damages.length)];
            String relief = reliefs[rand.nextInt(reliefs.length)];
            
            Post p = new Post(
                "Post #" + i + ": Update on " + keyword, 
                "Content discussing " + relief + " and " + damage, 
                "User" + i, 
                "2025-09-" + (10 + i%10), 
                platform
            );
            
            p.setSentiment(sentiment);
            p.setDamageType(damage);
            p.setReliefType(relief); // Gán loại cứu trợ
            
            results.add(p);
        }
        
        return results;
    }
}