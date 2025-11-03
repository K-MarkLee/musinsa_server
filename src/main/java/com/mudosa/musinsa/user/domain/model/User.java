package com.mudosa.musinsa.user.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 사용자 애그리거트 루트
 */
@Entity
@Table(name = "`user`")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    
    @Column(name = "user_name", nullable = false, length = 50)
    private String userName;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "user_email", length = 100)
    private String userEmail;
    
    @Column(name = "contact_number", length = 50)
    private String contactNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;
    
    @Column(name = "current_address")
    private String currentAddress;
    
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // OAuth 정보 (같은 애그리거트)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OAuth> oauths = new ArrayList<>();

    public static User create(
        String userName,
        String password,
        String email,
        UserRole role,
        String avatarUrl,
        String contactNumber,
        String currentAddress
    ) {
        User user = new User();
        user.userName = userName;
        user.password = password;
        user.userEmail = email;
        user.role = role;
        user.isActive = true;
        user.avatarUrl = avatarUrl;
        user.contactNumber = contactNumber;
        user.currentAddress = currentAddress;
        return user;
    }

    public void addOAuth(OAuth oauth) {
        this.oauths.add(oauth);
        oauth.assignUser(this);
    }

    public void setAvatarUrl(String avatarUrl){
        this.avatarUrl = avatarUrl;
    }

}
