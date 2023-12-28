package com.example.user.controllers;



import com.example.user.entities.CartItem;
import com.example.user.entities.OrderCart;
import com.example.user.entities.User;
import com.example.user.repositories.OrderCartRepository;
import com.example.user.utils.JsonMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;


@Controller
public class HomeController {

    private final RestTemplate restTemplate;
    private final JsonMapper jsonMapper;
    private final OrderCartRepository orderCartRepository;
    @Value("${catalog.api.url}")
    private String catalogApiUrl;

    public HomeController(RestTemplate restTemplate, JsonMapper jsonMapper, OrderCartRepository orderCartRepository) {
        this.restTemplate = restTemplate;
        this.jsonMapper = jsonMapper;
        this.orderCartRepository = orderCartRepository;
    }


    @GetMapping("/home")
    public String getHome(Model model) {
        // Call the catalog API to get data

        try {
        String url = catalogApiUrl+"/category/";
        String jsonResponse = restTemplate.getForObject(url, String.class);

        List<Map<String, Object>> categories = jsonMapper.parseJsonResponse(jsonResponse);
            model.addAttribute("categories", categories);
            for (Map<String, Object> category : categories) {
                String categoryId = category.get("id").toString(); // Assuming 'id' is the key for category ID
                String gamesApiUrl = catalogApiUrl+"/category/" + categoryId + "/games";
                String gamesJsonResponse = restTemplate.getForObject(gamesApiUrl, String.class);

                List<Map<String, Object>> games = jsonMapper.parseJsonResponse(gamesJsonResponse);


                model.addAttribute("games_"+categoryId, games);
            }



        } catch (Exception e) {
            // Handle the exception, log or return an error view
            model.addAttribute("errorData", "Error fetching data from the catalog API");
        }
        // Return the Thymeleaf template name
        return "home/home";
    }

    @GetMapping("/search")
    public String searchGames(@RequestParam String query, Model model) {
        try {
            // Search games by partial name
            String searchGamesApiUrl = catalogApiUrl+"/Game/getGameByPartialName/" + query.toUpperCase();
            String searchGamesJsonResponse = restTemplate.getForObject(searchGamesApiUrl, String.class);
            List<Map<String, Object>> searchedGames = jsonMapper.parseJsonResponse(searchGamesJsonResponse);
            model.addAttribute("searchedGames", searchedGames);

        } catch (HttpClientErrorException ex) {
            model.addAttribute("errorData", "Error from the catalog API: " + ex.getMessage());
        } catch (Exception e) {
            model.addAttribute("errorData", "Unexpected error: " + e.getMessage());
        }

        // Return the template for search results
        return "home/search-results";
    }




    @GetMapping("/recommendedGames")
    public String getRecommendedGames(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/user/login"; // Redirect to login page if the user is not logged in
        }

            List<Long> gameIds = getGameIdsFromUserOrders(user);

            if (!gameIds.isEmpty()) {
                List<Long> categories = getCategoriesFromGameIds(gameIds);
                List<Map<String, Object>> recommendedGames = getRecommendedGamesByCategories(categories);

                recommendedGames = removeDuplicates(recommendedGames, "id");

                recommendedGames = recommendedGames.stream()
                        .filter(game -> !gameIds.contains(((Integer) game.get("id")).longValue()))
                        .collect(Collectors.toList());

                model.addAttribute("recommendedGames", recommendedGames);
            }

        return "home/recommended-games";
    }

    @GetMapping("/searchGamesBelow20")
    public String searchGamesBelow20(Model model, HttpSession session) {
        try {
            String url = catalogApiUrl + "/Game/searchGamesUnder20";
            String under20GamesJsonResponse = restTemplate.getForObject(url, String.class);
            List<Map<String, Object>> under20Games = jsonMapper.parseJsonResponse(under20GamesJsonResponse);
            model.addAttribute("under20Games", under20Games);
        }     catch (Exception e) {
            // Handle the exception, log or return an error view
            model.addAttribute("errorData", "Error fetching data from the catalog API");
        }
        return "home/below20-games";
    }

    @GetMapping("/searchNewlyAddedGames")
    public String searchNewlyAddedGames(Model model, HttpSession session) {

        try {
            String url = catalogApiUrl + "/Game/getNewAddedGames";
            String newlyAddedGamesJsonResponse = restTemplate.getForObject(url, String.class);
            List<Map<String, Object>> newlyAddedGames = jsonMapper.parseJsonResponse(newlyAddedGamesJsonResponse);
            model.addAttribute("newlyAddedGames", newlyAddedGames);
        }     catch (Exception e) {
            // Handle the exception, log or return an error view
            model.addAttribute("errorData", "Error fetching data from the catalog API");
        }
        return "home/newlyAdded-games";
    }



    private List<Map<String, Object>> removeDuplicates(List<Map<String, Object>> list, String uniqueIdentifier) {
        Set<Object> seen = new HashSet<>();
        List<Map<String, Object>> uniqueList = new ArrayList<>();

        for (Map<String, Object> element : list) {
            Object identifierValue = element.get(uniqueIdentifier);

            if (identifierValue != null && seen.add(identifierValue)) {
                uniqueList.add(element);
            }
        }
        return uniqueList;
    }

    private List<Long> getGameIdsFromUserOrders(User user) {
        List<Long> gameIds = new ArrayList<>();

        // Assuming each OrderCart has a list of CartItems with game IDs
        List<OrderCart> orderCarts = orderCartRepository.findByUserId(user.getId());

        for (OrderCart orderCart : orderCarts) {
            for (CartItem cartItem : orderCart.getCartItems()) {
                gameIds.add(cartItem.getGameId());
            }
        }
        return gameIds;
    }

    private List<Long> getCategoriesFromGameIds(List<Long> gameIds) {
        List<Long> categories = new ArrayList<>();

        for (Long gameId : gameIds) {
            String apiUrl = catalogApiUrl+"/Game/" + gameId + "/categories";
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            if (response.getStatusCode().is2xxSuccessful()) {
                List<Map<String, Object>> categoryList = response.getBody();
                if (categoryList != null) {
                    for (Map<String, Object> category : categoryList) {
                        // Assuming each category has an "id" property
                        Integer categoryId = (Integer) category.get("id");

                        // Add the category name to the list if it has an "id"
                        if (categoryId != null) {
                            categories.add(Long.parseLong(categoryId.toString()));
                        }
                    }
                }
            } else {
                // Handle error if needed
                throw new RuntimeException("Error while fetching categories for gameId: " + gameId);
            }
        }

        return categories;
    }

    private List<Map<String, Object>> getRecommendedGamesByCategories(List<Long> categories) {
        List<Map<String, Object>> recommendedGames = new ArrayList<>();

        for (Long categoryId : categories) {
            // Assuming there's a method to get recommended games by category using the second API
            String recommendedGamesApiUrl = catalogApiUrl+"/category/" + categoryId+ "/games";
            String recommendedGamesJsonResponse = restTemplate.getForObject(recommendedGamesApiUrl, String.class);

            List<Map<String, Object>> categoryGames = jsonMapper.parseJsonResponse(recommendedGamesJsonResponse);
            recommendedGames.addAll(categoryGames);
        }

        return recommendedGames;
    }



}










