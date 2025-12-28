module vn.hust.group05 {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires com.google.gson;
    requires java.net.http;
    
    // THÊM 3 DÒNG NÀY ĐỂ CHẠY SELENIUM
    requires org.seleniumhq.selenium.api;
    requires org.seleniumhq.selenium.chrome_driver; 
    requires org.seleniumhq.selenium.support;  // (Có thể cần hoặc không, cứ thêm cho chắc)

    opens vn.hust.group05 to javafx.fxml;
    exports vn.hust.group05;
    
    opens vn.hust.group05.controller to javafx.fxml;
    opens vn.hust.group05.model to javafx.base, com.google.gson;
}