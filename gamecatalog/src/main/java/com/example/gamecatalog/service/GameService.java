package com.example.gamecatalog.service;

import com.example.gamecatalog.controllers.dtos.CreateGameRequest;
import com.example.gamecatalog.entities.Game;
import com.example.gamecatalog.repositories.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    public Game createGame(String name, String description,String publisher, Float price) {

        Game game = Game.builder().name(name.toUpperCase()).description(description).publisher(publisher).price(price).build();

        return gameRepository.save(game);
    }

    public Game updateGame(Long id, CreateGameRequest updatedGame) {
        Optional<Game> existingGameOptional = gameRepository.findById(id);

        if (existingGameOptional.isPresent()) {
            Game existingGame = existingGameOptional.get();

            existingGame.setName(updatedGame.getName());
            existingGame.setPrice(updatedGame.getPrice());
            existingGame.setDescription(updatedGame.getDescription());
            existingGame.setPublisher(updatedGame.getPublisher());
            return gameRepository.save(existingGame);
        } else {
            return null;
        }
    }

    public void uploadGameReference(String reference, Long gameId) {

        Optional<Game> existingGameOptional = gameRepository.findById(gameId);

        if (existingGameOptional.isPresent()) {
            Game existingGame = existingGameOptional.get();
            existingGame.setImagePath(reference);
            gameRepository.save(existingGame);
        }

    }

    public String getGameImageReference(Long gameId) {
        Optional<Game> existingGameOptional = gameRepository.findById(gameId);
        if (existingGameOptional.isPresent()){
            Game existingGame = existingGameOptional.get();
            return existingGame.getImagePath();
        }
        return null;
    }

    public Game updateEmbedGame(Long id, String embed) {
        Optional<Game> existingGameOptional = gameRepository.findById(id);

        Game existingGame = null;
        if (existingGameOptional.isPresent()) {
            existingGame = existingGameOptional.get();
            existingGame.setVideoEmbed(embed);
            gameRepository.save(existingGame);
        }
        return existingGame;
    }
}
