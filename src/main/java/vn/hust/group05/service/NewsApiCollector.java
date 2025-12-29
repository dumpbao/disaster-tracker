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

public class NewsApiCollector implements IDataCollector {

    // Key của bạn đây
    private static final String API_KEY = "4953c61f5294479fad8574d7834055f7"; 

    @Override
    public List<Post> collect(String keyword) {
        List<Post> results = new ArrayList<>();
        // Thay khoảng trắng bằng %20 để không lỗi URL
        String query = keyword.replace(" ", "%20");
        String url = "https://newsapi.org/v2/everything?q=" + query + "&sortBy=publishedAt&apiKey=" + API_KEY;

        try {
            System.out.println("Dang goi API: " + url);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
                if (jsonObject.has("articles")) {
                    JsonArray articles = jsonObject.getAsJsonArray("articles");
                    for (JsonElement element : articles) {
                        JsonObject article = element.getAsJsonObject();
                        
                        // Lấy dữ liệu và kiểm tra null
                        String title = (!article.get("title").isJsonNull()) ? article.get("title").getAsString() : "No Title";
                        String desc = (article.has("description") && !article.get("description").isJsonNull()) ? article.get("description").getAsString() : "";
                        String time = (!article.get("publishedAt").isJsonNull()) ? article.get("publishedAt").getAsString() : "";
                        String source = article.get("source").getAsJsonObject().get("name").getAsString();

                        Post p = new Post(title, desc, source, time, "NewsAPI");
                        
                        // Fake phân tích để lên biểu đồ cho đẹp
                        if (title.toLowerCase().contains("damage") || title.toLowerCase().contains("dead")) {
                            p.setSentiment("Negative");
                            p.setDamageType("People Affected");
                        } else if (title.toLowerCase().contains("help") || title.toLowerCase().contains("support")) {
                            p.setSentiment("Positive");
                            p.setReliefType("Food");
                        } else {
                            p.setSentiment("Neutral");
                        }
                        
                        results.add(p);
                    }
                }
            } else {
                System.out.println("Loi API: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }
}