package com.example.app.service;

import com.example.app.dto.CartDtos;
import com.example.app.entity.Cart;
import com.example.app.entity.CartItem;
import com.example.app.entity.Product;
import com.example.app.entity.User;
import com.example.app.kafka.CartItemEvent;
import com.example.app.repository.CartItemRepository;
import com.example.app.repository.CartRepository;
import com.example.app.repository.ProductRepository;
import com.example.app.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {
  private final CartRepository carts;
  private final CartItemRepository items;
  private final ProductRepository products;
  private final UserRepository users;
  private final KafkaTemplate<String, CartItemEvent> kafka;

  public CartService(
      CartRepository carts,
      CartItemRepository items,
      ProductRepository products,
      UserRepository users,
      KafkaTemplate<String, CartItemEvent> kafka) {
    this.carts = carts;
    this.items = items;
    this.products = products;
    this.users = users;
    this.kafka = kafka;
  }

  @Transactional
  public CartDtos.CartResponse add(String email, CartDtos.CartItemRequest request) {
    Cart cart = cart(email);
    Product product = product(request.productId());
    CartItem item =
        items
            .findByCartIdAndProductId(cart.getId(), product.getId())
            .orElseGet(
                () -> {
                  CartItem created = new CartItem();
                  created.setCart(cart);
                  created.setProduct(product);
                  created.setQuantity(0);
                  return created;
                });
    change(item, product, item.getQuantity() + request.quantity(), "ADDED");
    return response(cart);
  }

  @Transactional(readOnly = true)
  public CartDtos.CartResponse get(String email) {
    return response(cart(email));
  }

  @Transactional
  public CartDtos.CartResponse update(
      String email, Long productId, CartDtos.QuantityRequest request) {
    Cart cart = cart(email);
    CartItem item =
        items
            .findByCartIdAndProductId(cart.getId(), productId)
            .orElseThrow(
                () -> new jakarta.persistence.EntityNotFoundException("Cart item not found"));
    change(item, item.getProduct(), request.quantity(), "UPDATED");
    return response(cart);
  }

  @Transactional
  public void delete(String email, Long productId) {
    Cart cart = cart(email);
    CartItem item =
        items
            .findByCartIdAndProductId(cart.getId(), productId)
            .orElseThrow(
                () -> new jakarta.persistence.EntityNotFoundException("Cart item not found"));
    Product product = item.getProduct();
    product.setAvailableQuantity(product.getAvailableQuantity() + item.getQuantity());
    products.save(product);
    Long itemId = item.getId();
    items.delete(item);
    kafka.send(
        "cart-item-events",
        String.valueOf(productId),
        new CartItemEvent("REMOVED", cart.getUser().getId(), itemId, productId, 0, Instant.now()));
  }

  private void change(CartItem item, Product product, int quantity, String type) {
    int delta = quantity - item.getQuantity();
    if (delta > product.getAvailableQuantity()) {
      throw new IllegalArgumentException("Requested quantity exceeds availability");
    }
    product.setAvailableQuantity(product.getAvailableQuantity() - delta);
    products.save(product);
    item.setQuantity(quantity);
    CartItem saved = items.save(item);
    kafka.send(
        "cart-item-events",
        String.valueOf(product.getId()),
        new CartItemEvent(
            type,
            saved.getCart().getUser().getId(),
            saved.getId(),
            product.getId(),
            quantity,
            Instant.now()));
  }

  private Cart cart(String email) {
    return carts
        .findByUserEmail(email)
        .orElseGet(
            () -> {
              User user =
                  users
                      .findByEmail(email)
                      .orElseThrow(
                          () -> new jakarta.persistence.EntityNotFoundException("User not found"));
              Cart cart = new Cart();
              cart.setUser(user);
              return carts.save(cart);
            });
  }

  private Product product(Long id) {
    return products
        .findById(id)
        .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Product not found"));
  }

  private CartDtos.CartResponse response(Cart cart) {
    List<CartDtos.CartItemResponse> result =
        items.findAll().stream()
            .filter(item -> item.getCart().getId().equals(cart.getId()))
            .map(
                item ->
                    new CartDtos.CartItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getPrice(),
                        item.getQuantity(),
                        item.getProduct()
                            .getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()))))
            .toList();
    BigDecimal total =
        result.stream()
            .map(CartDtos.CartItemResponse::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    return new CartDtos.CartResponse(cart.getId(), result, total);
  }
}
