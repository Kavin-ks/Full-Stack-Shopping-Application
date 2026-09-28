package com.flipkart.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
public class User {

    @Id
    private String id;
    private String name;
    @Indexed(unique = true, sparse = true)
    private String email;
    @Indexed(unique = true, sparse = true)
    private String mobile;
    private String gender;
    /** USER or ADMIN (seller hub access). */
    private String role = "USER";
    private List<Address> addresses = new ArrayList<>();
    /** Product ids, most recently added first. */
    private List<String> wishlist = new ArrayList<>();
    private Instant createdAt = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public List<Address> getAddresses() { return addresses; }
    public void setAddresses(List<Address> addresses) { this.addresses = addresses; }
    public List<String> getWishlist() { return wishlist; }
    public void setWishlist(List<String> wishlist) { this.wishlist = wishlist; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
