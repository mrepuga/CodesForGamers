package com.example.notificationApp.services;


import com.example.notificationApp.kafka.GameMessage;
import com.example.notificationApp.model.UserDto;

import com.example.notificationApp.util.CustomRestTemplate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.util.List;


@Log4j2
@Component

public class NotificationService {


    private final JavaMailSender javaMailSender;


    @Autowired
    public NotificationService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;

    }

    public void notifyProductAvailable(GameMessage gameMessage) throws IOException {
        log.info(gameMessage.toString());

        List<UserDto> userDtoList = getUsersToAlert(gameMessage);

        for (UserDto userDto : userDtoList) {
            sendAlertMail(userDto.getEmail(), gameMessage.getGameId(), gameMessage.getName(), gameMessage.getPrice());
        }


    }

    private List<UserDto> getUsersToAlert(GameMessage gameMessage) throws IOException {
        String gameId = gameMessage.getGameId().toString();
        final String URI = String.format("https://localhost/user/getUsersToAlert?gameId=%s", gameId);

        try {
            // Use the custom RestTemplate that trusts all certificates
            RestTemplate restTemplate = new CustomRestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(URI, String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(responseEntity.getBody(), new TypeReference<List<UserDto>>() {});
            } else {
                // Handle non-successful response (e.g., log or throw an exception)
                throw new RuntimeException("Failed to retrieve users: " + responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            // Handle exceptions (e.g., log or rethrow)
            throw new IOException("Error while retrieving users", e);
        }

    }







    private void sendAlertMail(String email, Long gameId, String name, Float price) {
        log.info("Hello "+ email+", the gameId: ID:"+gameId+": "+ name+", "+ price+"€ is now currently available." );

        // Email properties
        String subject = "Game Availability Alert";
        String body = String.format("Hello %s,\n\nThe game with ID %s (%s) is now available at the price of € %s.", email, gameId.toString(), name, price.toString());
        // Create a SimpleMailMessage
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(subject);
        message.setText(body);
        message.setTo(email);

        // Send the email
        try {
            javaMailSender.send(message);
            log.info("Email sent successfully.");
        } catch (Exception e) {
            log.error("Error sending email: " + e.getMessage(), e);
        }
    }

}




