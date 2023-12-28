package com.example.gamecatalog.controllers;

import com.example.gamecatalog.controllers.dtos.CreateCodeGameItemRequest;
import com.example.gamecatalog.entities.Category;
import com.example.gamecatalog.entities.CodeGameItem;
import com.example.gamecatalog.entities.dto.CodeGameItemDTO;
import com.example.gamecatalog.repositories.CodeGameItemRepository;
import com.example.gamecatalog.service.CodeGameItemService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.ws.rs.GET;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/CodeGameItem")
public class CodeGameItemController {


    @Autowired
    private CodeGameItemService codeGameItemService;
    @Autowired
    private CodeGameItemRepository repository;

    @PostMapping
    public ResponseEntity<String> codeGamecreateItem(@RequestBody CreateCodeGameItemRequest createItemRequest) {
        String code = createItemRequest.getCode();

        if (codeGameItemService.existsByCode(code)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("CodeGameItem with code '" + code + "' already exists.");
        }

        try {
            CodeGameItem createdItem = codeGameItemService.createItem(
                    createItemRequest.getGameId(),
                    code,
                    createItemRequest.getPlatform()
            );

            URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{code}")
                    .buildAndExpand(createdItem.getCode())
                    .toUri();

            return ResponseEntity.created(uri).body(createdItem.getCode());
        } catch (IllegalArgumentException e) {

            log.error("Error creating item: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {

            log.error("Unexpected error creating item", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }


    @GetMapping("/{gameId}/{platform}")
    public ResponseEntity<List<CodeGameItem>> getCodeGameItems(
            @PathVariable Long gameId,
            @PathVariable String platform) {
        log.trace("getCodeGameItems");

        log.trace("Finding CodeGameItems with gameId: " + gameId + " and platform: " + platform.toUpperCase());
        List<CodeGameItem> codeGameItems = codeGameItemService.findByGameIdAndPlatform(gameId, platform.toUpperCase());

        if (codeGameItems.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(codeGameItems);
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<List<CodeGameItem>> getCodeGameItemsByGameId(
            @PathVariable Long gameId) {
        log.trace("getCodeGameItems");

        log.trace("Finding CodeGameItems with gameId: " + gameId);
        List<CodeGameItem> codeGameItems = codeGameItemService.findByGameId(gameId);

        if (codeGameItems.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(codeGameItems);
    }

    @GetMapping()
    public List<CodeGameItemDTO> getCodeGameItems() {
            log.trace("getCodeGameItems");
            List<CodeGameItem> codeGameItems = codeGameItemService.findAllOrderByGameId();
            List<CodeGameItemDTO> codeGameItemsDTO = new ArrayList<>();;

        for (int i = 0; i < codeGameItems.size(); i++) {
                CodeGameItemDTO codeGameItemDTO = new CodeGameItemDTO(codeGameItems.get(i));
                codeGameItemsDTO.add(codeGameItemDTO);
            }

            return codeGameItemsDTO;
    }



    @GetMapping("/getCodeGameItemByCode/{code}")
    public CodeGameItemDTO getCodeGameItemByCode(@PathVariable String code) {
        log.trace("getCodeGameItemByCode");
        CodeGameItem codeGameItem = codeGameItemService.getCodeGameItemByCode(code);
        if (codeGameItem == null) {
            return null;
        }
        CodeGameItemDTO  codeGameItemDTO = new CodeGameItemDTO(codeGameItem);
        return codeGameItemDTO;
    }



    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteCodeGameItem(@PathVariable String code) {
        log.trace("deleteCodeGameItem");

        log.trace("Deleting item with code: " + code);



        codeGameItemService.deleteItemByCode(code);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/availableItemCount")
    public ResponseEntity<Long> getAvailableItemCount(
            @RequestParam("gameId") Long gameId,
            @RequestParam("platform") String platform) {

        long count = codeGameItemService.getAvailableItemCount(gameId, platform);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/selectCodeGameItem/{platform}/{gameId}")
    public String selectCodeGameItem(@PathVariable String platform, @PathVariable Long gameId ) {
        CodeGameItem selectedCodeGameItem = codeGameItemService.selectCodeGameItem(platform,gameId);

        if (selectedCodeGameItem != null) {
            return selectedCodeGameItem.getCode();
        } else {
            return null;
        }
    }

    @GetMapping("/unselectCodeGameItem/{code}")
    public ResponseEntity<CodeGameItem> unselectCodeGameItem(@PathVariable String code ) {
        CodeGameItem unselectedCodeGameItem = codeGameItemService.unselectCodeGameItem(code);

        if (unselectedCodeGameItem != null) {
            return ResponseEntity.ok(unselectedCodeGameItem);
        } else {
            return ResponseEntity.notFound().build();
        }
    }




}
