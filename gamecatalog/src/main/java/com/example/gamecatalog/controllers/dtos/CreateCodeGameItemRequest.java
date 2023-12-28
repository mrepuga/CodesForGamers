package com.example.gamecatalog.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateCodeGameItemRequest {
    private final Long gameId;
    private  final String code;
    private  final String platform;

}
