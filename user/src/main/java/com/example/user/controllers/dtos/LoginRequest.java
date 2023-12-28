package com.example.user.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.validation.constraints.Email;

@Getter
@Setter
@AllArgsConstructor
public class LoginRequest {

    @NonNull
    @Email(message = "Invalid email format")
    private String email;

    @NonNull
    private String password;

}
