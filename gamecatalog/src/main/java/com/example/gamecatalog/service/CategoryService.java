package com.example.gamecatalog.service;

import com.example.gamecatalog.controllers.dtos.CreateCategoryRequest;
import com.example.gamecatalog.entities.Category;
import com.example.gamecatalog.entities.dto.CategoryDTO;
import com.example.gamecatalog.repositories.CategoryRepository;
import com.example.gamecatalog.repositories.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.gamecatalog.entities.Game;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private GameRepository gameRepository;


    public Category createCategory(String name, String description){

        Category category = Category.builder().name(name.toUpperCase()).description(description).build();
        return categoryRepository.save(category);

    }


    public Category updateGame(Long id, CreateCategoryRequest updatedCategory) {

        Optional<Category> existingCategoryOptional = categoryRepository.findById(id);

        if (existingCategoryOptional.isPresent()) {
            Category existingCategory = existingCategoryOptional.get();


            existingCategory.setName(updatedCategory.getName());
            existingCategory.setDescription(updatedCategory.getDescription());

            return categoryRepository.save(existingCategory);
        } else {
            return null;
        }

    }
}
