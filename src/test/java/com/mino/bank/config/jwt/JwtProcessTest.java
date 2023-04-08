package com.mino.bank.config.jwt;

import com.mino.bank.config.auth.LoginUser;
import com.mino.bank.domain.user.User;
import com.mino.bank.domain.user.UserEnum;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProcessTest {

    @Test
    void create_test() {
        //given
        //(1) 테스트에 사용할 유저 객체 생성
        User user = User.builder()
                .id(1L)
                .role(UserEnum.CUSTOMER)
                .build();
        LoginUser loginUser = new LoginUser(user);

        //when
        //(2) 테스트 진행하기 위해 토큰 생성
        String jwtToken = JwtProcess.create(loginUser);
        System.out.println("테스트 : "+jwtToken);

        //then
        //(3) 토큰은 생성시마다 값이 바뀌기 때문에, Token에 Bearer가 붙어있는지만 체크
        assertThat(jwtToken.startsWith(JwtVO.TOKEN_PREFIX));
    }

    @Test
    void verify_test() {
        //given
        //(1) 토큰 검증을 위해 Bearer를 제외한 토큰 값을 가져옴
        String jwtToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJiYW5rIiwicm9sZSI6IkNVU1RPTUVSIiwiaWQiOjEsImV4cCI6MTY4MTUzNzg1M30.8yhovOveayIJN-bCnFKx7ucQRbP0FVH8gLo9tD9a0HG0F2PKZu1cl6RWazwtMVi59ENh_Krve1xQGqFMBBANXA";

        //when
        //(2) 토큰 검증 후, 리턴값을 LoginUser 객체로 저장
        LoginUser loginUser = JwtProcess.verify(jwtToken);
        System.out.println("테스트 : "+loginUser.getUser().getId());
        System.out.println("테스트 : "+loginUser.getUser().getRole().name());

        //then
        Assertions.assertThat(loginUser.getUser().getId()).isEqualTo(1L);
        Assertions.assertThat(loginUser.getUser().getRole()).isEqualTo(UserEnum.CUSTOMER);
    }
}