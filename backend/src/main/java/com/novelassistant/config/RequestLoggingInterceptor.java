package com.novelassistant.config;

import com.novelassistant.util.LogUtil;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * 请求日志拦截器
 * 记录所有HTTP请求的详细信息，包括请求参数、响应状态等
 */
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LogUtil.getLogger(RequestLoggingInterceptor.class);
    private static final String REQUEST_START_TIME = "requestStartTime";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 生成并设置请求ID
        String requestId = request.getHeader("X-Request-ID");
        requestId = LogUtil.startRequest(requestId);
        
        // 记录请求开始时间
        request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());
        
        // 记录请求信息
        StringBuilder logMessage = new StringBuilder("\n请求开始 ------------------------------------------------\n");
        logMessage.append("请求ID: ").append(requestId).append("\n");
        logMessage.append("请求路径: ").append(request.getMethod()).append(" ").append(request.getRequestURI()).append("\n");
        
        // 记录请求头
        logMessage.append("请求头: \n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // 跳过敏感信息
            if ("Authorization".equalsIgnoreCase(headerName)) {
                logMessage.append("  ").append(headerName).append(": [PROTECTED]\n");
            } else {
                logMessage.append("  ").append(headerName).append(": ").append(request.getHeader(headerName)).append("\n");
            }
        }
        
        // 记录请求参数
        logMessage.append("请求参数: \n");
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            logMessage.append("  ").append(paramName).append(": ").append(request.getParameter(paramName)).append("\n");
        }
        
        logger.debug(logMessage.toString());
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // 在请求处理后、视图渲染前调用
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 计算请求处理时间
        Long startTime = (Long) request.getAttribute(REQUEST_START_TIME);
        long duration = System.currentTimeMillis() - startTime;
        
        // 记录响应信息
        StringBuilder logMessage = new StringBuilder("\n请求结束 ------------------------------------------------\n");
        logMessage.append("请求路径: ").append(request.getMethod()).append(" ").append(request.getRequestURI()).append("\n");
        logMessage.append("响应状态: ").append(response.getStatus()).append("\n");
        logMessage.append("处理时间: ").append(duration).append("ms\n");
        
        // 记录异常信息（如果有）
        if (ex != null) {
            logMessage.append("异常信息: \n").append(LogUtil.formatException(ex)).append("\n");
        }
        
        logger.debug(logMessage.toString());
        
        // 清理MDC中的数据
        LogUtil.endRequest();
    }
} 