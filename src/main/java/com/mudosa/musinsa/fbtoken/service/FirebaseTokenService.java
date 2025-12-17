package com.mudosa.musinsa.fbtoken.service;

import com.mudosa.musinsa.fbtoken.dto.FBTokenDTO;
import com.mudosa.musinsa.fbtoken.model.FirebaseToken;
import com.mudosa.musinsa.fbtoken.repository.FirebaseTokenRepository;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseTokenService {
    private final FirebaseTokenRepository firebaseTokenRepository;
    private final UserRepository userRepository;

    //Create
    public void createFirebaseToken(FBTokenDTO fbTokenDTO) throws DataIntegrityViolationException {
        User resultUser = userRepository.findById(Long.parseLong(fbTokenDTO.getMemberId())).orElseThrow(
                ()-> new NoSuchElementException("User not found")
        );

        FirebaseToken firebaseToken = FirebaseToken.builder()
                .firebaseTokenKey(fbTokenDTO.getToken())
                .user(resultUser).build();
            firebaseTokenRepository.save(firebaseToken);
    }

    //Read
    public List<FBTokenDTO> readFirebaseTokens(List<Long> userIds) {
        List<FirebaseToken> firebaseTokens = firebaseTokenRepository.findByUserIdIn(userIds);
        List<FBTokenDTO> result = new ArrayList<>();
        for (FirebaseToken firebaseToken : firebaseTokens) {
            FBTokenDTO dto = FBTokenDTO.builder()
                    .token(firebaseToken.getFirebaseTokenKey())
                    .memberId(firebaseToken.getUser().getId())
                    .build();
            result.add(dto);
        }
        return result;
    }

//    public int updateFirebaseToken(String token, Long userId) {
//        return firebaseTokenRepository.updateFirebaseToken(token, userId);
//    }
//
//    public void deleteFirebaseToken(Long tokenId) {
//        firebaseTokenRepository.deleteById(tokenId);
//    }
}
