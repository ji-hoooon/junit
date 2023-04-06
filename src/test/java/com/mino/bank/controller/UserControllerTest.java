package com.mino.bank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mino.bank.config.dummy.DummyObject;
import com.mino.bank.domain.user.UserRepository;
import com.mino.bank.dto.user.UserReqDto.JoinReqDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)

//통합테스트
public class UserControllerTest extends DummyObject {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper om;
    @Test
    public void join_success_test() throws Exception{
        //given
        JoinReqDto joinReqDto=new JoinReqDto();
        joinReqDto.setUsername("love");
        joinReqDto.setPassword("1234");
        joinReqDto.setEmail("love@nate.com");
        joinReqDto.setFullname("러브");

        //Object -> JSON
        String requestBody = om.writeValueAsString(joinReqDto);
        System.out.println("테스트 : "+requestBody);
        //when
        ResultActions resultActions=mvc.perform(post("/api/join").content(requestBody).contentType(MediaType.APPLICATION_JSON));
        //컨텐트를 넣으면 반드시 컨텐트를 설명하는 컨텐트타입이 필요하다.
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : "+responseBody);

        //then
        resultActions.andExpect(status().isCreated());  //201
//        resultActions.andExpect(status().isOk());   //200
    }

    @BeforeEach
    public void setUp(){
        dataSetting();
    }

    @Autowired
    UserRepository userRepository;

    private void dataSetting() {
        //DummyObject의 newUser()
        userRepository.save(newUser("ssar", "pepe ssar"));
    }

    @Test
    //통합테스트이므로, 서비스단에서 중복체크에서 예외가 발생하는 경우 테스트
    //: @BeforeEach로 미리 해당 유저를 생성
    public void join_fail_test() throws Exception{
        //given
        JoinReqDto joinReqDto=new JoinReqDto();
        joinReqDto.setUsername("ssar");
        joinReqDto.setPassword("1234");
        joinReqDto.setEmail("love@nate.com");
        joinReqDto.setFullname("러브");

        //Object -> JSON
        String requestBody = om.writeValueAsString(joinReqDto);
        System.out.println("테스트 : "+requestBody);
        //when
        ResultActions resultActions=mvc.perform(post("/api/join").content(requestBody).contentType(MediaType.APPLICATION_JSON));
        //컨텐트를 넣으면 반드시 컨텐트를 설명하는 컨텐트타입이 필요하다.
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : "+responseBody);

        //then
        resultActions.andExpect(status().isBadRequest());  //400
//        resultActions.andExpect(status().isCreated());  //201
//        resultActions.andExpect(status().isOk());   //200
    }
}
