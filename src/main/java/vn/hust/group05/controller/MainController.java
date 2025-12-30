package vn.hust.group05.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter; // Nhớ import cái này
import vn.hust.group05.model.Post;
import vn.hust.group05.service.*;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class MainController {

    // === UI COMPONENTS ===
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private ProgressIndicator loadingSpinner;

    // Table
    @FXML private TableView<Post> dataTable;
    @FXML private TableColumn<Post, String> colDate;
    @FXML private TableColumn<Post, String> colDamage;
    @FXML private TableColumn<Post, String> colSentiment;
    @FXML private TableColumn<Post, String> colTitle;

    // Charts
    @FXML private PieChart sentimentChart;
    @FXML private BarChart<String, Number> damageChart;
    @FXML private StackedBarChart<String, Number> reliefChart;
    @FXML private LineChart<String, Number> trendChart;
    @FXML private BarChart<String, Number> locationChart;

    // === DATA & SERVICES ===
    private ObservableList<Post> postList = FXCollections.observableArrayList();
    private IDataCollector dataCollector = new RealCollector(); 
    private DataAnalyzer analyzer = new DataAnalyzer();

    @FXML
    public void initialize() {
        // Cấu hình bảng
        colDate.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colDamage.setCellValueFactory(new PropertyValueFactory<>("damageType"));
        colSentiment.setCellValueFactory(new PropertyValueFactory<>("sentiment"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        dataTable.setItems(postList);

        // Double Click mở link
        dataTable.setRowFactory(tv -> {
            TableRow<Post> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Post rowData = row.getItem();
                    openWebpage(rowData.getUrl());
                }
            });
            return row;
        });

        // --- FIX LỖI SỐ LẺ TRÊN BIỂU ĐỒ (0.5, 1.5...) ---
        makeAxisInteger(damageChart);
        makeAxisInteger(reliefChart);
        makeAxisInteger(trendChart);
        makeAxisInteger(locationChart);
    }

    @FXML
    protected void onSearchButtonClick() {
        String keyword = searchField.getText();
        if (keyword == null || keyword.isEmpty()) return;

        loadingSpinner.setVisible(true);
        searchButton.setDisable(true);
        System.out.println("Dang tim kiem: " + keyword);

        new Thread(() -> {
            try {
                // Scrape
                List<Post> rawPosts = dataCollector.collect(keyword);
                
                // Analyze (Logic mới: Gộp 5 bài/lần)
                analyzer.analyzeAll(rawPosts);

                Platform.runLater(() -> {
                    postList.clear();
                    postList.addAll(rawPosts);
                    updateAllCharts(rawPosts);
                    
                    loadingSpinner.setVisible(false);
                    searchButton.setDisable(false);
                    System.out.println("Hoan thanh!");
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    loadingSpinner.setVisible(false);
                    searchButton.setDisable(false);
                });
            }
        }).start();
    }

    @SuppressWarnings("unchecked")
    private void updateAllCharts(List<Post> posts) {
        // A. Sentiment
        Map<String, Integer> sentStats = analyzer.getSentimentStats(posts);
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        sentStats.forEach((k, v) -> { if (v > 0) pieData.add(new PieChart.Data(k, v)); });
        sentimentChart.setData(pieData);

        // B. Damage
        Map<String, Integer> dmgStats = analyzer.getDamageStats(posts);
        XYChart.Series<String, Number> dmgSeries = new XYChart.Series<>();
        dmgSeries.setName("Cases");
        dmgStats.forEach((k, v) -> dmgSeries.getData().add(new XYChart.Data<>(k, v)));
        damageChart.getData().clear();
        damageChart.getData().add(dmgSeries);

        // C. Relief
        reliefChart.getData().clear();
        XYChart.Series<String, Number> sPos = new XYChart.Series<>(); sPos.setName("Positive");
        XYChart.Series<String, Number> sNeg = new XYChart.Series<>(); sNeg.setName("Negative");
        XYChart.Series<String, Number> sNeu = new XYChart.Series<>(); sNeu.setName("Neutral");
        
        Map<String, Map<String, Integer>> reliefData = analyzer.getReliefStats(posts);
        reliefData.forEach((relief, sents) -> {
            sPos.getData().add(new XYChart.Data<>(relief, sents.getOrDefault("Positive", 0)));
            sNeg.getData().add(new XYChart.Data<>(relief, sents.getOrDefault("Negative", 0)));
            sNeu.getData().add(new XYChart.Data<>(relief, sents.getOrDefault("Neutral", 0)));
        });
        reliefChart.getData().addAll(sPos, sNeg, sNeu);

        // D. Trend
        trendChart.getData().clear();
        XYChart.Series<String, Number> trendSeries = new XYChart.Series<>();
        trendSeries.setName("Posts per Day");
        analyzer.getTrendStats(posts).forEach((k, v) -> trendSeries.getData().add(new XYChart.Data<>(k, v)));
        trendChart.getData().add(trendSeries);

        // E. Location
        locationChart.getData().clear();
        XYChart.Series<String, Number> locSeries = new XYChart.Series<>();
        locSeries.setName("Mentions");
        analyzer.getLocationStats(posts).forEach((k, v) -> {
            if (v > 0) locSeries.getData().add(new XYChart.Data<>(k, v));
        });
        locationChart.getData().add(locSeries);
    }

    private void openWebpage(String urlString) {
        try {
            if (urlString != null && !urlString.isEmpty()) {
                Desktop.getDesktop().browse(new URI(urlString));
            }
        } catch (Exception e) {
            System.out.println("Khong mo duoc link: " + e.getMessage());
        }
    }

    // --- HÀM SỬA TRỤC SỐ (CHỈ HIỆN SỐ NGUYÊN) ---
    private void makeAxisInteger(XYChart<?, Number> chart) {
        if (chart == null) return;
        NumberAxis axis = (NumberAxis) chart.getYAxis();
        axis.setMinorTickVisible(false); // Tắt vạch nhỏ
        axis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                // Nếu là số nguyên thì hiện, số lẻ thì ẩn
                if (object.doubleValue() % 1 == 0) {
                    return String.format("%.0f", object.doubleValue());
                }
                return "";
            }
            @Override
            public Number fromString(String string) { return null; }
        });
    }
}