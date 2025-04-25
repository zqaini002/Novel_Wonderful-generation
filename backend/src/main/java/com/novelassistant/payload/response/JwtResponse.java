package com.novelassistant.payload.response;

import java.util.Date;
import java.util.List;

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private List<String> roles;
    private Date createdAt;
    private Date lastLoginAt;

    public JwtResponse(String accessToken, Long id, String username, String email, String nickname, List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.nickname = nickname;
        this.roles = roles;
    }
    
    public JwtResponse(String accessToken, Long id, String username, String email, String nickname, List<String> roles, Date createdAt, Date lastLoginAt) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.nickname = nickname;
        this.roles = roles;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    public String getAccessToken() {
        return token;
    }

    public void setAccessToken(String accessToken) {
        this.token = accessToken;
    }

    public String getTokenType() {
        return type;
    }

    public void setTokenType(String tokenType) {
        this.type = tokenType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public List<String> getRoles() {
        return roles;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getLastLoginAt() {
        return lastLoginAt;
    }
    
    public void setLastLoginAt(Date lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
} 