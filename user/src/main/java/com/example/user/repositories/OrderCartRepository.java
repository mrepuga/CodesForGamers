package com.example.user.repositories;

import com.example.user.entities.OrderCart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderCartRepository extends JpaRepository<OrderCart, Long> {
    List<OrderCart> findByUserId(Long id);
}
