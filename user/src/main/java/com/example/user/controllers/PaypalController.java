package com.example.user.controllers;

import com.example.user.controllers.dtos.MergedCartItem;
import com.example.user.entities.Cart;
import com.example.user.entities.CartItem;
import com.example.user.entities.OrderCart;
import com.example.user.entities.User;
import com.example.user.repositories.CartRepository;
import com.example.user.repositories.OrderCartRepository;
import com.example.user.services.CartService;
import com.example.user.services.PaypalService;
import com.example.user.utils.JsonMapper;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PaypalController {

    private  final PaypalService paypalService;
    private final CartRepository cartRepository;

    private final CartService cartService;

    private final RestTemplate restTemplate;
    private final JsonMapper jsonMapper;


    @PostMapping("/payment/create")
    public RedirectView createPayment(@RequestParam String totalPrice, HttpSession session){
        User user = (User) session.getAttribute("user");

        if(user == null){
            return new RedirectView("/home");
        }

        try{

           String cancelUrl = "https://localhost/payment/cancel";
           String successUrl = "https://localhost/payment/success";

            // change payment to actual values
           Payment payment = paypalService.createPayment(Double.valueOf(totalPrice), "EUR", "paypal", "sale", "Payment description", cancelUrl, successUrl);

           for(Links links: payment.getLinks()){
               if(links.getRel().equals("approval_url")){
                   return new RedirectView(links.getHref());
               }

           }
        }catch(PayPalRESTException e){
        log.error("Error occurred:",e);

        }
        return new RedirectView("/payment/error");
    }

    @GetMapping("/payment/success")
    public String paymentSuccess(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId, HttpSession session, Model model){

        User user = (User) session.getAttribute("user");

        if(user == null){
            return "redirect:/home";
        }

        try{
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if(payment.getState().equals("approved")){

               Cart cart = cartRepository.getCartByUserId(user.getId());
               if(cart.getCartItems().size() == 0){
                   return "paymentSuccess";
               }

               OrderCart orderCart = cartService.buyCart(cart.getId());

               List<CartItem> cartItems = orderCart.getCartItems();
               LocalDateTime time = orderCart.getPurchaseDateTime();

                List<MergedCartItem> mergedCartItems = new ArrayList<>();
                for (CartItem cartItem : cartItems) {
                    Map<String, Object> gameDetail = fetchGameDetails(cartItem.getGameId());
                    mergedCartItems.add(new MergedCartItem(cartItem, gameDetail));

                }


               model.addAttribute("mergedCartItems", mergedCartItems);
               model.addAttribute("time", time);


                return "user/paymentSuccess";
            }

        }catch(PayPalRESTException e){
            log.error("Error occurred:",e);
        }
        return "paymentSuccess";
    }
    @GetMapping("/payment/cancel")
    public String paymentCancel(){
        return "user/paymentCancel";
    }
    @GetMapping("/payment/error")
    public String paymentError(){
        return "user/paymentError";
    }


    ///AUX FUNCS

    private Map<String, Object> fetchGameDetails(Long gameId) {
        String apiUrl = "http://localhost:18081/Game/getGameById/"+gameId;
        String jsonResponse = restTemplate.getForObject(apiUrl, String.class);
        return jsonMapper.parseJsonObjectResponse(jsonResponse);
    }



}
