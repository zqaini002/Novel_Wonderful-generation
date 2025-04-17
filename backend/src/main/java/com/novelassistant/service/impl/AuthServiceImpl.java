package com.novelassistant.service.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.novelassistant.entity.Role;
import com.novelassistant.entity.Role.ERole;
import com.novelassistant.entity.User;
import com.novelassistant.payload.request.LoginRequest;
import com.novelassistant.payload.request.SignupRequest;
import com.novelassistant.payload.response.JwtResponse;
import com.novelassistant.payload.response.MessageResponse;
import com.novelassistant.repository.RoleRepository;
import com.novelassistant.repository.UserRepository;
import com.novelassistant.security.jwt.JwtUtils;
import com.novelassistant.security.services.UserDetailsImpl;
import com.novelassistant.service.AuthService;
import com.novelassistant.util.AuthenticationLogger;
import com.novelassistant.util.LogUtil;
import jakarta.annotation.PostConstruct;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger logger = LogUtil.getLogger(AuthServiceImpl.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationLogger authLogger;

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        logger.debug("开始认证用户: {}", loginRequest.getUsername());
        try {
            // 查询用户信息用于调试
            User user = userRepository.findByUsername(loginRequest.getUsername()).orElse(null);
            if (user != null) {
                logger.debug("用户存在: {}, 密码加密值: {}", user.getUsername(), user.getPassword());
                
                // 检查用户是否被禁用
                if (!user.isEnabled()) {
                    logger.warn("用户 {} 已被禁用，拒绝登录", user.getUsername());
                    throw new BadCredentialsException("账号已被禁用，请联系管理员");
                }
                
                // 记录密码验证细节，但不暴露原始密码
                authLogger.logPasswordVerification(user.getUsername(), loginRequest.getPassword(), user.getPassword());
            } else {
                logger.debug("用户不存在: {}", loginRequest.getUsername());
                throw new BadCredentialsException("用户不存在: " + loginRequest.getUsername());
            }
            
            // 尝试认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(), 
                            loginRequest.getPassword()));
            
            // 认证成功，设置上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 生成JWT令牌
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            // 获取用户详情
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());
            
            // 更新最后登录时间
            user.setLastLoginAt(new Date());
            userRepository.save(user);
            
            logger.info("用户 {} 认证成功, 拥有角色: {}", userDetails.getUsername(), roles);
            
            // 返回JWT响应
            return new JwtResponse(
                    jwt, 
                    userDetails.getId(), 
                    userDetails.getUsername(), 
                    userDetails.getEmail(),
                    userDetails.getNickname(),
                    roles,
                    user.getCreatedAt(),
                    user.getLastLoginAt());
        } catch (BadCredentialsException e) {
            logger.error("用户 {} 认证失败: {}", loginRequest.getUsername(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("认证过程发生异常: {}", e.getMessage());
            logger.error("异常详情: {}", LogUtil.formatException(e));
            throw e;
        }
    }

    @Override
    @Transactional
    public MessageResponse registerUser(SignupRequest signUpRequest) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            logger.warn("注册失败: 用户名已存在 {}", signUpRequest.getUsername());
            return new MessageResponse("错误: 用户名已被使用!");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            logger.warn("注册失败: 邮箱已存在 {}", signUpRequest.getEmail());
            return new MessageResponse("错误: 邮箱已被使用!");
        }

        // 创建新用户账号
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setNickname(signUpRequest.getNickname() != null ? signUpRequest.getNickname() : signUpRequest.getUsername());
        
        logger.debug("创建新用户: {}, 加密密码: {}", user.getUsername(), user.getPassword());
        
        Set<Role> roles = new HashSet<>();
        
        // 默认添加USER角色
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> {
                    logger.error("角色不存在: {}", ERole.ROLE_USER);
                    return new RuntimeException("错误: 角色不存在");
                });
        roles.add(userRole);
        
        // 如果请求中包含其他角色，则添加
        if (signUpRequest.getRoles() != null && !signUpRequest.getRoles().isEmpty()) {
            signUpRequest.getRoles().forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> {
                                    logger.error("角色不存在: {}", ERole.ROLE_ADMIN);
                                    return new RuntimeException("错误: 角色不存在");
                                });
                        roles.add(adminRole);
                        break;
                    default:
                        logger.warn("注册用户时指定了未知角色: {}", role);
                }
            });
        }

        user.setRoles(roles);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        user.setEnabled(true);
        userRepository.save(user);
        
        logger.info("用户注册成功: {}, 角色: {}", user.getUsername(), 
                roles.stream().map(r -> r.getName().toString()).collect(Collectors.joining(", ")));
        
        return new MessageResponse("用户注册成功!");
    }
    
    // 添加PostConstruct注解，确保在应用启动时被调用
    @PostConstruct
    @Transactional
    public void initializeRolesAndAdmin() {
        // 初始化角色
        createRoleIfNotFound(ERole.ROLE_USER);
        createRoleIfNotFound(ERole.ROLE_ADMIN);
        
        // 初始化管理员账号
        if (!userRepository.existsByUsername("admin")) {
            System.out.println("创建默认管理员账号: admin/admin123");
            
            User admin = new User(
                    "admin",
                    "admin@example.com",
                    encoder.encode("admin123"));
            admin.setNickname("管理员");
            
            Set<Role> roles = new HashSet<>();
            
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("错误: 用户角色未找到"));
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("错误: 管理员角色未找到"));
            
            roles.add(userRole);
            roles.add(adminRole);
            
            admin.setRoles(roles);
            userRepository.save(admin);
        }
    }
    
    private void createRoleIfNotFound(ERole name) {
        if (!roleRepository.findByName(name).isPresent()) {
            System.out.println("创建角色: " + name);
            roleRepository.save(new Role(name));
        }
    }
} 