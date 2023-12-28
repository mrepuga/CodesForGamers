package com.example.gamecatalog.service;

import com.example.gamecatalog.entities.CodeGameItem;
import com.example.gamecatalog.entities.Game;
import com.example.gamecatalog.kafka.GameMessage;
import com.example.gamecatalog.kafka.KafkaConstants;
import com.example.gamecatalog.repositories.CodeGameItemRepository;
import com.example.gamecatalog.repositories.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class CodeGameItemService {

    @Autowired
    private CodeGameItemRepository codeGameItemRepository;

    @Autowired
    private GameRepository gameRepository;


    @Autowired
    private KafkaTemplate<String, GameMessage> productMessageKafkaTemplate;

    public CodeGameItem createItem(Long gameId, String code, String platform) {

        CodeGameItem codeGameItem = CodeGameItem.builder().code(code).platform(platform.toUpperCase()).selected(false).build();

        Game game = gameRepository.findById(gameId).get();

        if (gameId != null) {
            codeGameItem.setGame(game);
        } else {
            throw new IllegalArgumentException("Could not find the game with Id: " + gameId);
        }
        List<CodeGameItem> existingItems = codeGameItemRepository.findByGameIdAndSelectedIsFalse(game.getId());

        if (existingItems.isEmpty()) {
            productMessageKafkaTemplate.send(KafkaConstants.GAME_TOPIC + KafkaConstants.SEPARATOR + KafkaConstants.ITEM_AVAILABLE,
                    GameMessage.builder().gameId(gameId).price(game.getPrice()).description(game.getDescription()).name(game.getName()).build());
        }
       codeGameItemRepository.save(codeGameItem);

        return codeGameItem;
    }

    public void deleteItemByCode(String code) {

        CodeGameItem codeGameItem = codeGameItemRepository.findByCode(code);

        if(codeGameItem != null && codeGameItem.getSelected() == false){
        codeGameItemRepository.delete(codeGameItem);
        }
    }

    public List<CodeGameItem> findByGameIdAndPlatform(Long gameId, String platform) {
        return codeGameItemRepository.findByGameIdAndPlatform(gameId, platform);
    }


    public Long getAvailableItemCount(Long gameId, String platform) {
        return codeGameItemRepository.countByGameIdAndPlatform(gameId, platform);
    }

    public CodeGameItem selectCodeGameItem(String platform, Long gameId) {
      List<CodeGameItem> codeGameItemList = codeGameItemRepository.findByGameIdAndPlatformAndSelectedIsFalse(gameId,platform);

      if(codeGameItemList.size()==0){
          return null;
      }

      CodeGameItem codeGameItem = codeGameItemList.get(0);
        if(codeGameItem != null){
            codeGameItem.setSelected(true);
            codeGameItemRepository.save(codeGameItem);
            return codeGameItem;
        }else{
            return null;
        }


    }

    public CodeGameItem unselectCodeGameItem(String code) {
        CodeGameItem codeGameItem = codeGameItemRepository.findByCode(code);

        if(codeGameItem != null){
            List<CodeGameItem> existingItems = codeGameItemRepository.findByGameIdAndSelectedIsFalse(codeGameItem.getGame().getId());

            if (existingItems.isEmpty()) {
                Game game = codeGameItem.getGame();
                productMessageKafkaTemplate.send(KafkaConstants.GAME_TOPIC + KafkaConstants.SEPARATOR + KafkaConstants.ITEM_AVAILABLE,
                        GameMessage.builder().gameId(game.getId()).price(game.getPrice()).description(game.getDescription()).name(game.getName()).build());
            }
            codeGameItem.setSelected(false);
            codeGameItemRepository.save(codeGameItem);
            return codeGameItem;
        }else{
            return null;
        }

    }

    public List<CodeGameItem> findByGameId(Long gameId) {
        return codeGameItemRepository.findByGameId(gameId);

    }

    public List<CodeGameItem> findAll() {
        return codeGameItemRepository.findAll();
    }

    public boolean existsByCode(String code) {
        return codeGameItemRepository.existsByCode(code);
    }

    public List<CodeGameItem> findAllOrderByGameId() {
        Sort sort = Sort.by(Sort.Order.asc("game.id"));
        return codeGameItemRepository.findAll(sort);
    }

    public CodeGameItem getCodeGameItemByCode(String code) {
        return codeGameItemRepository.findByCode(code);
    }
}

