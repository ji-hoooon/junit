package com.mino.bank.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


//가짜 환경에 MockMvc가 등록됨
@AutoConfigureMockMvc
//통합 테스트 수행
//: 가짜 환경에서 수행하는 Mockito 테스트
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
public class SecurityConfigTest {
    //가짜 환경에 등록된 MockMvc를 의존성 주입
    @Autowired
    private MockMvc mvc;

    @Test
    public void authentication_test() throws Exception{
        //given

        //when
        ResultActions resultActions=mvc.perform(MockMvcRequestBuilders.get(("/api/s/hello")));

        //웹, PostMan, 테스트에서 응답의 일관성을 유지하기 위해서 코드 변경 필요
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        int httpStatusCode = resultActions.andReturn().getResponse().getStatus();
        System.out.println("테스트 : "+responseBody);
        //:바디에 담기는 데이터가 없음
        System.out.println("테스트 : "+httpStatusCode);
        //:403출력

        //then
    }
    @Test
    public void authorization_test() throws Exception{
        //given


        //when

        //then

    }
}
