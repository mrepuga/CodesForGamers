package com.example.user.repositories;

import com.example.user.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    void deleteAllByCartId(Long id);

    List<CartItem> findByGameId(Long gameId);
}
