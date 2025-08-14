package com.william.pmt.controller;

import com.william.pmt.model.User;
import com.william.pmt.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public abstract class BaseController
{
    private final UserService users;

    protected BaseController(UserService users)
    {
        this.users = users;
    }

    protected User currentUser(Long id)
    {
        return users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-User-Id invalide"));
    }
}
