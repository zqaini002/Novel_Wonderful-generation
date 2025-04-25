package com.novelassistant.util;

import com.novelassistant.entity.User;
import com.novelassistant.repository.UserRepository;
import com.novelassistant.service.SystemLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 系统日志切面，用于自动记录API请求和异常
 */
@Aspect
@Component
public class SystemLogAspect {

    private final Logger logger = LogUtil.getLogger(SystemLogAspect.class);
    
    @Autowired
    private SystemLogService systemLogService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 定义控制器方法的切点
     */
    @Pointcut("execution(* com.novelassistant.controller.*.*(..))")
    public void controllerPointcut() {
    }
    
    /**
     * 在控制器方法执行前记录请求日志
     */
    @Before("controllerPointcut()")
    public void logBefore(JoinPoint joinPoint) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            
            HttpServletRequest request = attributes.getRequest();
            
            // 获取请求信息
            String url = request.getRequestURL().toString();
            String method = request.getMethod();
            String ip = getClientIp(request);
            String className = joinPoint.getSignature().getDeclaringTypeName();
            String methodName = joinPoint.getSignature().getName();
            Object[] args = joinPoint.getArgs();
            
            // 获取当前用户ID
            Long userId = getCurrentUserId();
            
            // 日志消息
            String message = String.format("API请求 - %s %s", method, url);
            
            // 请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("class", className);
            params.put("method", methodName);
            params.put("args", getParameterNames(joinPoint, args));
            params.put("ip", ip);
            params.put("url", url);
            params.put("httpMethod", method);
            
            // 记录日志
            systemLogService.addSystemLog("INFO", "API_REQUEST", message, userId != null ? userId.toString() : null, ip, 
                    Thread.currentThread().getName(), null, params);
            
        } catch (Exception e) {
            logger.error("记录API请求日志时发生错误", e);
        }
    }
    
    /**
     * 捕获控制器方法抛出的异常
     */
    @AfterThrowing(pointcut = "controllerPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            
            HttpServletRequest request = attributes.getRequest();
            
            // 获取请求信息
            String url = request.getRequestURL().toString();
            String method = request.getMethod();
            String ip = getClientIp(request);
            String className = joinPoint.getSignature().getDeclaringTypeName();
            String methodName = joinPoint.getSignature().getName();
            
            // 获取当前用户ID
            Long userId = getCurrentUserId();
            
            // 日志消息
            String message = String.format("API异常 - %s %s: %s", method, url, e.getMessage());
            
            // 异常堆栈
            String stackTrace = LogUtil.formatException(e);
            
            // 请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("class", className);
            params.put("method", methodName);
            params.put("exceptionType", e.getClass().getName());
            params.put("ip", ip);
            params.put("url", url);
            params.put("httpMethod", method);
            
            // 记录日志
            systemLogService.addSystemLog("ERROR", "API_EXCEPTION", message, userId != null ? userId.toString() : null, ip, 
                    Thread.currentThread().getName(), stackTrace, params);
            
        } catch (Exception ex) {
            logger.error("记录API异常日志时发生错误", ex);
        }
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                    !"anonymousUser".equals(authentication.getPrincipal())) {
                String username = authentication.getName();
                
                // 通过用户名从数据库查找用户ID
                Optional<User> userOpt = userRepository.findByUsername(username);
                if (userOpt.isPresent()) {
                    return userOpt.get().getId();
                } else {
                    logger.warn("无法通过用户名 '{}' 找到用户", username);
                }
            }
        } catch (Exception e) {
            logger.warn("获取当前用户ID时发生错误", e);
        }
        return null;
    }
    
    /**
     * 获取方法参数名和值的映射
     */
    private Map<String, Object> getParameterNames(JoinPoint joinPoint, Object[] args) {
        Map<String, Object> params = new HashMap<>();
        
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] parameterNames = signature.getParameterNames();
            
            for (int i = 0; i < parameterNames.length && i < args.length; i++) {
                Object arg = args[i];
                String value = arg == null ? "null" : arg.toString();
                
                // 避免记录过长的参数值
                if (value.length() > 500) {
                    value = value.substring(0, 500) + "... (截断)";
                }
                
                params.put(parameterNames[i], value);
            }
        } catch (Exception e) {
            // 如果无法获取参数名，则使用索引作为键
            logger.warn("无法获取方法参数名，使用索引代替", e);
            for (int i = 0; i < args.length; i++) {
                params.put("arg" + i, args[i]);
            }
        }
        
        return params;
    }
} 