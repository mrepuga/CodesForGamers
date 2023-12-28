package com.example.gamecatalog.controllers;

import com.example.gamecatalog.controllers.dtos.CreateCategoryRequest;
import com.example.gamecatalog.entities.Category;
import com.example.gamecatalog.entities.Game;
import com.example.gamecatalog.entities.dto.CategoryDTO;
import com.example.gamecatalog.entities.dto.GameDTO;
import com.example.gamecatalog.repositories.CategoryRepository;
import com.example.gamecatalog.repositories.GameRepository;
import com.example.gamecatalog.service.CategoryService;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private GameRepository gameRepository;

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDTO> getAllCategories() {
        log.trace("getAllCategories");
        List <Category> categories = categoryRepository.findAllCategories();
        return addGameIdsToList(categories);
    }

    @GetMapping(value = "/getCategoryByName/{name}")
    public ResponseEntity<CategoryDTO> getCategoryByName(@PathVariable(value = "name") String name){
        log.trace("getCategoryByName");
        Category category = categoryRepository.findByName(name);
        if (category != null) {
            CategoryDTO categoryDTO = new CategoryDTO(category);
            addGameIDsToCategory(categoryDTO);
            return new ResponseEntity<>(categoryDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping(value = "/getCategoryById/{id}")
      public CategoryDTO getCategory(@PathVariable Long id) {
        Optional<Category> category = categoryRepository.findById(id);

        if (!category.isPresent()) {
            return null;
        }
            CategoryDTO categoryDTO = new CategoryDTO(category);
            addGameIDsToCategory(categoryDTO);
            return categoryDTO;
    }



    @PostMapping
    public ResponseEntity<Long> createCategory(@RequestBody CreateCategoryRequest createCategoryRequest) {
        log.trace("createCategory");

        log.trace("Creating category " + createCategoryRequest);
        Long categoryId = categoryService.createCategory(
                createCategoryRequest.getName(),
                createCategoryRequest.getDescription()).getId();
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(categoryId)
                .toUri();

        return ResponseEntity.created(uri).body(categoryId);
    }

    @GetMapping("/{categoryId}/games")
    public ResponseEntity<List<GameDTO>> getAllGamesByCategoryId(@PathVariable(value = "categoryId") Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Not found Category  with id = " + categoryId);
        }
        List<Game> games = gameRepository.findByCategoriesId(categoryId);
        List<GameDTO> gameDTOS = addCategoriesIdsToList(games);
        return new ResponseEntity<>(gameDTOS, HttpStatus.OK);
    }

    @PutMapping("/updateCategory/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody CreateCategoryRequest updatedCategory) {
        Category modifiedCategory = categoryService.updateGame(id, updatedCategory);

        if (modifiedCategory != null) {
            return ResponseEntity.ok(modifiedCategory);
        } else {
            return ResponseEntity.notFound().build();
        }
    }




    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id){
        categoryRepository.deleteById(id);
    }




    /// AUXILIARY FUNCS

    private CategoryDTO addGameIDsToCategory(CategoryDTO categoryDTO){
        List <Game> games = gameRepository.findByCategoriesId(categoryDTO.getId());
        for (int i = 0; i < games.size(); i++) {
            categoryDTO.getGameIds().add(games.get(i).getId());
        }
        return categoryDTO;
    }

    private List<CategoryDTO> addGameIdsToList(List<Category> categories){
        List<CategoryDTO> categoriesDTO = new ArrayList<>();
        for (int i = 0; i < categories.size() ; i++) {
            categoriesDTO.add(new CategoryDTO(categories.get(i)));
            addGameIDsToCategory(categoriesDTO.get(i));
        }
        return categoriesDTO;
    }

    private GameDTO addCategoriesIDsToGame(GameDTO gameDTO){
        List <Category> categories = categoryRepository.findByGamesId(gameDTO.getId());
        for (int i = 0; i < categories.size(); i++) {
            gameDTO.getCategoryIds().add(categories.get(i).getId());

        }
        return gameDTO;
    }

    private List<GameDTO> addCategoriesIdsToList(List<Game> games){
        List<GameDTO> gamesDTO = new ArrayList<>();
        for (int i = 0; i < games.size() ; i++) {
            gamesDTO.add(new GameDTO(games.get(i)));
            addCategoriesIDsToGame(gamesDTO.get(i));
        }
        return gamesDTO;
    }

}
