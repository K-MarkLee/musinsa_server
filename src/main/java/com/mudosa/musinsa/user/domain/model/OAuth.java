package com.mudosa.musinsa.user.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * OAuth 인증 정보 엔티티
 * User 애그리거트 내부
 */
@Entity
@Table(name = "oauth")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuth extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "oauth_id")
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "oauth_provider", nullable = false, length = 20)
    private String oauthProvider;
    
    @Column(name = "oauth_provider_id", nullable = false)
    private String oauthProviderId;
    
    /**
     * OAuth 생성
     */
    public static OAuth create(String provider, String providerId) {
        OAuth oauth = new OAuth();
        oauth.oauthProvider = provider;
        oauth.oauthProviderId = providerId;
        return oauth;
    }
    
    /**
     * User 할당 (Package Private)
     */
    void assignUser(User user) {
        this.user = user;
    }
}
