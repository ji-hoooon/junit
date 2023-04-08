package com.mino.bank.config.jwt;

import com.mino.bank.config.auth.LoginUser;
import com.mino.bank.domain.User;
import com.mino.bank.domain.UserEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


//작성해둔 프로퍼티 설정을 적용하기 위한 어노테이션
// '[org.hibernate.type]': TRACE 적용
//: 쿼리에 들어가는 값까지 확인이 가능
@ActiveProfiles("test")
//가짜 환경으로 스프링에 있는 컴포넌트들을 스캔해서 빈으로 등록
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
//MockMvc를 모키토환경에서 사용하기 위한 어노테이션
@AutoConfigureMockMvc
class JwtAuthorizationFilterTest {
    @Autowired
    private MockMvc mvc;
    @Test
    void authorization_success_test() throws Exception {
        //given
        //(1) 권한 체크를 위한 유저 객체 생성
        User user = User.builder()
                .id(1L)
                .role(UserEnum.CUSTOMER)
                .build();
        LoginUser loginUser = new LoginUser(user);
        //(2) 로그인 유저 객체를 이용해 JWT 직접 생성
        String jwtToken = JwtProcess.create(loginUser);
        System.out.println("테스트 : "+jwtToken);

        //when
        //(3) 인증이 필요하지만, 없는 페이지 요청
//        ResultActions resultActions = mvc.perform(get("/api/s/hello/test"));

        //(3) 인증이 필요하지만, jwt을 담은 헤더를 담아서 없는 페이지 요청
        ResultActions resultActions = mvc.perform(get("/api/s/hello/test").header(JwtVO.HEADER, jwtToken));

        //then
        //(4) 404에러 발생 예상
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    void authorization_fail_test() throws Exception {
        //given

        //when
        //(1) 인증이 필요하지만, 토큰 없이 페이지 요청
        ResultActions resultActions = mvc.perform(get("/api/s"));

        //then
        //(2) 인증이 필요한 401에러 발생 예상
        resultActions.andExpect(status().isUnauthorized());

    }

    @Test
    void authorization_admin_test() throws Exception {
        //given
        //(1) 권한 체크를 위한 유저 객체 생성
        User user = User.builder()
                .id(1L)
                .role(UserEnum.CUSTOMER)
                .build();
        LoginUser loginUser = new LoginUser(user);
        //(2) 로그인 유저 객체를 이용해 JWT 직접 생성
        String jwtToken = JwtProcess.create(loginUser);
        System.out.println("테스트 : "+jwtToken);

        //when
        //(3) 인증이 필요하지만, 없는 페이지 요청
//        ResultActions resultActions = mvc.perform(get("/api/s/hello/test"));

        //(3) 인증이 필요하지만, jwt을 담은 헤더를 담아서 admin권한이 필요한 페이지 요청
        ResultActions resultActions = mvc.perform(get("/api/admin/hello/test").header(JwtVO.HEADER, jwtToken));

        //then
        //(4) 403에러 발생 예상 - 권한이 없음
        resultActions.andExpect(status().isForbidden());
    }



}