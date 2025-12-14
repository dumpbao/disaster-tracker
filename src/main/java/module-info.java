module vn.hust.group05 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.google.gson;
    requires java.net.http;

    opens vn.hust.group05 to javafx.fxml;
    exports vn.hust.group05;
    opens vn.hust.group05.controller to javafx.fxml;
}