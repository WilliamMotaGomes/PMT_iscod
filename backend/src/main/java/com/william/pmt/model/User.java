package com.william.pmt.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "users")
public class User
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(unique = true)
    private String email;

    private String passwordHash;
    private Instant createdAt = Instant.now();

    public Long getId(){ return id; }
    public String getUsername(){ return username; }
    public void setUsername(String username){ this.username = username; }
    public String getEmail(){ return email; }
    public void setEmail(String email){ this.email = email; }
    public String getPasswordHash(){ return passwordHash; }
    public void setPasswordHash(String passwordHash){ this.passwordHash = passwordHash; }
}
