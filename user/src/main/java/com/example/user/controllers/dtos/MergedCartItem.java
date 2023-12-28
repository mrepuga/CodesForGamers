package com.example.user.controllers.dtos;

import com.example.user.entities.CartItem;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
@Getter
@Setter
public class MergedCartItem {
    private CartItem cartItem;
    private Map<String, Object> gameDetails;

    public MergedCartItem(CartItem cartItem, Map<String, Object> gameDetails) {
        this.cartItem = cartItem;
        this.gameDetails = gameDetails;
    }




}
