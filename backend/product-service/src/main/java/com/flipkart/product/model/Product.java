package com.flipkart.product.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "products")
public class Product {

    @Id
    private String id;
    private String title;
    @Indexed
    private String brand;
    private String description;
    @Indexed
    private String category;
    private String categoryName;
    private String subCategory;
    private int mrp;
    private int price;
    private int discount;
    private double rating;
    private int ratingCount;
    private int reviewCount;
    /** Units available to sell. Decremented atomically when an order is placed. */
    private int stock;
    private String thumbnail;
    private List<String> images = new ArrayList<>();
    private List<String> highlights = new ArrayList<>();
    private Map<String, String> specifications = new LinkedHashMap<>();
    private String warranty;
    private String returnPolicy;
    private List<String> tags = new ArrayList<>();
    private boolean assured;
    private List<Review> reviews = new ArrayList<>();
    private Instant createdAt = Instant.now();

    /** Keeps MRP / price / discount consistent whenever prices are edited. */
    public void recalculateDiscount() {
        if (mrp < price) mrp = price;
        discount = mrp == 0 ? 0 : (int) Math.round((mrp - price) * 100.0 / mrp);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }
    public int getMrp() { return mrp; }
    public void setMrp(int mrp) { this.mrp = mrp; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public int getDiscount() { return discount; }
    public void setDiscount(int discount) { this.discount = discount; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public List<String> getHighlights() { return highlights; }
    public void setHighlights(List<String> highlights) { this.highlights = highlights; }
    public Map<String, String> getSpecifications() { return specifications; }
    public void setSpecifications(Map<String, String> specifications) { this.specifications = specifications; }
    public String getWarranty() { return warranty; }
    public void setWarranty(String warranty) { this.warranty = warranty; }
    public String getReturnPolicy() { return returnPolicy; }
    public void setReturnPolicy(String returnPolicy) { this.returnPolicy = returnPolicy; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public boolean isAssured() { return assured; }
    public void setAssured(boolean assured) { this.assured = assured; }
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
