package com.kepuxingqiu.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kepuxingqiu.common.context.UserContext;
import com.kepuxingqiu.common.result.Result;
import com.kepuxingqiu.common.result.ResultCode;
import com.kepuxingqiu.common.utils.JwtUtils;
import com.kepuxingqiu.common.utils.RedisUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String TOKEN_HEADER = "Authorization";

    private final JwtUtils jwtUtils;
    private final RedisUtils redisUtils;
    private final ObjectMapper objectMapper;

    public JwtInterceptor(JwtUtils jwtUtils, RedisUtils redisUtils, ObjectMapper objectMapper) {
        this.jwtUtils = jwtUtils;
        this.redisUtils = redisUtils;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader(TOKEN_HEADER);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(TOKEN_PREFIX)) {
            writeUnauthorized(response);
            return false;
        }

        String token = authHeader.substring(TOKEN_PREFIX.length());
        try {
            Long userId = jwtUtils.parseUserId(token);
            String redisKey = "token:" + userId;
            String storedToken = redisUtils.get(redisKey);
            if (!token.equals(storedToken)) {
                writeUnauthorized(response);
                return false;
            }
            // 续期：每次有效请求后重置过期时间
            redisUtils.expire(redisKey, jwtUtils.getExpireSeconds(), TimeUnit.SECONDS);
            UserContext.setUserId(userId);
            return true;
        } catch (Exception e) {
            writeUnauthorized(response);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(ResultCode.UNAUTHORIZED)));
    }
}
