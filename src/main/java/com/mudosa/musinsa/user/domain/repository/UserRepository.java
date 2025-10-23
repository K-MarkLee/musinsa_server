package com.mudosa.musinsa.user.domain.repository;

import com.mudosa.musinsa.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUserEmail(String email);
    
    boolean existsByUserEmail(String email);
}
