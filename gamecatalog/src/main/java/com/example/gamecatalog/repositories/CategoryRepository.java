package com.example.gamecatalog.repositories;

import com.example.gamecatalog.entities.Category;
import com.example.gamecatalog.entities.Game;
import com.example.gamecatalog.entities.dto.CategoryDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category,Long> {

    Category findByName(String name);

    List<Category> findByGamesId(Long id);
    @Query("SELECT c FROM Category c ORDER BY c.id ASC")
    List<Category> findAllCategories();
}
