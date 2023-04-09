package com.mino.bank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mino.bank.config.dummy.DummyObject;
import com.mino.bank.domain.Account;
import com.mino.bank.domain.User;
import com.mino.bank.dto.account.AccountReqDto.AccountSaveReqDto;
import com.mino.bank.handler.ex.CustomApiException;
import com.mino.bank.repository.AccountRepository;
import com.mino.bank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

//@Transactional
@Sql("classpath:db/teardown.sql")
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
class AccountControllerTest extends DummyObject {

    //(1) @Autowired로 주입시 빼먹을 수 있다.
    //(2) 리플렉션으로 찾는데 시간이 걸린다.
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;


    //영속성 컨텍스트를 비우기 위해 엔티티매니저 의존성 주입
    @Autowired
    private EntityManager em;

    //테스트 전에 DB에 유저를 추가하기 위한 어노테이션
    @BeforeEach
    public void setUp(){
        User ssar = userRepository.save(newUser("ssar", "pepe ssar"));
        User cos = userRepository.save(newUser("cos", "pepe coco"));

        //계좌삭제 테스트를 위해 계좌 데이터 생성
        Account ssarAccount1 = accountRepository.save(newAccount(1111L, ssar));
        Account cosAccount1 = accountRepository.save(newAccount(2222L, cos));

        //테스트를 제대로 수행하기 위해 영속성 컨텍스트 초기화
        em.clear();
    }

    //계좌등록 전에 로그인 필요
    //실제 로그인 로직 : jwt -> 인증 필터 -> 시큐리티 세션 생성
    //: JWT를 이용한 토큰 방식의 로그인보다는, 세션에 직접 LoginUser를 주입하는 방식으로 강제 로그인 진행
//    @WithUserDetails(value = "ssar")    //DB에서 해당 유저를 조회해서 세션에 담아주는 어노테이션
    //setUp()으로 추가 했음에도 setupBefore=TEST_METHOD 에러 발생
    //:  TEST_METHOD 즉, @WithUserDetails가 setUp() 메서드 수행 전에 실행되기 때문에
    @WithUserDetails(value = "ssar", setupBefore = TestExecutionEvent.TEST_EXECUTION)    //DB에서 해당 유저를 조회해서 세션에 담아주는 어노테이션
    @Test
    public void 계좌등록_test() throws Exception{
        //given
        AccountSaveReqDto accountSaveReqDto= new AccountSaveReqDto();
        accountSaveReqDto.setNumber(9999L);
        accountSaveReqDto.setPassword(1234L);
        //JSON으로 변환 -> ObjectMaper 의존성 주입
        String requestBody = om.writeValueAsString(accountSaveReqDto);
        System.out.println("테스트 : "+requestBody);

        //when
        //perform() : 요청 주소, 컨텐트, 컨텐트 타입
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post("/api/s/account").content(requestBody).contentType(MediaType.APPLICATION_JSON));
        //resultActions.andReturn()
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : "+responseBody);

        //then
        //검증 수행
        resultActions.andExpect(MockMvcResultMatchers.status().isCreated());
    }

    /**
     * 테스트시에 Insert 한 영속 객체들이 전부 영속화 되어 PC에 저장
     * 영속화 된 영속 객체들을 초기화해 개발 모드와 동일한 환경으로 테스트를 진행해야 한다.
     * 최초 select는 쿼리가 발생 - PC에 존재하면 1차 캐시
     * PC에 존재하면 LAZY 로딩시에는 쿼리가 발동하지 않는다.
     * LAZY 로딩시 PC에 없으면 쿼리 발동
     *
     * @throws Exception
     */
    @WithUserDetails(value = "ssar", setupBefore = TestExecutionEvent.TEST_EXECUTION)    //DB에서 해당 유저를 조회해서 세션에 담아주는 어노테이션
//    @WithUserDetails(value = "cos", setupBefore = TestExecutionEvent.TEST_EXECUTION)    //DB에서 해당 유저를 조회해서 세션에 담아주는 어노테이션
    //: LAZY 로딩 발동을 위한 테스트
    @Test
    public void deleteAccount_test() throws Exception{
        //given
//        Long number =2222L;
        Long number =1111L; //LAZY로딩 테스트

        //when
        ResultActions resultActions = mvc.perform(delete("/api/s/account/"+number));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : "+responseBody);


        //then
        //Junit 테스트에서 DELETE 쿼리는 DB관련으로 가장 마지막에 실행되면 발동하지 않는다.
        //assertThrows 명령이 존재하면 쿼리 발동
        assertThrows(CustomApiException.class, ()->accountRepository.findByNumber(number).orElseThrow(
                ()->new CustomApiException("계좌를 찾을 수 없습니다.")
        ));
    }

}