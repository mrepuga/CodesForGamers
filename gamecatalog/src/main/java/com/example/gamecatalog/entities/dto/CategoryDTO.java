package com.example.gamecatalog.entities.dto;

import com.example.gamecatalog.entities.Category;
import java.util.*;


public class CategoryDTO {

    private Long id;
    private String name;
    private String description;

    private List<Long> gameIds = new ArrayList<>();




    public CategoryDTO(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
    }





    public CategoryDTO(Optional<Category> category) {
        this(category.orElse(null));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Long> getGameIds() {
        return gameIds;
    }

    public void setGameIds(List<Long> gameIds) {
        this.gameIds = gameIds;
    }
}
