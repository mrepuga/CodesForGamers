package com.example.user.repositories;

import com.example.user.entities.Alert;
import com.example.user.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    Alert findByUser(User user);

    void deleteByGameIdAndUser(Long gameId, User currentUser);

    List<Alert> findAlertsByGameId(Long gameId);

    List<Alert> findAlertsByUser(User user);

    Alert findByUserAndGameId(User user, Long gameId);
}
