package com.example.user.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdatePasswordRequest {


    private String oldPassword;

    private String newPassword;

    private String confirmNewPassword;

}
