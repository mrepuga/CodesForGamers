package com.example.notificationApp.kafka;


import lombok.*;

@ToString
@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class GameMessage {

    private Long gameId;
    private String name;
    private String description;
    private Float price;


}
