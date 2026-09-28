package com.flipkart.user.service;

import com.flipkart.user.dto.Dtos.*;
import com.flipkart.user.exception.ApiException;
import com.flipkart.user.model.Address;
import com.flipkart.user.model.User;
import com.flipkart.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private static final int MAX_ADDRESSES = 10;

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    public User get(String userId) {
        return users.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
    }

    // ------------------------------------------------------------------ profile

    public UserView updateProfile(String userId, ProfileUpdate req) {
        User user = get(userId);
        String email = StringUtils.hasText(req.email()) ? req.email().trim().toLowerCase() : null;
        String mobile = StringUtils.hasText(req.mobile()) ? req.mobile().trim() : null;
        if (email == null && mobile == null) throw ApiException.badRequest("Either email or mobile number is required");

        if (email != null) users.findByEmail(email).filter(u -> !u.getId().equals(userId))
                .ifPresent(u -> { throw ApiException.conflict("This email is linked to another account"); });
        if (mobile != null) users.findByMobile(mobile).filter(u -> !u.getId().equals(userId))
                .ifPresent(u -> { throw ApiException.conflict("This mobile number is linked to another account"); });

        user.setName(req.name().trim());
        user.setEmail(email);
        user.setMobile(mobile);
        user.setGender(req.gender());
        return AuthService.toView(users.save(user));
    }

    // ------------------------------------------------------------------ addresses

    public List<Address> addresses(String userId) {
        return get(userId).getAddresses();
    }

    public List<Address> addAddress(String userId, AddressRequest req) {
        User user = get(userId);
        if (user.getAddresses().size() >= MAX_ADDRESSES) throw ApiException.badRequest("You can save up to 10 addresses");
        Address a = new Address();
        a.setId(UUID.randomUUID().toString());
        apply(a, req);
        boolean first = user.getAddresses().isEmpty();
        user.getAddresses().add(0, a);
        if (first || req.makeDefault()) makeDefault(user, a.getId());
        return users.save(user).getAddresses();
    }

    public List<Address> updateAddress(String userId, String addressId, AddressRequest req) {
        User user = get(userId);
        Address a = find(user, addressId);
        apply(a, req);
        if (req.makeDefault()) makeDefault(user, addressId);
        return users.save(user).getAddresses();
    }

    public List<Address> deleteAddress(String userId, String addressId) {
        User user = get(userId);
        Address a = find(user, addressId);
        user.getAddresses().remove(a);
        if (a.isDefaultAddress() && !user.getAddresses().isEmpty()) {
            user.getAddresses().get(0).setDefaultAddress(true);
        }
        return users.save(user).getAddresses();
    }

    // ------------------------------------------------------------------ wishlist

    public List<String> wishlist(String userId) {
        return get(userId).getWishlist();
    }

    public List<String> addToWishlist(String userId, String productId) {
        User user = get(userId);
        user.getWishlist().remove(productId);
        user.getWishlist().add(0, productId);
        return users.save(user).getWishlist();
    }

    public List<String> removeFromWishlist(String userId, String productId) {
        User user = get(userId);
        user.getWishlist().remove(productId);
        return users.save(user).getWishlist();
    }

    // ------------------------------------------------------------------ helpers

    private Address find(User user, String addressId) {
        return user.getAddresses().stream().filter(a -> a.getId().equals(addressId)).findFirst()
                .orElseThrow(() -> ApiException.notFound("Address not found"));
    }

    private void makeDefault(User user, String addressId) {
        user.getAddresses().forEach(x -> x.setDefaultAddress(x.getId().equals(addressId)));
    }

    private void apply(Address a, AddressRequest req) {
        a.setName(req.name().trim());
        a.setMobile(req.mobile());
        a.setPincode(req.pincode());
        a.setLocality(req.locality().trim());
        a.setAddress(req.address().trim());
        a.setCity(req.city().trim());
        a.setState(req.state());
        a.setLandmark(req.landmark());
        a.setAlternatePhone(req.alternatePhone());
        a.setType(req.type() == null ? "HOME" : req.type());
    }
}
