package com.example.user.repositories;

import com.example.user.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart,Long> {
    Cart getCartById(Long id);

    Cart getCartByUserId(Long userId);
}
