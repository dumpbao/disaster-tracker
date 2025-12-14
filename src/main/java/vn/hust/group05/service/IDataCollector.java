package vn.hust.group05.service;

import vn.hust.group05.model.Post;
import java.util.List;

public interface IDataCollector {
    // Hàm này nhận vào từ khóa, thời gian và trả về danh sách bài viết
    List<Post> collect(String keyword);
}