package com.example.user.controllers;

import com.example.user.utils.JsonMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Log4j2
@Controller
@RequestMapping("/category")
public class CategoryController {

    private final RestTemplate restTemplate;
    private final JsonMapper jsonMapper;
    @Value("${catalog.api.url}")
    private String catalogApiUrl;

    public CategoryController(RestTemplate restTemplate, JsonMapper jsonMapper) {
        this.restTemplate = restTemplate;
        this.jsonMapper = jsonMapper;
    }

    @GetMapping("/{categoryId}")
    public String getCategoryGames(@PathVariable Long categoryId, Model model) {


        String gamesApiUrl = catalogApiUrl+"/category/" + categoryId + "/games";


        String categoryApiUrl = catalogApiUrl+"/category/getCategoryById/" + categoryId;

        // Fetch category details as a single object
        String categoryJsonResponse = restTemplate.getForObject(categoryApiUrl, String.class);

        if(categoryJsonResponse== null){

            return "redirect:/home";
        }

        Map<String, Object> category = jsonMapper.parseJsonObjectResponse(categoryJsonResponse);

        // Fetch games for the category as a list of objects
        String gamesJsonResponse = restTemplate.getForObject(gamesApiUrl, String.class);
        List<Map<String, Object>> games = jsonMapper.parseJsonResponse(gamesJsonResponse);

        model.addAttribute("games", games);
        model.addAttribute("category", category);
        return "home/category";
    }





}
