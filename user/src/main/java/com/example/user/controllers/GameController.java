package com.example.user.controllers;

import com.example.user.entities.User;
import com.example.user.services.AlertService;
import com.example.user.services.CartItemService;
import com.example.user.services.CartService;
import com.example.user.services.YoutubeService;
import com.example.user.utils.JsonMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Log4j2
@Controller
@RequestMapping("/game")
public class GameController {

    private final RestTemplate restTemplate;
    private final JsonMapper jsonMapper;

    private final CartItemService cartItemService;
    private final CartService cartService;
    private final AlertService alertService;
    private final YoutubeService youTubeService;
    @Value("${catalog.api.url}")
    private String catalogApiUrl;

    public GameController(RestTemplate restTemplate, JsonMapper jsonMapper, CartItemService cartItemService, CartService cartService, AlertService alertService, YoutubeService youTubeService) {
        this.restTemplate = restTemplate;
        this.jsonMapper = jsonMapper;
        this.cartItemService = cartItemService;
        this.cartService = cartService;
        this.alertService = alertService;
        this.youTubeService = youTubeService;
    }

    @GetMapping("/{gameId}")
    public String getCategoryGames(@PathVariable Long gameId, Model model, HttpSession session) {


        String categoriesApiUrl = catalogApiUrl+"/Game/"+ gameId +"/categories";

        String gameApiUrl = catalogApiUrl+"/Game/getGameById/" + gameId;

        String platformsApiUrl = catalogApiUrl+"/Game/"+ gameId +"/platforms";

        // Fetch category details as a single object
        String gameJsonResponse = restTemplate.getForObject(gameApiUrl, String.class);

        if(gameJsonResponse == null){
            return "redirect:/home";
        }
            
        Map<String, Object> game = jsonMapper.parseJsonObjectResponse(gameJsonResponse);

        // Fetch games for the category as a list of objects
        String categoriesJsonResponse = restTemplate.getForObject(categoriesApiUrl, String.class);
        List<Map<String, Object>> categories = jsonMapper.parseJsonResponse(categoriesJsonResponse);

        // Fetch the platforms available
        String platformsJsonResponse = restTemplate.getForObject(platformsApiUrl, String.class);
        List<String> platforms = jsonMapper.parseJsonArrayToStringList(platformsJsonResponse);
        boolean userHasAlert = false;
        //alert
        User user = (User) session.getAttribute("user");
        if(user != null){
            userHasAlert = alertService.userHasAlert(gameId,user);
        }

        String embed  = (String) game.get("videoEmbed");
        String videoId = "";

        if(embed == null) {
            String gameName = (String) game.get("name");
            try {
                videoId = youTubeService.getVideoIdByGameName(gameName);
                RestTemplate restTemplate = new RestTemplate();

                // Set up the request body and headers if needed
                // For simplicity, assuming the second API expects the reference and gameId as path variables
                String apiUrl = catalogApiUrl+"/Game/updateEmbedGame/"+gameId+"/"+videoId;
                restTemplate.postForEntity(apiUrl, null, String.class);

            } catch (IOException e) {
                // Handle the exception (e.g., log it)
                e.printStackTrace();
            }
        }else {
            videoId = embed;
        }
        model.addAttribute("videoId", videoId);
        model.addAttribute("categories", categories);
        model.addAttribute("game", game);
        model.addAttribute("platforms", platforms);
        model.addAttribute("userHasAlert", userHasAlert);

        return "home/game";
    }

    @PostMapping("/addToCart/{platform}/{gameId}")
    public String addToCart(
            @PathVariable String platform,
            @RequestParam(name = "action", required = false) String action,
            @PathVariable Long gameId,
            HttpSession session
    ) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/home";
        }

            String apiUrl = catalogApiUrl+"/CodeGameItem/selectCodeGameItem/" + platform + "/" + gameId;
            String codeResponse = restTemplate.getForObject(apiUrl, String.class);

            if(codeResponse == null){
               return  "redirect:/game/" + gameId;
            }

        // Check if the user is the owner of the cart
        if (!user.equals(cartService.getCartById(user.getCart().getId()).getUser())) {
            // The user is not the owner of the cart, handle accordingly
            return "redirect:/game/" + gameId;
        }

            cartItemService.createCartItem(gameId, platform, user.getCart().getId(), codeResponse);

        if ("buyNow".equals(action)) {
            return "redirect:/cart";
        }else {
            return "redirect:/game/" + gameId;
        }
    }




}
