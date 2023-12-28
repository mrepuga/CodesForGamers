package com.example.gamecatalog.repositories;

import com.example.gamecatalog.entities.Game;
import com.example.gamecatalog.entities.dto.GameDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByCategoriesId(Long categoryId);

    List<Game> findByName(String name);

    Optional<Game> findById(Long id);

    @Query("SELECT g FROM Game g WHERE LOWER(g.name) LIKE LOWER(concat('%', ?1, '%')) ORDER BY g.name")
    List<Game> findGamesByPartialName(String partialName);

    @Query("SELECT g FROM Game g WHERE g.price BETWEEN ?1 AND ?2")
    List<Game> findGamesByPriceRange(Float minPrice, Float maxPrice);

    @Query("SELECT g FROM Game g ORDER BY g.id ASC")
    List<Game> findAllGames();

    @Query("SELECT DISTINCT c.platform FROM CodeGameItem c WHERE c.game.id = :gameId AND c.selected = false")
    List<String> findPlatformsByGameId(Long gameId);


    List<Game> findFirst10ByOrderByIdDesc();
}
