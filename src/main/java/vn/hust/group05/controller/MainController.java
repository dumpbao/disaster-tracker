package vn.hust.group05.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import vn.hust.group05.model.Post;
import vn.hust.group05.service.DummyCollector;
import vn.hust.group05.service.IDataCollector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {

    @FXML private TextField searchField;
    
    // Bảng dữ liệu
    @FXML private TableView<Post> dataTable;
    @FXML private TableColumn<Post, String> colPlatform;
    @FXML private TableColumn<Post, String> colDate;
    @FXML private TableColumn<Post, String> colDamage;
    @FXML private TableColumn<Post, String> colSentiment;
    @FXML private TableColumn<Post, String> colTitle;

    // Các biểu đồ
    @FXML private PieChart sentimentChart;
    @FXML private BarChart<String, Number> damageChart;

    private ObservableList<Post> postList = FXCollections.observableArrayList();
    private IDataCollector dataCollector = new DummyCollector();

    @FXML
    public void initialize() {
        // Cấu hình cột bảng
        colPlatform.setCellValueFactory(new PropertyValueFactory<>("platform"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colDamage.setCellValueFactory(new PropertyValueFactory<>("damageType"));
        colSentiment.setCellValueFactory(new PropertyValueFactory<>("sentiment"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));

        dataTable.setItems(postList);
    }

    @FXML
    protected void onSearchButtonClick() {
        String keyword = searchField.getText();
        postList.clear();
        
        // 1. Lấy dữ liệu
        List<Post> results = dataCollector.collect(keyword);
        postList.addAll(results);
        
        // 2. Cập nhật biểu đồ
        updateCharts();
        
        // Chuyển sang tab Dashboard (tùy chọn, ở đây mình để user tự bấm chuyển)
    }
    
    private void updateCharts() {
        // --- Cập nhật PieChart (Cảm xúc) ---
        int positive = 0, negative = 0, neutral = 0;
        for (Post p : postList) {
            if ("Positive".equals(p.getSentiment())) positive++;
            else if ("Negative".equals(p.getSentiment())) negative++;
            else neutral++;
        }
        sentimentChart.setData(FXCollections.observableArrayList(
            new PieChart.Data("Positive", positive),
            new PieChart.Data("Negative", negative),
            new PieChart.Data("Neutral", neutral)
        ));

        // --- Cập nhật BarChart (Thiệt hại) ---
        // Dùng Map để đếm số lượng từng loại thiệt hại
        Map<String, Integer> damageCounts = new HashMap<>();
        for (Post p : postList) {
            String type = p.getDamageType();
            damageCounts.put(type, damageCounts.getOrDefault(type, 0) + 1);
        }

        // Tạo Series dữ liệu cho BarChart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Number of Posts");
        
        for (Map.Entry<String, Integer> entry : damageCounts.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        damageChart.getData().clear();
        damageChart.getData().add(series);
    }
}