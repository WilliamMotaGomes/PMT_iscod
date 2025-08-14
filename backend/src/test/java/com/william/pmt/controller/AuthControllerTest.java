package com.william.pmt.controller;

import com.william.pmt.dto.LoginRequest;
import com.william.pmt.dto.RegisterRequest;
import com.william.pmt.model.User;
import com.william.pmt.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthControllerTest
{
    @Test
    void register_calls_service_and_returns_user()
    {
        UserService users = mock(UserService.class);
        User u = new User(); u.setUsername("alice");
        when(users.register(anyString(), anyString(), anyString())).thenReturn(u);

        AuthController ctrl = new AuthController(users);
        User res = ctrl.register(new RegisterRequest("alice", "a@a.com", "mdp"));

        assertSame(u, res);
        verify(users).register("alice", "a@a.com", "mdp");
    }

    @Test
    void login_ok_returns_200_with_body()
    {
        UserService users = mock(UserService.class);
        User u = new User(); u.setUsername("bob");
        when(users.login("b@b.com", "mdp")).thenReturn(Optional.of(u));

        AuthController ctrl = new AuthController(users);
        ResponseEntity<User> resp = ctrl.login(new LoginRequest("b@b.com", "mdp"));

        assertEquals(200, resp.getStatusCode().value());
        assertSame(u, resp.getBody());
    }

    @Test
    void login_ko_returns_401()
    {
        UserService users = mock(UserService.class);
        when(users.login("w@w.io", "mdp")).thenReturn(Optional.empty());

        AuthController ctrl = new AuthController(users);
        ResponseEntity<User> resp = ctrl.login(new LoginRequest("w@w.com", "mdp"));

        assertEquals(401, resp.getStatusCode().value());
        assertNull(resp.getBody());
    }
}
