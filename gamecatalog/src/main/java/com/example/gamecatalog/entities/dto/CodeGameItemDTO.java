package com.example.gamecatalog.entities.dto;


import com.example.gamecatalog.entities.CodeGameItem;

public class CodeGameItemDTO {


    private String code;


    private String platform;


    private Long gameId;


    private Boolean selected;


    public CodeGameItemDTO(CodeGameItem codeGameItem) {
        this.code = codeGameItem.getCode();
        this.platform = codeGameItem.getPlatform();
        this.gameId = codeGameItem.getGame().getId();
        this.selected = codeGameItem.getSelected();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
}

