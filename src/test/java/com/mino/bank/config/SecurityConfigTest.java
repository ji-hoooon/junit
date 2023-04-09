package com.mino.bank.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Sql("classpath:db/teardown.sql")

//가짜 환경에 MockMvc가 등록됨
@AutoConfigureMockMvc
//통합 테스트 수행
//: 가짜 환경에서 수행하는 Mockito 테스트
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
public class SecurityConfigTest {
    //가짜 환경에 등록된 MockMvc를 의존성 주입
    @Autowired
    private MockMvc mvc;

    //서버는 일관성있게 에러가 리턴되어야 하므로, 프론트에 전달되기 전 모든 에러를 제어
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
        //:403 출력 -> 401 출력이 필요

        //then

        assertThat(httpStatusCode).isEqualTo(401);
    }
    @Test
    public void authorization_test() throws Exception{
        //given

        //when
        ResultActions resultActions=mvc.perform(MockMvcRequestBuilders.get(("/api/admin/hello")));

        //웹, PostMan, 테스트에서 응답의 일관성을 유지하기 위해서 코드 변경 필요
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        int httpStatusCode = resultActions.andReturn().getResponse().getStatus();
        System.out.println("테스트 : "+responseBody);
        //:바디에 담기는 데이터가 없음
        System.out.println("테스트 : "+httpStatusCode);
        //:403 출력
        //then

        assertThat(httpStatusCode).isEqualTo(403);
    }
}
