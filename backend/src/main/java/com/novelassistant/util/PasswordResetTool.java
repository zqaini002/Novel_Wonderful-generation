package com.novelassistant.util;

import com.novelassistant.entity.User;
import com.novelassistant.repository.UserRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;
import java.util.Scanner;

/**
 * 密码重置工具
 * 仅在"passwordreset"配置文件激活时启用
 * 用法: 在启动时添加"--spring.profiles.active=passwordreset"参数
 */
@Component
@Profile("passwordreset")
public class PasswordResetTool implements CommandLineRunner {
    
    private static final Logger logger = LogUtil.getLogger(PasswordResetTool.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationLogger authLogger;
    
    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("===== 密码重置工具 =====");
        System.out.println("注意: 此工具用于管理员重置用户密码，请谨慎使用");
        
        // 显示所有用户
        System.out.println("\n当前系统中的所有用户:");
        userRepository.findAll().forEach(user -> {
            System.out.printf("ID: %d, 用户名: %s, 邮箱: %s%n", 
                    user.getId(), user.getUsername(), user.getEmail());
        });
        
        while (true) {
            System.out.print("\n请输入要重置密码的用户名(输入exit退出): ");
            String username = scanner.nextLine().trim();
            
            if ("exit".equalsIgnoreCase(username)) {
                break;
            }
            
            Optional<User> optionalUser = userRepository.findByUsername(username);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                System.out.printf("找到用户: %s (ID: %d)%n", user.getUsername(), user.getId());
                System.out.printf("当前密码哈希: %s%n", user.getPassword());
                
                System.out.print("请输入新密码: ");
                String newPassword = scanner.nextLine();
                
                if (newPassword.length() < 6) {
                    System.out.println("错误: 密码长度必须至少为6个字符");
                    continue;
                }
                
                // 生成新的加密密码
                String encodedPassword = authLogger.encodeAndLogPassword(newPassword);
                user.setPassword(encodedPassword);
                user.setUpdatedAt(new Date());
                userRepository.save(user);
                
                logger.info("用户 {} 的密码已重置", username);
                System.out.println("密码重置成功！");
                
                // 验证新密码
                boolean matches = passwordEncoder.matches(newPassword, encodedPassword);
                System.out.printf("密码验证测试: %s%n", matches ? "通过" : "失败");
            } else {
                System.out.printf("错误: 用户 '%s' 不存在%n", username);
            }
        }
        
        System.out.println("密码重置工具已退出。");
        System.exit(0);
    }
} 