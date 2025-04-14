package com.novelassistant.payload.response;

import java.util.List;

public class JwtResponse {
    private String accessToken;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private List<String> roles;

    public JwtResponse(String accessToken, Long id, String username, String email, String nickname, List<String> roles) {
        this.accessToken = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.nickname = nickname;
        this.roles = roles;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
} 