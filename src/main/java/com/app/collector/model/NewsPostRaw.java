package com.app.collector.model;

import java.time.LocalDateTime;

public class NewsPostRaw {

    private String url;
    private String source;
    private String keyword;
    private String title;
    private String content;
    private LocalDateTime publishedTime;
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public LocalDateTime getPublishedTime() {
		return publishedTime;
	}
	public void setPublishedTime(LocalDateTime publishedTime) {
		this.publishedTime = publishedTime;
	}
    
}
