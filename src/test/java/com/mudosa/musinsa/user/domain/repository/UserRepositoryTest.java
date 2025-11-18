package com.mudosa.musinsa.user.domain.repository;

import com.mudosa.musinsa.ServiceConfig;
import com.mudosa.musinsa.order.application.dto.UserInfoDto;
import com.mudosa.musinsa.user.domain.model.User;
import com.mudosa.musinsa.user.domain.model.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class UserRepositoryTest extends ServiceConfig {

    @DisplayName("사용자 id로 이름, 주소, 번호를 조회한다. ")
    @Test
    void findDtoById(){
        //given
        User user = saveUser("tester");

        userRepository.save(user);

        //when
        UserInfoDto result = userRepository.findDtoById(user.getId());

        //then
        assertThat(result)
                .extracting("userName","currentAddress", "contactNumber")
                .contains(
                        "tester", "서울 강남구", "010-0000-0000"
                );
    }

    private User saveUser(String userName) {
        User user = User.create(userName, "test1234", "test@test.com", UserRole.USER, "http://mudosa/uploads/avatar/avatar1.png", "010-0000-0000", "서울 강남구");
        userRepository.save(user);
        return user;
    }

}