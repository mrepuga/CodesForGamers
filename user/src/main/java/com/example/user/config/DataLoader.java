package com.example.user.config;

import com.example.user.entities.TypeUser;
import com.example.user.repositories.UserRepository;
import com.example.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements ApplicationRunner {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Value("${data.loader.initialized}")
    private boolean dataInitialized;

    @Value("${web.admin.password}")
    private String pass;

    @Value("${web.admin.email}")
    private String email;

    public void run(ApplicationArguments args) throws Exception {
        // You can add your user creation logic here

        if (!dataInitialized) {
            String[][] userData = {
                    {email, "Admin admin", pass, "444444444", String.valueOf(TypeUser.ADMIN)}
            };
            for (String[] user : userData) {
                createUser(user[0], user[1], user[2], user[3], user[4]);
            }
            dataInitialized = true;
        }
    }

    private void createUser(String email, String fullName,String password, String phoneNumber, String typeUser) {
        // Check if the user already exists
        if (userRepository.findByEmail(email) == null) {
            password = new BCryptPasswordEncoder().encode(password);
            if(typeUser== "CLIENT"){
                userService.createClientUser(email, password, fullName, phoneNumber);
            } else if (typeUser== "ADMIN") {
                userService.createAdminUser(email,password,fullName,phoneNumber);
            }

        }
    }
}
