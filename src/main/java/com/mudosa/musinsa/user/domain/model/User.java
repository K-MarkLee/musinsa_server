package com.mudosa.musinsa.user.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


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

    @Builder
    private User(String userName, String password, String userEmail, String contactNumber, UserRole role, String currentAddress, String avatarUrl, Boolean isActive) {
        this.userName = userName;
        this.password = password;
        this.userEmail = userEmail;
        this.contactNumber = contactNumber;
        this.role = role;
        this.currentAddress = currentAddress;
        this.avatarUrl = avatarUrl;
        this.isActive = isActive;
    }

    public static User create(
        String userName,
        String password,
        String email,
        UserRole role,
        String avatarUrl,
        String contactNumber,
        String currentAddress
    ) {
       return User.builder()
               .avatarUrl(avatarUrl)
               .currentAddress(currentAddress)
               .contactNumber(contactNumber)
               .password(password)
               .userEmail(email)
               .userName(userName)
               .role(role)
               .isActive(true)
               .build();
    }
}
