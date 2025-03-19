package com.nhhoang.e_commerce.security.jwt;

import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader("token");

        if (token != null && token.startsWith("Bearer ")) {
            try {
                String jwtToken = token.substring(7);
                String userId = jwtUtil.extractUserId(jwtToken);

                User user = userRepository.findById(userId).orElse(null);
                if (user != null && !jwtUtil.isTokenExpired(jwtToken)) {
                    request.setAttribute("user", user);
                } else {
                    request.setAttribute("user", null);
                }
            } catch (Exception e) {
                System.out.println("Invalid token: " + e.getMessage());
                request.setAttribute("user", null);
            }
        } else {
            System.out.println("No valid Authorization header found");
            request.setAttribute("user", null);
        }
        filterChain.doFilter(request, response);
    }
}