package com.novelassistant.repository;

import com.novelassistant.entity.User;
import com.novelassistant.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 可能存在的用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 可能存在的用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 判断用户名是否已存在
     * @param username 用户名
     * @return 是否存在
     */
    Boolean existsByUsername(String username);
    
    /**
     * 判断邮箱是否已存在
     * @param email 邮箱
     * @return 是否存在
     */
    Boolean existsByEmail(String email);

    /**
     * 统计指定日期之后创建的用户数量
     * @param date 日期
     * @return 用户数量
     */
    long countByCreatedAtAfter(Date date);
    
    /**
     * 统计指定日期之后登录的用户数量
     * @param date 日期
     * @return 用户数量
     */
    long countByLastLoginAtAfter(Date date);
    
    /**
     * 统计具有特定角色名称的用户数量
     * @param roleName 角色名称
     * @return 用户数量
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countByRoles_Name(@Param("roleName") Role.ERole roleName);
} 