package com.example.gamecatalog.entities.dto;

import com.example.gamecatalog.entities.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.example.gamecatalog.entities.Category;
public class GameDTO {
    private Long id;
    private String name;
    private String description;

    private String publisher;
    private Float price;
    private String imagePath;

    private String videoEmbed;
    private List<Long> categoryIds = new ArrayList<>();

    public GameDTO() {
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public GameDTO(Game game) {
        this.id = game.getId();
        this.name = game.getName();
        this.description = game.getDescription();
        this.publisher = game.getPublisher();
        this.price = game.getPrice();
        this.imagePath = game.getImagePath();
        this.videoEmbed = game.getVideoEmbed();
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


    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }


    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    public String getVideoEmbed() {
        return videoEmbed;
    }

    public void setVideoEmbed(String videoEmbed) {
        this.videoEmbed = videoEmbed;
    }
}
