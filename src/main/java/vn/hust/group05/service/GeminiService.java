package vn.hust.group05.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class GeminiService {

    private static final String API_KEY = "AIzaSyASLw3kjZsJ-SKm2zUBlz38d7yJtZbBWrw"; 
    
    // --- SỬA THÀNH BẢN 2.0-FLASH (Bản ổn định, Quota cao) ---
    // Model này có trong danh sách của bạn: "models/gemini-2.0-flash"
    private static final String MODEL_NAME = "models/gemini-2.5-flash-lite";
    
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/" 
                                          + MODEL_NAME 
                                          + ":generateContent?key=" + API_KEY;

    public String askGemini(String newsContent) {
        try {
            // Prompt xử lý
            String prompt = "Analyze this news (Title + Content) about storm/flood in Vietnam:\n" +
                    "\"" + newsContent + "\"\n\n" +
                    "Return a single string with format: Sentiment|DamageType|ReliefType|Location\n" +
                    "Rules:\n" +
                    "- Sentiment: Positive, Negative, Neutral\n" +
                    "- DamageType: People, Infrastructure, Agriculture, None\n" +
                    "- ReliefType: Money, Goods, Forces, None\n" +
                    "- Location: Specific Province/City name (e.g. Hanoi, Lao Cai) or Unknown\n" +
                    "Example output: Negative|People|None|Lao Cai";

            String jsonBody = "{\n" +
                    "  \"contents\": [\n" +
                    "    {\n" +
                    "      \"role\": \"user\",\n" +
                    "      \"parts\": [\n" +
                    "        { \"text\": \"" + escapeJson(prompt) + "\" }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            // --- XỬ LÝ LỖI ---
            if (response.statusCode() != 200) {
                System.err.println("❌ LOI GOOGLE API (" + response.statusCode() + "): " + response.body());
                return "Neutral|None|None|Unknown";
            }

            return extractTextFromResponse(response.body());

        } catch (Exception e) {
            e.printStackTrace();
            return "Neutral|None|None|Unknown";
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }

    private String extractTextFromResponse(String jsonResponse) {
        try {
            int textKeyIndex = jsonResponse.indexOf("\"text\"");
            if (textKeyIndex == -1) return "Neutral|None|None|Unknown";

            int startQuote = jsonResponse.indexOf("\"", textKeyIndex + 6);
            if (startQuote == -1) return "Neutral|None|None|Unknown";
            
            int endQuote = jsonResponse.indexOf("\"", startQuote + 1);
            if (endQuote == -1) return "Neutral|None|None|Unknown";
            
            String result = jsonResponse.substring(startQuote + 1, endQuote);
            return result.replace("\\n", "").trim();
        } catch (Exception e) {
            return "Neutral|None|None|Unknown";
        }
    }
}