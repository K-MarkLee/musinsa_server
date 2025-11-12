package com.mudosa.musinsa.fbtoken.repository;

import com.mudosa.musinsa.fbtoken.model.FirebaseToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FirebaseTokenRepository extends JpaRepository<FirebaseToken, Long> {

    List<FirebaseToken> findByUserId(Long userId);

    List<FirebaseToken> findByUserIdIn(List<Long> userIds);

    @Modifying
    @Query("UPDATE FirebaseToken f SET f.firebaseTokenKey = :ftKey WHERE f.tokenId = :tId")
    int updateFirebaseToken(@Param("ftKey") String ftKey, @Param("tId") Long tId);

    @Modifying
    void deleteByTokenId(Long id);
}
