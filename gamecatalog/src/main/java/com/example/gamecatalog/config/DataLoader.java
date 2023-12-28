package com.example.gamecatalog.config;


import com.example.gamecatalog.entities.Category;
import com.example.gamecatalog.entities.Game;
import com.example.gamecatalog.repositories.CategoryRepository;
import com.example.gamecatalog.repositories.GameRepository;
import com.example.gamecatalog.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataLoader implements ApplicationRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private GameService gameService;


    @Override
    public void run(ApplicationArguments args) {
        loadGamesWithCategories();
    }

    private void loadCategories() {


        String[][] categoryData = {
                {"Action", "Exciting action games"},
                {"Adventure", "Thrilling adventure games"},
                {"RPG", "Role-playing games"},
                {"Strategy", "Strategic games"},
                {"Simulation", "Simulation games"},
                {"Puzzle", "Brain-teasing puzzle games"},
                {"Sports", "Sports-related games"},
                {"Horror", "Spooky horror games"},
                {"Sci-Fi", "Science fiction games"},
                {"Fantasy", "Fantasy-themed games"}
        };


        for (String[] data : categoryData) {
            Category category = Category.builder().name(data[0].toUpperCase()).description(data[1]).build();
            categoryRepository.save(category);
        }

    }

    private void loadGames() {
        String[][] gameData = {
                {"Assassin's Creed Valhalla", "Open-world action-adventure", "Ubisoft", "59.99"},
                {"Halo Infinite", "Action-packed sci-fi", "Microsoft Studios", "59.99"},
                {"God of War", "Action-adventure", "Santa Monica Studio", "49.99"},
                {"The Witcher 3: Wild Hunt", "Epic RPG journey", "CD Projekt", "39.99"},
                {"Dragon Age: Inquisition", "Epic Fantasy RPG", "BioWare", "49.99"},
                {"Persona 5", "Stylish JRPG", "Atlus", "14.99"},
                {"Sid Meier's Civilization VI", "Turn-based strategy", "2K Games", "49.99"},
                {"Stellaris", "Grand strategy", "Paradox Interactive", "39.99"},
                {"The Sims 4", "Life simulation", "Electronic Arts", "29.99"},
                {"Cities: Skylines", "City-building simulation", "Paradox Interactive", "29.99"},
                {"Portal 2", "Mind-bending puzzle challenge", "Valve", "19.99"},
                {"Tetris Effect", "Puzzle", "Enhance, Inc.", "29.99"},
                {"FIFA 22", "Sports simulator", "EA Sports", "19.99"},
                {"NBA 2K22", "Basketball simulation", "2K Sports", "59.99"},
                {"Resident Evil Village", "Horror survival", "Capcom", "49.99"},
                {"Dead by Daylight", "Survival horror", "Behaviour Interactive", "9.99"},
                {"Mass Effect: Legendary Edition", "Sci-Fi adventure", "Electronic Arts", "59.99"},
                {"Star Wars Jedi: Fallen Order", "Action-adventure", "Respawn Entertainment", "39.99"},
                {"The Legend of Zelda: Breath of the Wild", "Fantasy action-adventure", "Nintendo", "59.99"},
                {"Final Fantasy XV", "Action RPG", "Square Enix", "49.99"}
        };


        for (String[] data : gameData) {
            Game game = Game.builder().name(data[0].toUpperCase()).description(data[1]).publisher(data[2]).price(Float.parseFloat(data[3])).build();
            gameRepository.save(game);
        }

    }



    private void loadGamesWithCategories() {

        List<Category> categories = categoryRepository.findAll();
        List<Game> games = gameRepository.findAll();

        if (games.isEmpty() && categories.isEmpty()) {
            loadGames();
            loadCategories();

            categories = categoryRepository.findAll();
            games = gameRepository.findAll();


            int categoryIndex = 0;

            for (int i = 0; i < games.size(); i += 2) {

                if (i + 1 < games.size() && categoryIndex < categories.size()) {
                    Game game1 = games.get(i);
                    Game game2 = games.get(i + 1);

                    Category category = categories.get(categoryIndex);
                    game1.getCategories().add(category);
                    game2.getCategories().add(category);

                    gameRepository.save(game1);
                    gameRepository.save(game2);


                    categoryIndex++;
                }
            }
        }
    }
}





