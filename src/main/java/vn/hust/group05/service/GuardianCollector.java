package vn.hust.group05.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import vn.hust.group05.model.Post;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class GuardianCollector implements IDataCollector {

    // === THAY KEY CỦA BẠN VÀO ĐÂY ===
    private static final String API_KEY = "ad5d7bfa-d787-4c3b-844f-8485a124819d"; // Thay bằng key bạn vừa đăng ký, ví dụ: "a1b2-c3d4..."

    @Override
    public List<Post> collect(String keyword) {
        List<Post> results = new ArrayList<>();
        
        // URL của The Guardian: Tìm tin cũ thoải mái, lấy thêm trường bodyText để có nội dung
        String query = keyword.replace(" ", "%20");
        String url = "https://content.guardianapis.com/search?q=" + query 
           + "&api-key=" + API_KEY 
           + "&show-fields=bodyText&page-size=50&order-by=relevance";

        try {
            System.out.println("Dang goi The Guardian API: " + url);
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Cấu trúc JSON của Guardian: { "response": { "results": [...] } }
                JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonObject resp = root.getAsJsonObject("response");
                
                if (resp.has("results")) {
                    JsonArray articles = resp.getAsJsonArray("results");
                    
                    for (JsonElement element : articles) {
                        JsonObject article = element.getAsJsonObject();
                        
                        String title = article.get("webTitle").getAsString();
                        String time = article.get("webPublicationDate").getAsString();
                        String source = "The Guardian";
                        
                        // Lấy nội dung tóm tắt (cắt 200 ký tự cho gọn)
                        String content = "";
                        if (article.has("fields")) {
                            content = article.getAsJsonObject("fields").get("bodyText").getAsString();
                            if (content.length() > 200) content = content.substring(0, 200) + "...";
                        }

                        Post p = new Post(title, content, source, time, "GuardianAPI");
                        
                        // Phân tích sơ bộ để vẽ biểu đồ (Cái này phải tự làm vì API không trả về sentiment)
                        analyzePost(p);
                        
                        results.add(p);
                    }
                }
            } else {
                System.out.println("Loi API: " + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("Tim thay " + results.size() + " bai viet that tu The Guardian.");
        return results;
    }

    private void analyzePost(Post p) {
        String text = (p.getTitle() + " " + p.getContent()).toLowerCase();
        
        // Logic đơn giản để gán nhãn dữ liệu thật
        if (text.contains("death") || text.contains("kill") || text.contains("destroy") || text.contains("damage")) {
            p.setSentiment("Negative");
            p.setDamageType(text.contains("flood") ? "Flood" : "Infrastructure");
        } else if (text.contains("aid") || text.contains("rescue") || text.contains("help") || text.contains("fund")) {
            p.setSentiment("Positive");
            p.setReliefType("Money");
        } else {
            p.setSentiment("Neutral");
            p.setDamageType("None");
        }
    }
}