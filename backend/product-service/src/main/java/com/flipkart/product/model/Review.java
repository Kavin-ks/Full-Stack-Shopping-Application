package com.flipkart.product.model;

public class Review {
    private String userId;
    private String userName;
    private int rating;
    private String title;
    private String comment;
    private String date;

    public Review() {}

    public Review(String userId, String userName, int rating, String title, String comment, String date) {
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.title = title;
        this.comment = comment;
        this.date = date;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
