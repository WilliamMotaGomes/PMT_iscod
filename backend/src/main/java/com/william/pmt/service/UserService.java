package com.william.pmt.service;

import com.william.pmt.model.User;
import com.william.pmt.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService
{
    private final UserRepository users;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository users)
    {
        this.users = users;
    }

    /** Enrengistre l'utilisateur et ses données dans la DB */
    public User register(String username, String email, String password)
    {
        User u = new User();

        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(password));

        return users.save(u);
    }

    /** Connecte l'utilisateur depuis ses informations de connexion */
    public Optional<User> login(String email, String password)
    {
        return users.findByEmail(email).filter(u -> encoder.matches(password, u.getPasswordHash()));
    }

    /** Trouve un utilisateur depuis son id */
    public Optional<User> findById(Long id)
    {
        return users.findById(id);
    }
}
