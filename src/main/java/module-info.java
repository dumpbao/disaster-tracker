module vn.hust.group05 {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires com.google.gson;
    requires java.net.http;

    opens vn.hust.group05 to javafx.fxml;
    exports vn.hust.group05;
    
    // Mở quyền cho Controller hoạt động
    opens vn.hust.group05.controller to javafx.fxml;

    // === DÒNG QUAN TRỌNG ĐỂ SỬA LỖI CỦA BẠN ===
    // Cấp quyền cho JavaFX (để hiện bảng) và Gson (để đọc JSON) truy cập vào model
    opens vn.hust.group05.model to javafx.base, com.google.gson;
}