package com.example.user.services;

import com.example.user.entities.Cart;
import com.example.user.entities.CartItem;
import com.example.user.entities.OrderCart;
import com.example.user.repositories.CartItemRepository;
import com.example.user.repositories.CartRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;


@Service
public class CartItemService {

    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private CartRepository cartRepository;
    @Value("${catalog.api.url}")
    private String catalogApiUrl;


    public CartItem createCartItem(Long gameId, String platform, Long cartId, String code) {

        CartItem cartItem = CartItem.builder().gameId(gameId).price(getGamePriceById(gameId)).
                platform(platform).gameName(getGameNameById(gameId)).code(code).
                cart(cartRepository.getCartById(cartId)).build();
        return cartItemRepository.save(cartItem);
    }

    private Float getGamePriceById(Long gameId) {
        String gameApiUrl = catalogApiUrl+"/Game/getGameById/" + gameId;
        RestOperations restTemplate = new RestTemplate();;
        String gameJsonResponse = restTemplate.getForObject(gameApiUrl, String.class);

        if (gameJsonResponse == null) {
            return 0F;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(gameJsonResponse);

            JsonNode priceNode = rootNode.get("price");

            if (priceNode != null && priceNode.isNumber()) {
                return priceNode.floatValue();
            } else {
                return 0F;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0F;
        }


    }


    private String getGameNameById(Long gameId) {
        String gameApiUrl = catalogApiUrl+"/Game/getGameById/" + gameId;

        // Create an instance of RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Use restTemplate to get the game information
        String gameJsonResponse = restTemplate.getForObject(gameApiUrl, String.class);

        if (gameJsonResponse == null) {
            return "Unknown";
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(gameJsonResponse);

            JsonNode nameNode = rootNode.get("name");

            if (nameNode != null && nameNode.isTextual()) {
                return nameNode.textValue();
            } else {
                return "Unknown";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }


    public boolean deleteCartItem(Long cartItemId) {
        Optional<CartItem> cartItemOptional = cartItemRepository.findById(cartItemId);

        if (cartItemOptional.isPresent()) {
            cartItemRepository.deleteById(cartItemId);
            return true;
        } else {
            return false;
        }
    }

    public void deleteCartItemsByGameId(Long gameId) {
        List<CartItem> cartItems = cartItemRepository.findByGameId(gameId);

        if (!cartItems.isEmpty()) {
            for (CartItem cartItem: cartItems){
                if(cartItem.getOrderCart() == null){
                    cartItemRepository.deleteById(cartItem.getId());
                }
            }
        }
    }


}
