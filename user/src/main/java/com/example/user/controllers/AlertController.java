package com.example.user.controllers;

import com.example.user.entities.Alert;
import com.example.user.entities.User;
import com.example.user.repositories.AlertRepository;
import com.example.user.services.AlertService;
import com.example.user.utils.JsonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/alert")
public class AlertController {


    private final AlertService alertService;

    private final AlertRepository alertRepository;

    private final RestTemplate restTemplate;
    private final JsonMapper jsonMapper;
    @Value("${catalog.api.url}")
    private String catalogApiUrl;

    @Autowired
    public AlertController(AlertService alertService, AlertRepository alertRepository, RestTemplate restTemplate, JsonMapper jsonMapper) {
        this.alertService = alertService;
        this.alertRepository = alertRepository;
        this.restTemplate = restTemplate;
        this.jsonMapper = jsonMapper;
    }


    @PostMapping("/createOrDeleteAlert")
    public String createOrDeleteAlert(@RequestParam("gameId") Long gameId,
                                      @RequestParam(value = "deleteAlert", required = false) Boolean deleteAlert,
                                      HttpSession session) {
        // Get the current user from the session or however it is managed
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            // Handle the case when there's no logged-in user
            return "redirect:/home"; // Redirect to login page or handle appropriately
        }

        if (Boolean.TRUE.equals(deleteAlert)) {
            // Delete the alert
             alertService.deleteAlert(gameId, currentUser);
        } else {
            // Create the alert
            alertService.createAlert(gameId, currentUser);
        }

        // Redirect or return appropriate view
        return "redirect:/game/"+gameId; // Redirect to the game page or handle appropriately
    }


    @GetMapping("/showAlerts")
    public String showAlerts(Model model, HttpSession session) {

        if (session.getAttribute("user") == null || session.getAttribute("admin") != null) {
            return "redirect:/home";
        }
        User user = (User) session.getAttribute("user");

            List<Alert> alerts = alertRepository.findAlertsByUser(user);

            if(!alerts.isEmpty()){
                List<Map<String, Object>> gameDetailsList = new ArrayList<>();
                for(Alert alert: alerts){
                    Map<String, Object> gameDetail = fetchGameDetails(alert.getGameId());
                    gameDetailsList.add(gameDetail);
                }

                model.addAttribute("alerts",alerts);
                model.addAttribute("gameDetailsList",gameDetailsList);

            }

        return "user/viewAlerts";

    }

    @DeleteMapping("/delete/{alertId}")
    public String deleteAlert(@PathVariable String alertId, HttpSession session) {

        if(session.getAttribute("user") == null)
            return "redirect:/home";

        User user = (User) session.getAttribute("user");
        Optional<Alert> optionalAlert = alertRepository.findById(Long.parseLong(alertId));

        if (optionalAlert.isPresent()) {
            Alert alert = optionalAlert.get();

            if (alert.getUser().equals(user)) {
                alertRepository.delete(alert);
            }
        }

        return "redirect:/alert/showAlerts";
    }

///AUX FUNC
    private Map<String, Object> fetchGameDetails(Long gameId) {
        String apiUrl = catalogApiUrl+"/Game/getGameById/"+gameId;
        String jsonResponse = restTemplate.getForObject(apiUrl, String.class);
        return jsonMapper.parseJsonObjectResponse(jsonResponse);
    }








}
