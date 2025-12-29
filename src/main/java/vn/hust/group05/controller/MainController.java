package vn.hust.group05.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import vn.hust.group05.model.Post;
import vn.hust.group05.service.*;

import java.util.List;
import java.util.Map;

public class MainController {

    // === KHAI BÁO FXML ===
    @FXML private TextField searchField;
    
    // Bảng dữ liệu
    @FXML private TableView<Post> dataTable;
    @FXML private TableColumn<Post, String> colPlatform;
    @FXML private TableColumn<Post, String> colDate;
    @FXML private TableColumn<Post, String> colDamage;
    @FXML private TableColumn<Post, String> colSentiment;
    @FXML private TableColumn<Post, String> colTitle;

    // Biểu đồ
    @FXML private PieChart sentimentChart;
    @FXML private BarChart<String, Number> damageChart;
    @FXML private StackedBarChart<String, Number> reliefChart; // Biểu đồ mới

    // === DỮ LIỆU & SERVICE ===
    // Dùng 1 list duy nhất để đồng bộ dữ liệu giữa Controller và Giao diện
    private ObservableList<Post> postList = FXCollections.observableArrayList();
    
    private IDataCollector dataCollector = new HybridCollector(); // Dùng Guardian để lấy tin cũ
    private DataAnalyzer analyzer = new DataAnalyzer();

    @FXML
    public void initialize() {
        // Cấu hình các cột cho bảng
        // Lưu ý: Chuỗi trong PropertyValueFactory phải trùng tên biến trong class Post
        colPlatform.setCellValueFactory(new PropertyValueFactory<>("source")); // Post.java dùng 'source', không phải 'platform'
        colDate.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colDamage.setCellValueFactory(new PropertyValueFactory<>("damageType"));
        colSentiment.setCellValueFactory(new PropertyValueFactory<>("sentiment"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));

        // Gán list dữ liệu vào bảng
        dataTable.setItems(postList);
    }

    @FXML
    protected void onSearchButtonClick() {
        String keyword = searchField.getText();
        if (keyword == null || keyword.isEmpty()) {
            return;
        }

        // 1. Lấy dữ liệu từ API
        List<Post> rawPosts = dataCollector.collect(keyword);
        
        // 2. Cập nhật vào bảng (Xóa cũ, thêm mới)
        postList.clear();
        postList.addAll(rawPosts);

        // 3. Cập nhật tất cả biểu đồ
        updateAllCharts(rawPosts);
    }

    // Hàm riêng để vẽ biểu đồ cho gọn code
    @SuppressWarnings("unchecked")
    private void updateAllCharts(List<Post> posts) {
        
        // --- A. Vẽ PieChart (Cảm xúc) ---
        Map<String, Integer> sentimentStats = analyzer.analyzeSentiment(posts);
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : sentimentStats.entrySet()) {
            if (entry.getValue() > 0) {
                pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
        }
        sentimentChart.setData(pieData);
        sentimentChart.setTitle("Sentiment Overview");

        // --- B. Vẽ BarChart (Thiệt hại) ---
        Map<String, Integer> damageStats = analyzer.analyzeDamageType(posts);
        XYChart.Series<String, Number> damageSeries = new XYChart.Series<>();
        damageSeries.setName("Cases");
        
        for (Map.Entry<String, Integer> entry : damageStats.entrySet()) {
            damageSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        damageChart.getData().clear();
        damageChart.getData().add(damageSeries);
        damageChart.setTitle("Damage Statistics");

        // --- C. Vẽ StackedBarChart (Cứu trợ & Cảm xúc) ---
        // Cấu trúc Stacked cần 3 series riêng biệt cho Positive, Negative, Neutral
        XYChart.Series<String, Number> sPositive = new XYChart.Series<>(); sPositive.setName("Positive");
        XYChart.Series<String, Number> sNegative = new XYChart.Series<>(); sNegative.setName("Negative");
        XYChart.Series<String, Number> sNeutral  = new XYChart.Series<>(); sNeutral.setName("Neutral");

        // Lấy dữ liệu phân tích phức hợp từ Analyzer
        Map<String, Map<String, Integer>> reliefData = analyzer.analyzeReliefAndSentiment(posts);

        for (String reliefType : reliefData.keySet()) {
            Map<String, Integer> sents = reliefData.get(reliefType);
            
            // Thêm dữ liệu vào từng cột chồng
            sPositive.getData().add(new XYChart.Data<>(reliefType, sents.getOrDefault("Positive", 0)));
            sNegative.getData().add(new XYChart.Data<>(reliefType, sents.getOrDefault("Negative", 0)));
            sNeutral.getData().add(new XYChart.Data<>(reliefType, sents.getOrDefault("Neutral", 0)));
        }

        reliefChart.getData().clear();
        reliefChart.getData().addAll(sPositive, sNegative, sNeutral);
        reliefChart.setTitle("Relief Efforts & Sentiment");
    }
}