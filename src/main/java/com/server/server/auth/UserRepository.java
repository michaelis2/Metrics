package com.server.server.auth;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    public Optional<User> findByUsername(String var1);

    public boolean existsByUsername(String var1);

    public Optional<User> findByEmail(String var1);
}
