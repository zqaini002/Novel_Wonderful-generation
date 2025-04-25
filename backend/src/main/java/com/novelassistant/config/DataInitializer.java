package com.novelassistant.config;

import com.novelassistant.entity.Role;
import com.novelassistant.entity.Role.ERole;
import com.novelassistant.entity.User;
import com.novelassistant.repository.RoleRepository;
import com.novelassistant.repository.UserRepository;
import com.novelassistant.util.AuthenticationLogger;
import com.novelassistant.util.LogUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 数据初始化类
 * 用于初始化角色和默认管理员账号
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LogUtil.getLogger(DataInitializer.class);
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationLogger authLogger;

    @Override
    public void run(String... args) throws Exception {
        logger.info("开始初始化基础数据...");
        initRoles();
        initAdminUser();
        checkExistingUsers();
        logger.info("基础数据初始化完成");
    }
    
    /**
     * 初始化角色
     */
    private void initRoles() {
        if (roleRepository.count() == 0) {
            logger.info("创建默认角色...");
            Role userRole = new Role(ERole.ROLE_USER);
            Role adminRole = new Role(ERole.ROLE_ADMIN);
            
            roleRepository.save(userRole);
            roleRepository.save(adminRole);
            logger.info("创建了 {} 个角色: {}, {}", 2, userRole.getName(), adminRole.getName());
        } else {
            logger.debug("角色已存在，跳过创建");
        }
    }
    
    /**
     * 初始化管理员用户
     */
    private void initAdminUser() {
        String adminUsername = "admin";
        Optional<User> existingAdmin = userRepository.findByUsername(adminUsername);
        
        if (existingAdmin.isPresent()) {
            User admin = existingAdmin.get();
            logger.debug("管理员用户已存在 - ID: {}, 用户名: {}", admin.getId(), admin.getUsername());
            logger.debug("当前密码: {}", admin.getPassword());
            
            // 检查密码是否为"admin123"
            if (!passwordEncoder.matches("admin123", admin.getPassword())) {
                logger.info("重置管理员密码为默认值: admin123");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setUpdatedAt(new Date());
                userRepository.save(admin);
                logger.info("管理员密码已重置");
            }
            
            // 确保拥有管理员角色
            boolean hasAdminRole = admin.getRoles().stream()
                    .anyMatch(role -> role.getName() == ERole.ROLE_ADMIN);
            
            if (!hasAdminRole) {
                logger.info("为管理员用户添加ADMIN角色");
                Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("未找到ADMIN角色"));
                admin.getRoles().add(adminRole);
                userRepository.save(admin);
                logger.info("已为管理员用户添加ADMIN角色");
            }
        } else {
            logger.info("创建默认管理员用户: {}", adminUsername);
            
            // 创建新管理员用户
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail("admin@example.com");
            admin.setNickname("系统管理员");
            
            // 使用AuthLogger记录密码加密过程
            String encodedPassword = authLogger.encodeAndLogPassword("admin123");
            admin.setPassword(encodedPassword);
            
            // 设置角色
            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("未找到USER角色"));
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("未找到ADMIN角色"));
            
            roles.add(userRole);
            roles.add(adminRole);
            admin.setRoles(roles);
            
            // 设置其他属性
            admin.setEnabled(true);
            admin.setCreatedAt(new Date());
            admin.setUpdatedAt(new Date());
            
            userRepository.save(admin);
            logger.info("默认管理员用户创建成功 - ID: {}", admin.getId());
        }
    }
    
    /**
     * 检查已存在用户的密码
     */
    private void checkExistingUsers() {
        logger.debug("检查现有用户密码情况...");
        userRepository.findAll().forEach(user -> {
            logger.debug("用户: {}, 密码: {}", user.getUsername(), user.getPassword());
            // 检查是否使用了BCrypt加密算法
            if (!user.getPassword().startsWith("$2a$")) {
                logger.warn("用户 {} 的密码未使用BCrypt加密，可能存在安全风险", user.getUsername());
            }
        });
    }
} 