package com.example.gamecatalog.kafka;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public class KafkaConstants {

    // misc
    public static final String SEPARATOR = ".";

    // topic items
    public static final String GAME_TOPIC = "game";

    // commands
    public static final String ITEM_AVAILABLE = "item_available";

}
