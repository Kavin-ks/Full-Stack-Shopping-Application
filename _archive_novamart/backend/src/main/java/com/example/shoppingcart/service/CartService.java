package com.example.shoppingcart.service;

import com.example.shoppingcart.dto.CartResponse;
import com.example.shoppingcart.model.CartItem;
import com.example.shoppingcart.model.Product;
import com.example.shoppingcart.repository.CartRepository;
import com.example.shoppingcart.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    public CartResponse getCart() {
        return buildResponse();
    }

    public CartResponse addItem(String productId, int quantity, Product fallbackProduct) {
        Optional<CartItem> existing = cartRepository.findByProductId(productId);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setSubTotal(item.getQuantity() * item.getPrice());
            cartRepository.save(item);
        } else {
            Optional<Product> prodOpt = productRepository.findById(productId);
            Product p = prodOpt.orElse(fallbackProduct);
            if (p != null) {
                CartItem newItem = new CartItem(null, p.getId(), p.getName(), p.getPrice(), quantity, p.getPrice() * quantity, p.getEmoji());
                cartRepository.save(newItem);
            }
        }
        return buildResponse();
    }

    public CartResponse updateQuantity(String id, int quantity) {
        Optional<CartItem> itemOpt = cartRepository.findById(id);
        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            if (quantity <= 0) {
                cartRepository.deleteById(id);
            } else {
                item.setQuantity(quantity);
                item.setSubTotal(quantity * item.getPrice());
                cartRepository.save(item);
            }
        }
        return buildResponse();
    }

    public CartResponse removeItem(String id) {
        cartRepository.deleteById(id);
        return buildResponse();
    }

    public CartResponse clearCart() {
        cartRepository.deleteAll();
        return buildResponse();
    }

    private CartResponse buildResponse() {
        List<CartItem> items = cartRepository.findAll();
        int totalQty = items.stream().mapToInt(CartItem::getQuantity).sum();
        double totalAmt = items.stream().mapToDouble(CartItem::getSubTotal).sum();
        return new CartResponse(items, totalQty, totalAmt);
    }
}
