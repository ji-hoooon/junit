package com.mino.bank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mino.bank.config.dummy.DummyObject;
import com.mino.bank.domain.Account;
import com.mino.bank.domain.Transaction;
import com.mino.bank.domain.User;
import com.mino.bank.dto.account.AccountReqDto.AccountDepositReqDto;
import com.mino.bank.dto.account.AccountReqDto.AccountSaveReqDto;
import com.mino.bank.dto.account.AccountReqDto.AccountTransferReqDto;
import com.mino.bank.dto.account.AccountReqDto.AccountWithdrawReqDto;
import com.mino.bank.handler.ex.CustomApiException;
import com.mino.bank.repository.AccountRepository;
import com.mino.bank.repository.TransactionRepository;
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

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Autowired
    private TransactionRepository transactionRepository;


    //영속성 컨텍스트를 비우기 위해 엔티티매니저 의존성 주입
    @Autowired
    private EntityManager em;

    //테스트 전에 DB에 유저를 추가하기 위한 어노테이션
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
        resultActions.andExpect(status().isCreated());
    }

    @BeforeEach
    public void setUp(){
//        User ssar = userRepository.save(newUser("ssar", "pepe ssar"));
//        User cos = userRepository.save(newUser("cos", "pepe coco"));

        //계좌삭제 테스트를 위해 계좌 데이터 생성
//        Account ssarAccount1 = accountRepository.save(newAccount(1111L, ssar));
//        Account cosAccount1 = accountRepository.save(newAccount(2222L, cos));
        //:계좌 상세보기 테스트를 위해 삭제

        //테스트 전에 DB에 유저를 추가하기 위한 어노테이션
        dataSetting();

        //테스트를 제대로 수행하기 위해 영속성 컨텍스트 초기화
        em.clear();
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


    @Test
    public void depositAccount_test() throws Exception{
        //given
        //(1) 테스트를 위한 DTO 생성
        AccountDepositReqDto accountDepositReqDto = new AccountDepositReqDto();
        accountDepositReqDto.setNumber(1111L);
        accountDepositReqDto.setAmount(100L);
        accountDepositReqDto.setGubun("DEPOSIT");
        accountDepositReqDto.setTel("01088888888");

        //(2) 요청 DTO 확인
        String requestBody = om.writeValueAsString(accountDepositReqDto);
        System.out.println("테스트 : "+requestBody);

        //when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post("/api/account/deposit").content(requestBody).contentType(MediaType.APPLICATION_JSON));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : "+responseBody);

        //then
        //잔액이 아닌 상태코드만 확인하면 된다.
        resultActions.andExpect(status().isCreated());
        //잔액이 궁금하면 @JsonIgnore를 잠시 주석해제 후 확인 -> 서비스에서 테스트

    }


    /**
     * 계좌출금 테스트
     * @throws Exception
     */
    @WithUserDetails(value = "ssar", setupBefore = TestExecutionEvent.TEST_EXECUTION)    //DB에서 해당 유저를 조회해서 세션에 담아주는 어노테이션
    @Test
    public void withdrawAccount_test() throws Exception{
        //given
        //(1) 테스트를 위한 DTO 작성
        AccountWithdrawReqDto accountWithdrawReqDto = new AccountWithdrawReqDto();
        accountWithdrawReqDto.setNumber(1111L);
        accountWithdrawReqDto.setPassword(1234L);
        accountWithdrawReqDto.setAmount(100L);
        accountWithdrawReqDto.setGubun("WITHDRAW");

        //(2) 요청 DTO 확인
        String requestBody = om.writeValueAsString(accountWithdrawReqDto);
        System.out.println("테스트 : "+requestBody);

        //when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post("/api/s/account/withdraw").content(requestBody).contentType(MediaType.APPLICATION_JSON));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : "+responseBody);

        //then
        resultActions.andExpect(status().isCreated());


    }

    /**
     * 계좌이체 테스트
     * @throws Exception
     */
    @WithUserDetails(value = "ssar", setupBefore = TestExecutionEvent.TEST_EXECUTION)    //DB에서 해당 유저를 조회해서 세션에 담아주는 어노테이션
    @Test
    public void transferAccount_test() throws Exception{
        //given
        //(1) 테스트를 위한 DTO 작성
        AccountTransferReqDto accountTransferReqDto=new AccountTransferReqDto();
        accountTransferReqDto.setWithdrawNumber(1111L);
        accountTransferReqDto.setDepositNumber(2222L);
        accountTransferReqDto.setWithdrawPassword(1234L);
        accountTransferReqDto.setAmount(100L);
        accountTransferReqDto.setGubun("TRANSFER");


        //(2) 요청 DTO 확인
        String requestBody = om.writeValueAsString(accountTransferReqDto);
        System.out.println("테스트 : "+requestBody);

        //when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post("/api/s/account/transfer").content(requestBody).contentType(MediaType.APPLICATION_JSON));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : "+responseBody);

        //then
        resultActions.andExpect(status().isCreated());
        //: 서비스에서 검증은 했지만, JsonIgnore를 주석처리하고 입금계좌1100과, 출금계좌 900을 DTO로 확인한다.
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
    public void findDetailAccount_test() throws Exception{
        //given
        Long number=1111L;
        String page="0";

        //when
        ResultActions resultActions=mvc.perform(MockMvcRequestBuilders.get("/api/s/account/"+number).param("page", page));
        //동적 쿼리이므로 -> Param 필요 -> 하나일 땐 param, 두개이상 params
        //바디가 존재할 때만 필요한 content, ContentType - POST,PUT
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : "+responseBody);

        //then
        //: JSON 데이터 검증시에는 jsonPath 이용
//        resultActions.andExpect(jsonPath("$.data.transactions[0].balance").value(900L));
//        resultActions.andExpect(jsonPath("$.data.transactions[1].balance").value(800L));
//        resultActions.andExpect(jsonPath("$.data.transactions[2].balance").value(700L));
//        resultActions.andExpect(jsonPath("$.data.transactions[3].balance").value(800L));
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
}