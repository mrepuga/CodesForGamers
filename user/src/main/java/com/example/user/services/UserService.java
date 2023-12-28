package com.example.user.services;

import com.example.user.entities.Cart;
import com.example.user.entities.TypeUser;
import com.example.user.entities.User;
import com.example.user.repositories.AlertRepository;
import com.example.user.repositories.CartRepository;
import com.example.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AlertRepository alertRepository;
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public User createClientUser(String email, String password, String fullName, String phoneNumber) {

        User user = User.builder().email(email).password(password).fullName(fullName).phoneNumber(phoneNumber).typeUser(TypeUser.CLIENT).build();

        userRepository.save(user);

        Cart emptyCart = new Cart();
        emptyCart.setUser(user);
        user.setCart(emptyCart);

        cartRepository.save(emptyCart);

        return user;
    }

    public User createAdminUser(String email, String password, String fullName, String phoneNumber) {

        User user = User.builder().email(email).password(password).fullName(fullName).phoneNumber(phoneNumber).typeUser(TypeUser.ADMIN).build();

        return userRepository.save(user);
    }


    public User updateClientUser(Long userId, String newEmail, String newPassword, String newFullName, String newPhoneNumber) {

        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setEmail(newEmail);
            user.setPassword(newPassword);
            user.setFullName(newFullName);
            user.setPhoneNumber(newPhoneNumber);


            return userRepository.save(user);
        } else {
            throw new EntityNotFoundException("User not found with ID: " + userId);
        }
    }

    public User updateClientUserInfo(Long userId, String newEmail, String newFullName, String newPhoneNumber) {

        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setEmail(newEmail);
            user.setFullName(newFullName);
            user.setPhoneNumber(newPhoneNumber);


            return userRepository.save(user);
        } else {
            throw new EntityNotFoundException("User not found with ID: " + userId);
        }
    }
    public User updatePassword(Long userId, String oldPassword, String newPassword, String confirmNewPassword) {

        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if(passwordEncoder.matches(oldPassword,user.getPassword()) &&
                newPassword.equals(confirmNewPassword) ){

                user.setPassword(passwordEncoder.encode(newPassword));

            }else {
                throw new EntityNotFoundException("Password does not match");
            }


            return userRepository.save(user);
        } else {
            throw new EntityNotFoundException("User not found with ID: " + userId);
        }
    }






    public boolean existsUserByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void deleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }

    public Set<User> getUsersToAlert(Long gameId) {
        return alertRepository.findAlertsByGameId(gameId)
                .stream()
                .map(alert -> alert.getUser())
                .collect(Collectors.toSet());
    }
}
