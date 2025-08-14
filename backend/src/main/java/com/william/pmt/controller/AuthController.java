package com.william.pmt.controller;

import com.william.pmt.dto.*;
import com.william.pmt.model.User;
import com.william.pmt.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth")
@CrossOrigin
public class AuthController
{
    private final UserService users;

    public AuthController(UserService users)
    {
        this.users = users;
    }

    /** Rajoute un utilisateur dans la DB pour se connecter par la suite */
    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest req)
    {
        return users.register(req.username(), req.email(), req.password());
    }

    /** Appel pour se connecter avec les identifiants */
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequest req)
    {
        return users.login(req.email(), req.password())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }
}
