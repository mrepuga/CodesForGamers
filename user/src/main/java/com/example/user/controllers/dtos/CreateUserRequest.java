package com.example.user.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import javax.validation.constraints.Email;

@Getter
@AllArgsConstructor
public class CreateUserRequest {
    @NonNull
    private final String fullName;

    @NonNull
    @Email(message = "Invalid email format")
    private final String email;

    @NonNull
    private final String password;

    @NonNull
    private final String phoneNumber;

}
