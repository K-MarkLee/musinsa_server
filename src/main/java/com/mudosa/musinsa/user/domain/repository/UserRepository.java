package com.mudosa.musinsa.user.domain.repository;

import com.mudosa.musinsa.order.application.dto.UserInfoDto;
import com.mudosa.musinsa.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUserEmail(String email);
    
    boolean existsByUserEmail(String email);

    UserInfoDto findDtoById(Long userId);
}
