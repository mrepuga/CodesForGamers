package com.example.user.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
@Getter
@AllArgsConstructor
public class CreateCartItemRequest {

    @NonNull
    private final Long gameId;

    @NonNull
    private final String platform;

    @NonNull
    private final Long cartId;


}
