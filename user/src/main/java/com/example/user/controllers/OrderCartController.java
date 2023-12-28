package com.example.user.controllers;

import com.example.user.controllers.dtos.MergedCartItem;
import com.example.user.controllers.dtos.MergedOrderCartItem;
import com.example.user.entities.CartItem;
import com.example.user.entities.OrderCart;
import com.example.user.entities.User;
import com.example.user.repositories.OrderCartRepository;
import com.example.user.utils.JsonMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Log4j2
@Controller
@RequestMapping("/orderCart")
public class OrderCartController {

    private final OrderCartRepository orderCartRepository;

    private final RestTemplate restTemplate;
    private final JsonMapper jsonMapper;

    @Value("${catalog.api.url}")
    private String catalogApiUrl;
    @Autowired
    public OrderCartController(OrderCartRepository orderCartRepository, RestTemplate restTemplate, JsonMapper jsonMapper) {
        this.orderCartRepository = orderCartRepository;
        this.restTemplate = restTemplate;
        this.jsonMapper = jsonMapper;
    }

    @GetMapping("/show")
    public String showOrderCarts(Model model, HttpSession session) {

        if (session.getAttribute("user") == null || session.getAttribute("admin") != null) {
            return "redirect:/home";
        }
        User user = (User) session.getAttribute("user");

        List<OrderCart> orderCarts = orderCartRepository.findByUserId(user.getId());

        List<MergedOrderCartItem> mergedOrderCartItems = new ArrayList<>();

        for (OrderCart orderCart : orderCarts) {
            List<CartItem> cartItems = orderCart.getCartItems();
            LocalDateTime time = orderCart.getPurchaseDateTime();

            List<MergedCartItem> mergedCartItems = new ArrayList<>();
            for (CartItem cartItem : cartItems) {
                Map<String, Object> gameDetail = fetchGameDetails(cartItem.getGameId());
                mergedCartItems.add(new MergedCartItem(cartItem, gameDetail));
            }

            mergedOrderCartItems.add(new MergedOrderCartItem(orderCart, mergedCartItems, time));
        }

        mergedOrderCartItems.sort(Comparator.comparing(MergedOrderCartItem::getTime).reversed());
        model.addAttribute("mergedOrderCartItems", mergedOrderCartItems);

        return "user/orderCarts";
    }

    ///AUX FUNCS

    private Map<String, Object> fetchGameDetails(Long gameId) {
        String apiUrl = catalogApiUrl+"/Game/getGameById/"+gameId;
        String jsonResponse = restTemplate.getForObject(apiUrl, String.class);
        return jsonMapper.parseJsonObjectResponse(jsonResponse);
    }


}
