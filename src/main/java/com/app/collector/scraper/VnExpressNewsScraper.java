package com.app.collector.scraper;

import org.jsoup.nodes.Document;

public class VnExpressNewsScraper extends AbstractNewsScraper {

	@Override
	protected String extractTitle(Document doc) {
	    return doc.selectFirst("h1") != null
	            ? doc.selectFirst("h1").text()
	            : "";
	}

	@Override
	protected String extractContent(Document doc) {
	    return doc.select("article p").text();
	}

	@Override
	protected String extractTime(Document doc) {
	    return doc.select("span.date").text();
	}
}