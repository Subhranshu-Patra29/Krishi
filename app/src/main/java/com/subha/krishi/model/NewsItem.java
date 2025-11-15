package com.subha.krishi.model;

public class NewsItem {
    private final String author;
    private final String title;
    private final String description;
    private final String url;
    private final String imageUrl;
    private final String publishedAt;
    private final boolean isUserBlog;

    public NewsItem(String author, String title, String description, String url, String imageUrl, String publishedAt, boolean isUserBlog) {
        this.author = author;
        this.title = title;
        this.description = description;
        this.url = url;
        this.imageUrl = imageUrl;
        this.publishedAt = publishedAt;
        this.isUserBlog = isUserBlog;
    }

    public String getAuthor() { return "Source: " + author; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getUrl() { return url; }
    public String getImageUrl()
    {
        if (imageUrl == null || imageUrl.isEmpty() || imageUrl.equals("null")) { return ""; }
        return imageUrl;
    }
    public String getPublishedAt() { return "Published: " + publishedAt.substring(0, 10); }
    public boolean isUserBlog() { return isUserBlog; }

}
