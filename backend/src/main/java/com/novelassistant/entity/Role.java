package com.novelassistant.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;

/**
 * 角色实体类
 */
@Data
@Entity
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true)
    private ERole name;
    
    public Role(ERole name) {
        this.name = name;
    }
    
    /**
     * 角色枚举
     */
    public enum ERole {
        ROLE_USER,    // 普通用户
        ROLE_ADMIN    // 管理员
    }
} 