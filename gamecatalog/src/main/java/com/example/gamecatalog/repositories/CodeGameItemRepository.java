package com.example.gamecatalog.repositories;

import com.example.gamecatalog.entities.CodeGameItem;
import com.example.gamecatalog.entities.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeGameItemRepository extends JpaRepository<CodeGameItem,Long> {
    CodeGameItem findByCode(String code);

    List<CodeGameItem> findByGameIdAndPlatform(Long gameId, String platform);

    Long countByGameIdAndPlatform(Long gameId, String platform);

    List<CodeGameItem> findByGameIdAndPlatformAndSelectedIsFalse(Long gameId, String platform);

    List<CodeGameItem> findByGameIdAndPlatformAndSelectedIsTrue(Long gameId, String platform);

    List<CodeGameItem> findByGameId(Long gameId);

    boolean existsByCode(String code);


    List<CodeGameItem> findByGameIdAndSelectedIsFalse(Long gameId);
}
