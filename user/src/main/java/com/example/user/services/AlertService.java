package com.example.user.services;

import com.example.user.entities.Alert;
import com.example.user.entities.User;
import com.example.user.repositories.AlertRepository;
import com.example.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertService {
    @Autowired
    private AlertRepository alertRepository;
    @Autowired
    private UserRepository userRepository;


    public boolean userHasAlert(Long gameId, User user) {

       Alert alert =  alertRepository.findByUserAndGameId(user, gameId);

       return alert != null && alert.getGameId().equals(gameId);
    }

    @Transactional
    public void deleteAlert(Long gameId, User currentUser) {

        alertRepository.deleteByGameIdAndUser(gameId, currentUser);

    }

    public void createAlert(Long gameId, User currentUser) {
        Alert alert = Alert.builder().gameId(gameId).user(currentUser).build();
        alertRepository.save(alert);

    }
}
