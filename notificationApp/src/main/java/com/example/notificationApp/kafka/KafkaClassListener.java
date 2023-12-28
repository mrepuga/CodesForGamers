package com.example.notificationApp.kafka;

import com.example.notificationApp.services.NotificationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

@Log4j2
@Component
public class KafkaClassListener {

    @Autowired
    private NotificationService notificationService;

    @KafkaListener(topics = KafkaConstants.GAME_TOPIC + KafkaConstants.SEPARATOR + KafkaConstants.ITEM_AVAILABLE, groupId = "group-1")
    void gameAvailable(GameMessage gameMessage) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, UnrecoverableKeyException {
        log.trace("productAvailable");

        notificationService.notifyProductAvailable(gameMessage);
    }


}
