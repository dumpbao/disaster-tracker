package vn.hust.group05.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class GeminiService {

    private static final String API_KEY = "AIzaSyDUuH4iUkosm1ax_nlxShJDyKH-Y0iby3w"; // Nhớ điền API Key
    private static final String MODEL_NAME = "models/gemini-2.5-flash"; // Dùng bản 2.0 Flash xử lý batch tốt hơn Lite
    
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/" 
                                          + MODEL_NAME 
                                          + ":generateContent?key=" + API_KEY;

    // Hàm này bây giờ chỉ nhận text và gửi đi, KHÔNG thêm prompt thừa
    public String askGemini(String finalPrompt) {
        try {
            // JSON Body chuẩn
            String jsonBody = "{\n" +
                    "  \"contents\": [\n" +
                    "    {\n" +
                    "      \"role\": \"user\",\n" +
                    "      \"parts\": [\n" +
                    "        { \"text\": \"" + escapeJson(finalPrompt) + "\" }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"generationConfig\": {\n" +
                    "       \"temperature\": 0.3,\n" + // Giảm nhiệt độ để kết quả ổn định, bớt sáng tạo linh tinh
                    "       \"maxOutputTokens\": 8192\n" +
                    "  }\n" +
                    "}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) System.out.print(response.statusCode() + response.body());

            return extractTextFromResponse(response.body());

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "");
    }

    private String extractTextFromResponse(String jsonResponse) {
        try {
            // Parsing thủ công để tránh thư viện ngoài, nhưng cần cẩn thận
            int textKeyIndex = jsonResponse.indexOf("\"text\"");
            if (textKeyIndex == -1) return "";

            int startQuote = jsonResponse.indexOf("\"", textKeyIndex + 6);
            if (startQuote == -1) return "";
            
            // Tìm endQuote nhưng phải bỏ qua escaped quote (\")
            int endQuote = startQuote + 1;
            while (endQuote < jsonResponse.length()) {
                if (jsonResponse.charAt(endQuote) == '"' && jsonResponse.charAt(endQuote - 1) != '\\') {
                    break;
                }
                endQuote++;
            }
            
            if (endQuote >= jsonResponse.length()) return "";
            
            String result = jsonResponse.substring(startQuote + 1, endQuote);
            // Unescape ký tự xuống dòng để tách mảng sau này
            return result.replace("\\n", "\n").trim();
        } catch (Exception e) {
            return "";
        }
    }
}