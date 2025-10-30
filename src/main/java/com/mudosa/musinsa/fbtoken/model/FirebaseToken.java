package com.mudosa.musinsa.fbtoken.model;

import com.mudosa.musinsa.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="firebase_token")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FirebaseToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;
    private String firebaseTokenKey;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;
}
