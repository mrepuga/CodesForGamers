package com.example.user.services;

import com.example.user.entities.Cart;
import com.example.user.entities.CartItem;
import com.example.user.entities.OrderCart;
import com.example.user.repositories.CartItemRepository;
import com.example.user.repositories.CartRepository;
import com.example.user.repositories.OrderCartRepository;
import com.example.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderCartRepository orderCartRepository;


    public Cart addToCart(Long cartId, List<Long> cartItemIds) {
        Optional<Cart> optionalCart = cartRepository.findById(cartId);
        if (optionalCart.isPresent()) {
            Cart cart = optionalCart.get();

            List<CartItem> cartItems = cartItemRepository.findAllById(cartItemIds);

            // Add cart items to the cart
            cart.getCartItems().addAll(cartItems);

            // Update the total price or perform other necessary actions

            // Save the updated cart
            cartRepository.save(cart);

        }
        return null;

    }
    @Transactional
    public OrderCart buyCart(Long cartId) {
        Optional<Cart> cartOptional = cartRepository.findById(cartId);
        OrderCart orderCart = new OrderCart();
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();

            orderCart = createOrderCartFromCart(cart);

            for (CartItem cartItem : cart.getCartItems()) {
                cartItem.setCart(null);
                cartItem.setOrderCart(orderCart);
            }
            orderCartRepository.save(orderCart);

            return orderCart;
        } else {
            return orderCart;
        }
    }
    private OrderCart createOrderCartFromCart(Cart cart) {
        return OrderCart.builder()
                .user(cart.getUser())
                .purchaseDateTime(LocalDateTime.now())
                .cartItems(new ArrayList<>(cart.getCartItems())).
                totalPrice(calculateTotalPrice(cart.getCartItems()))
                .build();
    }

    private Float calculateTotalPrice(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(CartItem::getPrice)
                .reduce(0.0f, Float::sum);
    }


    public Cart getCartById(Long id) {
        return cartRepository.findById(id).get();
    }
}