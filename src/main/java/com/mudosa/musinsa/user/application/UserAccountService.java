package com.mudosa.musinsa.user.application;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.user.controller.dto.UserCreateRequest;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.model.UserRole;
import com.mudosa.musinsa.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserAccountService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(UserCreateRequest request) {
        if (!isPhonenumberValid(request.getContactNumber())) {
            throw new BusinessException(ErrorCode.INVALID_PHONE_NUMBER_FORMAT);
        }

        if (!isEmailValid(request.getEmail())) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_FORMAT);
        }

        if (!isPasswordValid(request.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }

        if(userRepository.existsByUserEmail(request.getEmail())){
            throw new BusinessException(ErrorCode.ALREADY_REGISTERED_EMAIL);
        }

        User user = User.create(
                request.getUserName(),
                passwordEncoder.encode(request.getPassword()),
                request.getEmail(),
                UserRole.USER,
                request.getAvatarUrl()
        );

        // 연락처와 주소 설정
        if (request.getContactNumber() != null) {
            user.setContactNumber(request.getContactNumber());
        }
        if (request.getAddress() != null) {
            user.setCurrentAddress(request.getAddress());
        }

        userRepository.save(user);
    }

    public static boolean isEmailValid(String email) {
        if (email == null) return false;
        // RFC 5322 기준 간단 버전
        String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(regex);
    }

    public static boolean isPhonenumberValid(String phoneNumber) {
        if (phoneNumber == null) return false;
        // 숫자 11자리, 01로 시작하는 휴대폰 번호만 허용
        return phoneNumber.matches("^01[016789]\\d{7,8}$");
    }

    public static boolean isPasswordValid(String password) {
        if (password == null) return false;
        // 최소 8자, 영문자, 숫자, 특수문자 포함
        String regex = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,}$";
        return password.matches(regex);
    }
}
