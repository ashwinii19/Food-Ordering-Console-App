package com.techlabs.foodzone.model;

public class Review {
    private int reviewId;
    private int userId;
    private int menuId;
    private String reviewText;
    private int rating;

    public Review(int reviewId, int userId, int menuId, String reviewText, int rating) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.menuId = menuId;
        this.reviewText = reviewText;
        this.rating = rating;
    }

    public int getReviewId() {
        return reviewId;
    }

    public int getUserId() {
        return userId;
    }

    public int getMenuId() {
        return menuId;
    }

    public String getReviewText() {
        return reviewText;
    }

    public int getRating() {
        return rating;
    }
}
