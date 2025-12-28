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
import vn.hust.group05.service.DummyCollector;
import vn.hust.group05.service.IDataCollector;
import vn.hust.group05.service.RealCollector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {

    @FXML private TextField searchField;
    @FXML private TableView<Post> dataTable;
    @FXML private TableColumn<Post, String> colPlatform;
    @FXML private TableColumn<Post, String> colDate;
    @FXML private TableColumn<Post, String> colDamage;
    @FXML private TableColumn<Post, String> colSentiment;
    @FXML private TableColumn<Post, String> colTitle;

    @FXML private PieChart sentimentChart;
    @FXML private BarChart<String, Number> damageChart;
    
    // Biểu đồ mới cho Problem 3
    @FXML private StackedBarChart<String, Number> reliefChart;
    @FXML private CategoryAxis xAxisRelief;

    private ObservableList<Post> postList = FXCollections.observableArrayList();
    private IDataCollector dataCollector = new RealCollector();

    @FXML
    public void initialize() {
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
        List<Post> results = dataCollector.collect(keyword);
        postList.addAll(results);
        updateCharts();
    }
    @SuppressWarnings("unchecked")
    private void updateCharts() {
        // 1. PieChart (Tổng quan cảm xúc)
        int pos = 0, neg = 0, neu = 0;
        for (Post p : postList) {
            switch (p.getSentiment()) {
                case "Positive" -> pos++;
                case "Negative" -> neg++;
                default -> neu++;
            }
        }
        sentimentChart.setData(FXCollections.observableArrayList(
            new PieChart.Data("Positive", pos),
            new PieChart.Data("Negative", neg),
            new PieChart.Data("Neutral", neu)
        ));

        // 2. BarChart (Thiệt hại)
        Map<String, Integer> dmgCounts = new HashMap<>();
        for (Post p : postList) {
            if (!"None".equals(p.getDamageType())) { // Bỏ qua cái None cho đỡ rác
                dmgCounts.put(p.getDamageType(), dmgCounts.getOrDefault(p.getDamageType(), 0) + 1);
            }
        }
        XYChart.Series<String, Number> dmgSeries = new XYChart.Series<>();
        dmgSeries.setName("Damage Count");
        for (var entry : dmgCounts.entrySet()) {
            dmgSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        damageChart.getData().clear();
        damageChart.getData().add(dmgSeries);

        // 3. StackedBarChart (Cứu trợ vs Cảm xúc)
        // Cần 3 series: Positive, Negative, Neutral
        XYChart.Series<String, Number> sPositive = new XYChart.Series<>(); sPositive.setName("Positive");
        XYChart.Series<String, Number> sNegative = new XYChart.Series<>(); sNegative.setName("Negative");
        XYChart.Series<String, Number> sNeutral = new XYChart.Series<>(); sNeutral.setName("Neutral");

        // Map<ReliefItem, Map<Sentiment, Count>>
        Map<String, Map<String, Integer>> reliefMap = new HashMap<>();

        for (Post p : postList) {
            String relief = p.getReliefType();
            if ("None".equals(relief)) continue; // Bỏ qua nếu không nói về cứu trợ

            reliefMap.putIfAbsent(relief, new HashMap<>());
            Map<String, Integer> sentMap = reliefMap.get(relief);
            sentMap.put(p.getSentiment(), sentMap.getOrDefault(p.getSentiment(), 0) + 1);
        }

        for (String item : reliefMap.keySet()) {
            Map<String, Integer> sents = reliefMap.get(item);
            sPositive.getData().add(new XYChart.Data<>(item, sents.getOrDefault("Positive", 0)));
            sNegative.getData().add(new XYChart.Data<>(item, sents.getOrDefault("Negative", 0)));
            sNeutral.getData().add(new XYChart.Data<>(item, sents.getOrDefault("Neutral", 0)));
        }

        reliefChart.getData().clear();
        reliefChart.getData().addAll(sPositive, sNegative, sNeutral);
    }
}