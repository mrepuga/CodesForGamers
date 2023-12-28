package com.example.gamecatalog.controllers.dtos;

import com.example.gamecatalog.entities.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Getter
@AllArgsConstructor
public class CreateGameRequest {

    private final String name;
    private final String description;
    private final String publisher;
    private final Float price;


}
