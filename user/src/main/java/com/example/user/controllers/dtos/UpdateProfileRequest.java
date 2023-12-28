package com.example.user.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.Email;


@Getter
@Setter
@AllArgsConstructor
public class UpdateProfileRequest {



    private String fullName;



    @Email(message = "Invalid email format")
    private String email;


    private String phoneNumber;


}
