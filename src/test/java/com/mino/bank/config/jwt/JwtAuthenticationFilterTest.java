package com.mino.bank.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mino.bank.config.dummy.DummyObject;
import com.mino.bank.domain.user.UserRepository;
import com.mino.bank.dto.user.UserReqDto.LoginReqDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//작성해둔 프로퍼티 설정을 적용하기 위한 어노테이션
// '[org.hibernate.type]': TRACE 적용
//: 쿼리에 들어가는 값까지 확인이 가능
@ActiveProfiles("test")
//가짜 환경으로 스프링에 있는 컴포넌트들을 스캔해서 빈으로 등록
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
//MockMvc를 모키토환경에서 사용하기 위한 어노테이션
@AutoConfigureMockMvc
class JwtAuthenticationFilterTest extends DummyObject {
    @Autowired
    private ObjectMapper om;
    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp(){
        //테스트를 위해 데이터 셋업
        //(1) UserRepository 의존성 주입
        //(2) extends DummyObject
        //(3) 유저 객체를 DB에 삽입
        userRepository.save(newUser("ssar", "pepe ssar"));
    }
    @Test
    void successfulAuthentication_test() throws Exception {
        //given
        //(1) request, response 데이터를 받아 getInputStream()으로 JSON 데이터 파싱
        //: 파싱 결과로 받은 LoginReqDto가 given데이터로 바디에 담겨온다.
        //-> ObjectMapper를 의존성 주입
        LoginReqDto loginReqDto = new LoginReqDto();
        //실제로 사용하지 않을 생성자를 굳이 테스트를 위해서 만들어서 작성하지말것
        loginReqDto.setUsername("ssar");
        loginReqDto.setPassword("1234");

        //(2) ObjectMapper로 LoginReqDto를 JSON으로 변환
        String requestBody = om.writeValueAsString(loginReqDto);
        System.out.println("테스트 : "+requestBody);

        //when
        //(3) 강제 로그인 부분은 UserDetailsService의 LoadUserByUsername 실행되는 부분이므로 Post 요청을 수행해서 테스트
        //: 가짜환경에서 요청을 수행한 결과를 ResultActions에 담는다.
        //가짜환경의 요청에는 HTTP메서드, 컨텐트, 컨텐트타입을 명시해야한다.
        ResultActions resultActions = mvc.perform(post("/api/login").content(requestBody).contentType(MediaType.APPLICATION_JSON));
        //(4) 요청에 대한 리턴에서 응답을 얻어서 내용을 문자열로 만들어서 responseBody에 담는다.
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : "+responseBody);
        //현재 DB에 해당 유저가 존재하지 않으므로, 테스트 실패 -> @BeforeEach나, @SetUp으로 미리 만들어준다.

        //then


    }

    @Test
    void unsuccessfulAuthentication_test() {
        //given

        //when

        //then

    }
}