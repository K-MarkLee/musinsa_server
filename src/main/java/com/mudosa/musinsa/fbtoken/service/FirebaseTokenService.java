package com.mudosa.musinsa.fbtoken.service;

import com.mudosa.musinsa.fbtoken.dto.FBTokenDTO;
import com.mudosa.musinsa.fbtoken.model.FirebaseToken;
import com.mudosa.musinsa.fbtoken.repository.FirebaseTokenRepository;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class FirebaseTokenService {
    private final FirebaseTokenRepository firebaseTokenRepository;
    private final UserRepository userRepository;

    //Create
    public void createFirebaseToken(Long userId, String token) {
        User resultUser = userRepository.findById(userId).orElseThrow(
                ()-> new NoSuchElementException("User not found")
        );

        FirebaseToken firebaseToken = FirebaseToken.builder()
                .firebaseTokenKey(token)
                .user(resultUser).build();
        firebaseTokenRepository.save(firebaseToken);
    }

    //Read
    public List<FBTokenDTO> readFirebaseTokens(List<Long> userIds) {
        List<FirebaseToken> firebaseTokens = firebaseTokenRepository.findByUserIdIn(userIds);
        List<FBTokenDTO> result = new ArrayList<>();
        for (FirebaseToken firebaseToken : firebaseTokens) {
            FBTokenDTO dto = FBTokenDTO.builder()
                    .firebaseTokenKey(firebaseToken.getFirebaseTokenKey())
                    .userId(firebaseToken.getUser().getId())
                    .build();
            result.add(dto);
        }
        return result;
    }

    public int updateFirebaseToken(String token, Long userId) {
        return firebaseTokenRepository.updateFirebaseToken(token, userId);
    }

    public void deleteFirebaseToken(Long tokenId) {
        firebaseTokenRepository.deleteById(tokenId);
    }
}
