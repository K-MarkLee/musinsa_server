package com.mudosa.musinsa.common.domain.repository;

import com.mudosa.musinsa.common.domain.model.StatusCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * StatusCode Repository
 */
@Repository
public interface StatusCodeRepository extends JpaRepository<StatusCode, Integer> {
    
    List<StatusCode> findByDomainType(String domainType);
    
    Optional<StatusCode> findByStatusCodeAndDomainType(String statusCode, String domainType);
}
