package com.server.server.auth;


import java.util.Collections;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {
    private final UserRepository repo;

    public CustomUserDetailsService(UserRepository repo) {
        this.repo = repo;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = (User)this.repo.findByUsername(username).orElseThrow();
        return org.springframework.security.core.userdetails.User.withUsername((String)u.getUsername()).password(u.getPassword()).authorities(Collections.emptyList()).build();
    }
}
