package com.mudosa.musinsa.fbtoken.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="firebase_token")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FirebaseToken extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;

    @Column(unique = true)
    private String firebaseTokenKey;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;
}
