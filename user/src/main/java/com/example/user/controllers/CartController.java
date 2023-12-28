package com.example.user.controllers;

import com.example.user.controllers.dtos.MergedCartItem;
import com.example.user.entities.Cart;
import com.example.user.entities.CartItem;
import com.example.user.entities.OrderCart;
import com.example.user.entities.User;
import com.example.user.repositories.CartItemRepository;
import com.example.user.repositories.CartRepository;
import com.example.user.services.CartItemService;
import com.example.user.services.CartService;
import com.example.user.utils.JsonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;
    private final CartRepository cartRepository;
    private final RestTemplate restTemplate;
    private final JsonMapper jsonMapper;
    private final CartItemService cartItemService;
    private  final CartItemRepository cartItemRepository;
    @Value("${catalog.api.url}")
    private String catalogApiUrl;

    @Autowired
    public CartController(CartService cartService, CartRepository cartRepository,
                          RestTemplate restTemplate, JsonMapper jsonMapper, CartItemService cartItemService, CartItemRepository cartItemRepository) {
        this.cartService = cartService;
        this.cartRepository = cartRepository;
        this.restTemplate = restTemplate;
        this.jsonMapper = jsonMapper;
        this.cartItemService = cartItemService;
        this.cartItemRepository = cartItemRepository;
    }


    @GetMapping("")
    public String showCart(Model model, HttpSession session) {

        if (session.getAttribute("user") == null || session.getAttribute("admin") != null) {
            return "redirect:/home";
        }
        User user = (User) session.getAttribute("user");

        if (user != null && user.getCart() != null) {

            Cart cart = cartRepository.getCartByUserId(user.getId());
            List<CartItem> cartItems = cart.getCartItems();
            Float totalPrice = 0.00F;

            List<MergedCartItem> mergedCartItems = new ArrayList<>();
            for (CartItem cartItem : cartItems) {
               Map<String, Object> gameDetail = fetchGameDetails(cartItem.getGameId());
                mergedCartItems.add(new MergedCartItem(cartItem, gameDetail));
                totalPrice = totalPrice + cartItem.getPrice();
            }

            model.addAttribute("totalPrice", totalPrice);
            model.addAttribute("mergedCartItems", mergedCartItems);
        }
        return "user/cart";

    }

    @DeleteMapping("/{cartItemId}")
    public String deleteCartItem(@PathVariable Long cartItemId, HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/home";
        }


        CartItem cartItem = cartItemRepository.getById(cartItemId);

        if (!user.equals(cartItem.getCart().getUser())) {
            // The user is not the owner of the cart item, handle accordingly
            return "redirect:/home";
        }

        String code = cartItem.getCode();

        String apiUrl = catalogApiUrl+"/CodeGameItem/unselectCodeGameItem/"+code;
        String codeResponse = restTemplate.getForObject(apiUrl, String.class);

        boolean deleted = cartItemService.deleteCartItem(cartItemId);

        if (deleted) {
            return "redirect:/cart";
        } else {
            return "ERROR DELETING cartItem";
        }
    }

///AUX FUNCS

    private Map<String, Object> fetchGameDetails(Long gameId) {
        String apiUrl = catalogApiUrl+"/Game/getGameById/"+gameId;
        String jsonResponse = restTemplate.getForObject(apiUrl, String.class);
        return jsonMapper.parseJsonObjectResponse(jsonResponse);
    }





}
