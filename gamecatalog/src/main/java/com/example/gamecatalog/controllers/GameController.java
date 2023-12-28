package com.example.gamecatalog.controllers;

import com.example.gamecatalog.controllers.dtos.CreateGameRequest;
import com.example.gamecatalog.entities.Category;
import com.example.gamecatalog.entities.Game;
import com.example.gamecatalog.entities.dto.CategoryDTO;
import com.example.gamecatalog.entities.dto.GameDTO;
import com.example.gamecatalog.repositories.CategoryRepository;
import com.example.gamecatalog.repositories.GameRepository;
import com.example.gamecatalog.service.GameService;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;

@Log4j2
@RestController
@RequestMapping("/Game")
public class GameController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameService gameService;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public List<GameDTO> getAllGames() {
        log.trace("getAllGames");
        List<Game> games = gameRepository.findAllGames();
        List<GameDTO> gamesDTO = addCategoriesIdsToList(games);
        return gamesDTO;
    }
    @PostMapping()
    public ResponseEntity<Long> createGame(@RequestBody CreateGameRequest createGameRequest) {
        log.trace("createGame");

        log.trace("Creating Game " + createGameRequest);
        Long gameId = gameService.createGame(
                createGameRequest.getName(),
                createGameRequest.getDescription(),
                createGameRequest.getPublisher(),
                createGameRequest.getPrice()).getId();
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(gameId)
                .toUri();

        return ResponseEntity.created(uri).body(gameId);
    }

    @GetMapping(value = "/getGameByName/{name}")
    public List<GameDTO> getGameByName(@PathVariable(value = "name") String name){
        log.trace("getGameByName");
        List<Game> games =  gameRepository.findByName(name);
        List<GameDTO> gamesDTO = addCategoriesIdsToList(games);
        return gamesDTO;
    }

    @GetMapping(value = "/getGameById/{id}")
    public GameDTO getGameById(@PathVariable(value = "id") Long id){
        log.trace("getGameById");
        Optional<Game> gameOptional = gameRepository.findById(id);

        if (!gameOptional.isPresent()) {
            return null;
        }

        GameDTO gameDTO = new GameDTO(gameOptional.get());
        addCategoriesIDsToGame(gameDTO);
        return gameDTO;
    }



    @GetMapping(value = "/getGameByPartialName/{name}")
    public List<GameDTO> getGameByPartialName(@PathVariable(value = "name") String name){
        log.trace("getGameByPartialName");
        List<Game> games =  gameRepository.findGamesByPartialName(name);
        List<GameDTO> gamesDTO = addCategoriesIdsToList(games);
        return gamesDTO;
    }

    @GetMapping("/searchByPriceRange")
    public ResponseEntity<List<GameDTO>> searchGamesByPriceRange(
            @RequestParam(name = "minPrice") float minPrice,
            @RequestParam(name = "maxPrice") float maxPrice) {

        List<Game> games = gameRepository.findGamesByPriceRange(minPrice, maxPrice);
        List<GameDTO> gamesDTO = addCategoriesIdsToList(games);
        if (games.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(gamesDTO);
        }
    }

    @GetMapping("/searchGamesUnder20")
    public ResponseEntity<List<GameDTO>> searchGamesUnder20() {

        List<Game> games = gameRepository.findGamesByPriceRange(0F, 20F);
        List<GameDTO> gamesDTO = addCategoriesIdsToList(games);
        if (games.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(gamesDTO);
        }
    }

    @GetMapping("/getNewAddedGames")
    @ResponseStatus(HttpStatus.OK)
    public List<GameDTO> getNewAddedGames() {
        log.trace("getAllGames");
        List<Game> games = gameRepository.findFirst10ByOrderByIdDesc();
        List<GameDTO> gamesDTO = addCategoriesIdsToList(games);
        return gamesDTO;
    }



    @GetMapping("/{gameId}/categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategoriesByGameId(@PathVariable(value = "gameId") Long gameId) {
        if (!gameRepository.existsById(gameId)) {
            throw new ResourceNotFoundException("Not found Game with id = " + gameId);
        }
        List<Category> categories = categoryRepository.findByGamesId(gameId);
        List<CategoryDTO> categoriesDTO = addGameIdsToList(categories);
        return new ResponseEntity<>(categoriesDTO, HttpStatus.OK);
    }

    @GetMapping("/{gameId}/platforms")
    public ResponseEntity<List<String>> getPlatformsByGameId(@PathVariable Long gameId) {
        List<String> platforms = gameRepository.findPlatformsByGameId(gameId);
        return ResponseEntity.ok(platforms);
    }

    @PutMapping("/updateGame/{id}")
    public ResponseEntity<Game> updateGame(@PathVariable Long id, @RequestBody CreateGameRequest createGameRequest) {


        Game modifiedGame = gameService.updateGame(id, createGameRequest);

        if (modifiedGame != null) {
            return ResponseEntity.ok(modifiedGame);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/updateEmbedGame/{id}/{embed}")
    public ResponseEntity<Game> updateEmbedGame(@PathVariable Long id, @PathVariable String embed) {
        Game modifiedGame = gameService.updateEmbedGame(id, embed);
        if (modifiedGame != null) {
            return ResponseEntity.ok(modifiedGame);
        } else {
            return ResponseEntity.notFound().build();
        }
    }




    // Link a game to a category
    @PostMapping("/link/{gameId}/category/{categoryId}")
    public ResponseEntity<Object> linkGameToCategory(@PathVariable Long gameId, @PathVariable Long categoryId) {
        Optional<Game> gameOptional = gameRepository.findById(gameId);
        Optional<Category> categoryOptional = categoryRepository.findById(categoryId);

        if (!gameOptional.isPresent() || !categoryOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Game game = gameOptional.get();
        Category category = categoryOptional.get();

        // Link the game to the category

        game.getCategories().add(category);
        gameRepository.save(game);
        category.getGames().add(game);
        categoryRepository.save(category);

        return ResponseEntity.ok().build();
    }

    // Unlink a game from a category
    @PostMapping("/unlink/{gameId}/category/{categoryId}")
    public ResponseEntity<Object> unlinkGameFromCategory(@PathVariable Long gameId, @PathVariable Long categoryId) {
        Optional<Game> gameOptional = gameRepository.findById(gameId);
        Optional<Category> categoryOptional = categoryRepository.findById(categoryId);

        if (!gameOptional.isPresent() || !categoryOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Game game = gameOptional.get();
        Category category = categoryOptional.get();

        // Unlink the game from the category
        game.getCategories().remove(category);
        gameRepository.save(game);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(@PathVariable Long id){
        log.trace("deleteGame");
        gameRepository.deleteById(id);
    }

    @PostMapping("/uploadImageReference/{reference}/{gameId}")
    public ResponseEntity<String> uploadImageReference(@PathVariable String reference, @PathVariable Long gameId) {
        gameService.uploadGameReference(reference, gameId);

        // Return an appropriate response, e.g., success message
        return ResponseEntity.ok("Image reference received successfully.");
    }

    @GetMapping("/imageReference/{gameId}")
    public ResponseEntity<String> imageReference(@PathVariable Long gameId) {
       String imageReference =  gameService.getGameImageReference( gameId);

        if (imageReference != null) {
            // Return the old filename in the response
            return new ResponseEntity<>(imageReference, HttpStatus.OK);
        } else {
            // Return an appropriate response for not found
            return new ResponseEntity<>("", HttpStatus.OK);
        }
    }



    /// AUXILIARY FUNCS
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

}
