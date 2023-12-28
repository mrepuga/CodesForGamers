package com.example.user.controllers.dtos;

import com.example.user.entities.OrderCart;

import java.time.LocalDateTime;
import java.util.List;

public class MergedOrderCartItem {

    private OrderCart orderCart;
    private List<MergedCartItem> mergedCartItems;
    private LocalDateTime time;

    public MergedOrderCartItem(OrderCart orderCart, List<MergedCartItem> mergedCartItems, LocalDateTime time) {
        this.orderCart = orderCart;
        this.mergedCartItems = mergedCartItems;
        this.time = time;
    }

    public OrderCart getOrderCart() {
        return orderCart;
    }

    public void setOrderCart(OrderCart orderCart) {
        this.orderCart = orderCart;
    }

    public List<MergedCartItem> getMergedCartItems() {
        return mergedCartItems;
    }

    public void setMergedCartItems(List<MergedCartItem> mergedCartItems) {
        this.mergedCartItems = mergedCartItems;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

}
