package com.example.notificationApp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {

    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;

}
