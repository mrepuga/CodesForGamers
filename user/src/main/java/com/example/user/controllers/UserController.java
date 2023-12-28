package com.example.user.controllers;

import com.example.user.controllers.dtos.*;
import com.example.user.entities.TypeUser;
import com.example.user.entities.User;
import com.example.user.repositories.UserRepository;
import com.example.user.services.UserService;
import com.example.user.utils.Sessions;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Log4j2
@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @PostMapping("/registerClient")
    public String createClientUser(@ModelAttribute("createUserRequest") @Valid CreateUserRequest createUserRequest, BindingResult result,Model model,  HttpSession session) {


        if (result.hasErrors()) {
            model.addAttribute("createUserRequest", createUserRequest);
            model.addAttribute("error", "validation");
            return "redirect:/user/register?error=validation";
        }
        if (userService.existsUserByEmail(createUserRequest.getEmail())) {
            model.addAttribute("createUserRequest", createUserRequest);
            model.addAttribute("error", "exists");
            return "redirect:/user/register?error=exists";
        }
        String encodedPassword = passwordEncoder.encode(createUserRequest.getPassword());



        userService.createClientUser(
                createUserRequest.getEmail(),
                encodedPassword,
                createUserRequest.getFullName(),
                createUserRequest.getPhoneNumber()).getId();

        User user = userRepository.findByEmail(createUserRequest.getEmail());
        session.setAttribute("user", user);
        Sessions.addSession(session);
        return "redirect:/home";
    }


    @GetMapping("/register")
    public String showRegisterForm(@RequestParam(name = "error", required = false)String error,Model model,  HttpSession session) {
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser != null) {
            // User is already logged in, redirect to home
            return "redirect:/home";
        }

        model.addAttribute("error", error);
        model.addAttribute("createUserRequest", new CreateUserRequest("", "", "",""));

        return "user/register";
    }


    // Display login form
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(name = "error", required = false)String error,Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser != null) {
            // User is already logged in, redirect to home
            return "redirect:/home";
        }

        model.addAttribute("error", error);
        model.addAttribute("loginForm", new LoginRequest("", ""));
        return "user/login";
    }

    // Handle login submission
    @PostMapping("/login")
    public String login(@ModelAttribute("loginRequest") @Valid LoginRequest loginRequest, BindingResult result,
                        Model model, HttpSession session) {

        if (result.hasErrors()) {
            model.addAttribute("error", "validation");
            return "redirect:/user/login?error=validation";
        }

        User user = userRepository.findByEmail(loginRequest.getEmail());

        if(user != null) {
            // Use passwordEncoder to match the provided password with the encoded password in the database
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                // Set up a session with user information
                if (user.getTypeUser() == TypeUser.ADMIN) {
                    session.setAttribute("admin", user);
                } else {

                }

                session.setAttribute("user",user);
                Sessions.addSession(session);
                // Redirect to the home page or dashboard
                return "redirect:/home";
            }
        }
            // Add an error message and return to the login form on login failure
            model.addAttribute("error", "Invalid");
            return "redirect:/user/login?error=invalid";

    }

    @GetMapping("/logout")
    public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return "redirect:/home";
    }

    @GetMapping("/profile")
    public String showProfilePage(@RequestParam(name = "error", required = false)String error, Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser == null) {
            // User is not logged in, redirect to home
            return "redirect:/home";
        }
        model.addAttribute("error", error);
        model.addAttribute("passwordForm", new UpdatePasswordRequest("","",""));
        return "user/profile";
    }



    @PostMapping("/profile")
    public String updateProfileOrPassword(
            @ModelAttribute("updateProfileRequest") @Valid UpdateProfileRequest updateProfileRequest,
            @ModelAttribute("updatePasswordRequest") @Valid UpdatePasswordRequest updatePasswordRequest,
            BindingResult result,
            @RequestParam(name = "action", required = false) String action,
            Model model, HttpSession session) {

        if (result.hasErrors()) {
            model.addAttribute("error", "validation");
            return "redirect:/user/profile?error=validation";
        }

        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser != null) {
            // User is already logged in, redirect to home
            return "redirect:/home";
        }

        if ("updateProfile".equals(action)) {
            // Update user information


            if(userRepository.existsByEmail(updateProfileRequest.getEmail()) &&
                !loggedInUser.getEmail().equals(updateProfileRequest.getEmail())) {

                model.addAttribute("error", "validation");
                return "redirect:/user/profile?error=validation";
            }
            userService.updateClientUserInfo(loggedInUser.getId(),
                    updateProfileRequest.getEmail(),
                    updateProfileRequest.getFullName(),
                    updateProfileRequest.getPhoneNumber());
        } else if ("updatePassword".equals(action)) {
            // Update password
            if(passwordEncoder.matches(updatePasswordRequest.getOldPassword(),loggedInUser.getPassword()) &&
                    updatePasswordRequest.getNewPassword().equals(updatePasswordRequest.getConfirmNewPassword()) ){

                userService.updatePassword(loggedInUser.getId(),
                        updatePasswordRequest.getOldPassword(),
                        updatePasswordRequest.getNewPassword(),
                        updatePasswordRequest.getConfirmNewPassword());

            }else {
                model.addAttribute("error", "passwordValidation");
                return "redirect:/user/profile?error=passwordValidation";
            }
        }

        // Refresh user information in the session
        session.setAttribute("user", userRepository.findById(loggedInUser.getId()).orElse(null));

        // Redirect to the profile page or another appropriate page
        return "redirect:/user/profile";
    }



    @GetMapping(value = "/getUserById/{id}")
    public User getUserById(@PathVariable(value = "id") Long id){
        log.trace("getUserById");
        return userRepository.findById(id).get();

    }


    @GetMapping("/getUsersToAlert")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetUserResponse[]> getUsersToAlert(@RequestParam Long gameId) {
        log.trace("getUsersToAlert");
        return ResponseEntity.ok().body(userService.getUsersToAlert(gameId)
                .stream()
                .map(user -> GetUserResponse.fromDomain(user))
                .toArray(GetUserResponse[]::new));
    }


}
