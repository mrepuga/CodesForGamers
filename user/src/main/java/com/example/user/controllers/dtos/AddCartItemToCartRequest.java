package com.example.user.controllers.dtos;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;
@Getter
@AllArgsConstructor
public class AddCartItemToCartRequest {
        @NonNull
        private List<Long> cartItemIds;



}
