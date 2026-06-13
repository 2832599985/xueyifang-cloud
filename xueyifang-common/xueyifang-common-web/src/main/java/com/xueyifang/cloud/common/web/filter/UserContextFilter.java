package com.xueyifang.cloud.common.web.filter;

import com.xueyifang.cloud.common.core.auth.AuthConstants;
import com.xueyifang.cloud.common.core.context.LoginUserContext;
import com.xueyifang.cloud.common.core.context.UserContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        UserContextHolder.clear();
        try {
            resolveUserContext(request).ifPresent(UserContextHolder::set);
            filterChain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
        }
    }

    private Optional<LoginUserContext> resolveUserContext(HttpServletRequest request) {
        Optional<Long> userId = parseLong(request.getHeader(AuthConstants.USER_ID_HEADER));
        if (userId.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new LoginUserContext(
                userId.get(),
                parseInteger(request.getHeader(AuthConstants.USER_ROLE_HEADER)).orElse(null),
                parseInteger(request.getHeader(AuthConstants.USER_PUBLISH_PERMISSION_HEADER)).orElse(null)));
    }

    private Optional<Long> parseLong(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(value.trim()));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private Optional<Integer> parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(value.trim()));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }
}
