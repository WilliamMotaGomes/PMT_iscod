package com.william.pmt.service;

import com.william.pmt.model.User;
import com.william.pmt.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceMoreTest
{
    @Test
    void register_hashes_password_and_saves()
    {
        UserRepository repo = mock(UserRepository.class);
        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        when(repo.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserService svc = new UserService(repo);
        User saved = svc.register("alice", "a@example.com", "secret");

        verify(repo).save(cap.capture());
        User u = cap.getValue();
        assertEquals("alice", u.getUsername());
        assertEquals("a@example.com", u.getEmail());

        assertNotEquals("secret", u.getPasswordHash());
        assertTrue(new BCryptPasswordEncoder().matches("secret", u.getPasswordHash()));

        assertEquals(saved.getUsername(), u.getUsername());
    }

    @Test
    void login_success_when_password_matches()
    {
        UserRepository repo = mock(UserRepository.class);
        User stored = new User();
        stored.setEmail("u@example.com");
        stored.setPasswordHash(new BCryptPasswordEncoder().encode("mdp"));
        when(repo.findByEmail("u@example.com")).thenReturn(Optional.of(stored));

        UserService svc = new UserService(repo);
        assertTrue(svc.login("u@example.com", "mdp").isPresent());
    }

    @Test
    void login_empty_when_password_wrong_or_user_missing()
    {
        UserRepository repo = mock(UserRepository.class);
        User stored = new User();
        stored.setEmail("u@example.com");
        stored.setPasswordHash(new BCryptPasswordEncoder().encode("mdp"));
        when(repo.findByEmail("u@example.com")).thenReturn(Optional.of(stored));
        when(repo.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        UserService svc = new UserService(repo);
        assertTrue(svc.login("u@example.com", "badmdp").isEmpty());
        assertTrue(svc.login("missing@example.com", "xxxx").isEmpty());
    }
}
