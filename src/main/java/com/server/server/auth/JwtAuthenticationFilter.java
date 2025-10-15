package com.server.server.auth;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter
        extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService uds) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = uds;
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String token;
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ") && this.jwtUtil.validateToken(token = header.substring(7))) {
            String username = this.jwtUtil.extractUsername(token);
            String role = this.jwtUtil.extractRole(token);
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken((Object)userDetails, null, authorities);
            SecurityContextHolder.getContext().setAuthentication((Authentication)auth);
        }
        chain.doFilter((ServletRequest)request, (ServletResponse)response);
    }
}

