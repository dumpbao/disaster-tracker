package com.app.collector.model;

import java.time.LocalDateTime;

public class FacebookPostRaw {

    private String url;
    private String authorName;
    private String contentText;
    private LocalDateTime publishedTime;
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getAuthorName() {
		return authorName;
	}
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
	public String getContentText() {
		return contentText;
	}
	public void setContentText(String contentText) {
		this.contentText = contentText;
	}
	public LocalDateTime getPublishedTime() {
		return publishedTime;
	}
	public void setPublishedTime(LocalDateTime publishedTime) {
		this.publishedTime = publishedTime;
	}

    
}
