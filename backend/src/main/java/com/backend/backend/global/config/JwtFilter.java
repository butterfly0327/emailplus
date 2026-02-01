package com.backend.backend.global.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtTokenUtil jwtTokenUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final long accessExpiration;
    private final List<String> permitAllPaths;

    private static final String REFRESH_TOKEN_PREFIX = "RT:";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        if (isPermitAllPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String accessToken = jwtTokenUtil.resolveAccessToken(request);

        if (accessToken == null) {
            log.warn("Access token이 존재하지 않습니다. URI: {}", requestURI);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access token이 필요합니다.");
            return;
        }

        if (jwtTokenUtil.validateToken(accessToken)) {
            UsernamePasswordAuthenticationToken authenticationToken = jwtTokenUtil.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        } else {
            // Access token 만료 시 refresh token으로 재발급 시도
            String accountId = jwtTokenUtil.getClaims(accessToken).getSubject();
            String refreshToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + accountId);

            if (refreshToken == null || !jwtTokenUtil.validateToken(refreshToken)) {
                log.warn("Refresh token이 없거나 만료되었습니다. accountId: {}", accountId);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰이 만료되었습니다. 다시 로그인해주세요.");
                return;
            }

            Claims claims = jwtTokenUtil.getClaims(refreshToken);
            String id = claims.getSubject();
            String newToken = jwtTokenUtil.createToken(id, accessExpiration);
            jwtTokenUtil.setHeaderAccessToken(response, newToken);

            UsernamePasswordAuthenticationToken authenticationToken = jwtTokenUtil.getAuthentication(newToken);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            log.info("Access token 재발급 완료. accountId: {}", id);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPermitAllPath(String requestURI) {
        return permitAllPaths.stream().anyMatch(path -> {
            if (path.endsWith("/**")) {
                String basePath = path.substring(0, path.length() - 3);
                return requestURI.startsWith(basePath);
            }
            return requestURI.equals(path);
        });
    }
}
