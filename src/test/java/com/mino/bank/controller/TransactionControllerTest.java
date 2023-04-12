package com.mino.bank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mino.bank.config.dummy.DummyObject;
import com.mino.bank.domain.Account;
import com.mino.bank.domain.Transaction;
import com.mino.bank.domain.User;
import com.mino.bank.repository.AccountRepository;
import com.mino.bank.repository.TransactionRepository;
import com.mino.bank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.persistence.EntityManager;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Sql("classpath:db/teardown.sql")
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
class TransactionControllerTest extends DummyObject {
    //서비스 레이어에서 안한 DTO테스트 수행 필요!!

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

    @Autowired
    private TransactionRepository transactionRepository;


    //영속성 컨텍스트를 비우기 위해 엔티티매니저 의존성 주입
    @Autowired
    private EntityManager em;

    //테스트 전에 DB에 유저를 추가하기 위한 어노테이션
    @BeforeEach
    public void setUp(){
        dataSetting();
        em.clear();
    }
    private void dataSetting() {
        User ssar = userRepository.save(newUser("ssar", "쌀"));
        User cos = userRepository.save(newUser("cos", "코스,"));
        User love = userRepository.save(newUser("love", "러브"));
        User admin = userRepository.save(newUser("admin", "관리자"));

        Account ssarAccount1 = accountRepository.save(newAccount(1111L, ssar));
        Account cosAccount = accountRepository.save(newAccount(2222L, cos));
        Account loveAccount = accountRepository.save(newAccount(3333L, love));
        Account ssarAccount2 = accountRepository.save(newAccount(4444L, ssar));

        Transaction withdrawTransaction1 = transactionRepository
                .save(newWithdrawTransaction(ssarAccount1, accountRepository));
        Transaction depositTransaction1 = transactionRepository
                .save(newDepositTransaction(cosAccount, accountRepository));
        Transaction transferTransaction1 = transactionRepository
                .save(newTransferTransaction(ssarAccount1, cosAccount, accountRepository));
        Transaction transferTransaction2 = transactionRepository
                .save(newTransferTransaction(ssarAccount1, loveAccount, accountRepository));
        Transaction transferTransaction3 = transactionRepository
                .save(newTransferTransaction(cosAccount, ssarAccount1, accountRepository));
    }

    //테스트 전에 DB에 유저를 추가하기 위한 어노테이션
    //계좌등록 전에 로그인 필요
    //실제 로그인 로직 : jwt -> 인증 필터 -> 시큐리티 세션 생성
    //: JWT를 이용한 토큰 방식의 로그인보다는, 세션에 직접 LoginUser를 주입하는 방식으로 강제 로그인 진행
//    @WithUserDetails(value = "ssar")    //DB에서 해당 유저를 조회해서 세션에 담아주는 어노테이션
    //setUp()으로 추가 했음에도 setupBefore=TEST_METHOD 에러 발생
    //:  TEST_METHOD 즉, @WithUserDetails가 setUp() 메서드 수행 전에 실행되기 때문에
    @WithUserDetails(value = "ssar", setupBefore = TestExecutionEvent.TEST_EXECUTION)    //DB에서 해당 유저를 조회해서 세션에 담아주는 어노테이션
    @Test
    public void findTransactionalList_ssar_test() throws Exception{
        //given
        Long number=1111L;
        String gubun="ALL";
        String page="0";

        //when
        ResultActions resultActions=mvc.perform(MockMvcRequestBuilders.get("/api/s/account/"+number+"/transaction").param("gubun", gubun).param("page", page));
        //동적 쿼리이므로 -> Param 필요 -> 하나일 땐 param, 두개이상 params
        //바디가 존재할 때만 필요한 content, ContentType - POST,PUT
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : "+responseBody);

        //then
        //: JSON 데이터 검증시에는 jsonPath 이용
        resultActions.andExpect(jsonPath("$.data.transactions[0].balance").value(900L));
        resultActions.andExpect(jsonPath("$.data.transactions[1].balance").value(800L));
        resultActions.andExpect(jsonPath("$.data.transactions[2].balance").value(700L));
        resultActions.andExpect(jsonPath("$.data.transactions[3].balance").value(800L));
    }

    @WithUserDetails(value = "cos", setupBefore = TestExecutionEvent.TEST_EXECUTION)    //DB에서 해당 유저를 조회해서 세션에 담아주는 어노테이션
    @Test
    public void findTransactionalList_cos_test() throws Exception{
        //given
        Long number=2222L;
        String gubun="ALL";
        String page="0";

        //when
        ResultActions resultActions=mvc.perform(MockMvcRequestBuilders.get("/api/s/account/"+number+"/transaction").param("gubun", gubun).param("page", page));
        //동적 쿼리이므로 -> Param 필요 -> 하나일 땐 param, 두개이상 params
        //바디가 존재할 때만 필요한 content, ContentType - POST,PUT
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : "+responseBody);

        //then
        resultActions.andExpect(jsonPath("$.data.transactions[0].balance").value(1100L));
        resultActions.andExpect(jsonPath("$.data.transactions[1].balance").value(1200L));
        resultActions.andExpect(jsonPath("$.data.transactions[2].balance").value(1100L));
    }
}