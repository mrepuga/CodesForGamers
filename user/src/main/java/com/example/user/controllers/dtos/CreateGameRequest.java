package com.example.user.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreateGameRequest {

    private final String name;
    private final String description;
    private final String publisher;
    private final Float price;
}
