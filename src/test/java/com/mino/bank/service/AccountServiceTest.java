package com.mino.bank.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mino.bank.config.dummy.DummyObject;
import com.mino.bank.domain.Account;
import com.mino.bank.domain.Transaction;
import com.mino.bank.domain.User;
import com.mino.bank.dto.account.AccountReqDto.AccountDepositReqDto;
import com.mino.bank.dto.account.AccountReqDto.AccountSaveReqDto;
import com.mino.bank.dto.account.AccountRespDto.AccountDepositRespDto;
import com.mino.bank.dto.account.AccountRespDto.AccountSaveRespDto;
import com.mino.bank.repository.AccountRepository;
import com.mino.bank.repository.TransactionRepository;
import com.mino.bank.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

//필요한 환경만 띄워서 테스트하는 가짜 환경 테스트
@ExtendWith(MockitoExtension.class)
class AccountServiceTest extends DummyObject {
    @InjectMocks    //@Mock들이 주입되는 대상
    private AccountService accountService;  //@Mock 객체를 대신 주입해준다.

    @Mock
    private UserRepository userRepository;  //가짜 환경에 주입할 가짜 객체
    @Mock
    private AccountRepository accountRepository;  //가짜 환경에 주입할 가짜 객체

    @Mock
    private TransactionRepository transactionRepository;    //가짜 환경에 주입할 가짜 객체

    @Spy
    private ObjectMapper om;

    @Test
    void 계좌등록_test() throws Exception{
        //given
        //: 계좌 등록시 사용할 유저 아이디 명시
        Long userId = 1L;
        AccountSaveReqDto accountSaveReqDto=new AccountSaveReqDto();
        accountSaveReqDto.setNumber(1111L);
        accountSaveReqDto.setPassword(1234L);

        //stub
        //(1) findById
//        User user=newMockUser(1L, "ssar", "pepe ssar"); //id를 넣기 위해서는 newMockUser 필요
        User user=newMockUser(userId, "ssar", "pepe ssar"); //id를 넣기 위해서는 newMockUser 필요
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        //: DB에서 조회했을 때 해당 유저가 존재해야 가능하므로

        //(2) findByNumber
        when(accountRepository.findByNumber(any())).thenReturn(Optional.empty());
        //: DB에서 조회했을 때 해당 번호를 가진 계좌가 없어야 가능하므로

        //(3) save
        //id를 넣기 위해서는 newMockAccount 필요 -> newAccount 메서드와 newMockAccount 메서드 작성
        Account account=newMockAccount(1L,1111L,1000L,user);
        when(accountRepository.save(any())).thenReturn(account);

        //when
        AccountSaveRespDto accountSaveRespDto = accountService.계좌등록(accountSaveReqDto, userId);
        //RespDto를 JSON으로 확인
        String responseBody = om.writeValueAsString(accountSaveRespDto);
        System.out.println("테스트 : "+responseBody);

        //then
        assertThat(accountSaveRespDto.getNumber()).isEqualTo(1111L);

    }


    /**
     * 테스트 할 대상
     * (1) Account -> balance 변경 여부
     * (2) Transaction -> balance 기록 여부
     * @throws Exception
     */
    @Test
    public void 계좌입금_test() throws Exception{
        //given
        //(1) 테스트를 위한 DTO 생성
        AccountDepositReqDto accountDepositReqDto = new AccountDepositReqDto();
        accountDepositReqDto.setNumber(1111L);
        accountDepositReqDto.setAmount(100L);
        accountDepositReqDto.setGubun("DEPOSIT");
        accountDepositReqDto.setTel("01088888888");

        //(2) 테스트에 필요한 스텁 작성 :
        // 서비스 테스트할 떄 mock 객체의 경우 연관된 객체들간에 서로 공유하면 안 됨 꼬임 방지를 위해 각각 작성

        //stub
//        //1. findByNumber
//        User ssar = newMockUser(1L, "ssar", "pepe ssar");   //실행 됨
//        Account ssarAccount1 = newMockAccount(1L, 1111L, 1000L, ssar);  //실행 됨 -ssarAccount1 1000원
//        when(accountRepository.findByNumber(any())).thenReturn(Optional.of(ssarAccount1));  //실제 서비스 수행 이전까지는 실행되지 않은 코드 -> 서비스 실행시 1100원 -> deposit() 실행시 1200원
//        //2. save
//        //: 더미 오브젝트로 newMockTransaction 작성
//        Transaction transaction = newMockDepositTransaction(1L, ssarAccount1); //실행 됨 -ssarAccount1 1100원 / transaction - 1100원
//        when(transactionRepository.save(any())).thenReturn(transaction);    //실제 서비스 수행 이전까지는 실행되지 않은 코드

        //1. findByNumber
        User ssar = newMockUser(1L, "ssar", "pepe ssar");   //실행 됨
        Account ssarAccount1 = newMockAccount(1L, 1111L, 1000L, ssar);  //실행 됨
        when(accountRepository.findByNumber(any())).thenReturn(Optional.of(ssarAccount1));  //실제 서비스 수행 이전까지는 실행되지 않은 코드
        //2. save
        //: 더미 오브젝트로 newMockTransaction 작성
        Account ssarAccount2 = newMockAccount(1L, 1111L, 1000L, ssar);  //실행 됨
        Transaction transaction = newMockDepositTransaction(1L, ssarAccount2); //실행 됨
        when(transactionRepository.save(any())).thenReturn(transaction);    //실제 서비스 수행 이전까지는 실행되지 않은 코드


        //when
        AccountDepositRespDto accountDepositRespDto = accountService.계좌입금(accountDepositReqDto);
        System.out.println("테스트 : 트랜잭션 입금 계좌 잔액 : "+accountDepositRespDto.getTransaction().getDepositAccountBalance());
        System.out.println("테스트 : 계좌 쪽 잔액"+ssarAccount1.getBalance());
        //트랜잭션 부분의 입금 계좌 잔액 : 1100, 계좌 쪽 잔액 : 1200 으로 서로 다름
        //-> 스텁에 필요한 객체는 각각 만들어서 사용한다.

        //then
        assertThat(ssarAccount1.getBalance()).isEqualTo(1100L);
        assertThat(ssarAccount2.getBalance()).isEqualTo(1100L);
        assertThat(accountDepositRespDto.getTransaction().getDepositAccountBalance()).isEqualTo(1100L);

    }
}