package com.example.user.repositories;

import com.example.user.entities.TypeUser;
import com.example.user.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByTypeUser(TypeUser client);

    boolean existsByEmail(String email);

    User findByEmail(String email);


}
