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
import com.mino.bank.handler.ex.CustomApiException;
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
     * 정석적인 서비스 테스트
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
        // 서비스 테스트할 떄 mock 객체의 경우 연관된 객체들간에 서로 공유하면 안 됨 꼬임 방지를 위해 독립적으로 각각 작성

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
        User ssar2 = newMockUser(1L, "ssar", "pepe ssar");   //실행 됨
        Account ssarAccount2 = newMockAccount(1L, 1111L, 1000L, ssar2);  //실행 됨
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
    /**
     * 합리적인 서비스 테스트
     * 테스트 할 대상
     * (1) 0원 입금 안되도록 테스트
     * (2) 입금시 기존 잔액에 추가로 더해지는지
     * (3) DTO가 잘 작성되는지  -> 컨트롤러에서도 가능하므로 생략가능
     * @throws Exception
     */

    @Test
    public void 계좌입금_test2() throws Exception{


        //given
        //(1) 테스트를 위한 DTO 생성
        AccountDepositReqDto accountDepositReqDto = new AccountDepositReqDto();
        accountDepositReqDto.setNumber(1111L);

//        accountDepositReqDto.setAmount(0L); //(1) 0원 입금 테스트 실행
        //(2) 입금시 기존 잔액에 추가로 더해지는지 -> Service에 syst 추가
        accountDepositReqDto.setAmount(100L);
        accountDepositReqDto.setGubun("DEPOSIT");
        accountDepositReqDto.setTel("01088888888");

        //계좌가 필요하므로 stub 생성
        User ssar = newMockUser(1L, "ssar", "pepe ssar");   //실행 됨
        Account ssarAccount1 = newMockAccount(1L, 1111L, 1000L, ssar);  //실행 됨
        when(accountRepository.findByNumber(any())).thenReturn(Optional.of(ssarAccount1));  //실제 서비스 수행 이전까지는 실행되지 않은 코드

        //(3) DTO가 잘 작성되는지 확인 하기위해 stub 생성
        User ssar2 = newMockUser(1L, "ssar", "pepe ssar");   //실행 됨
        Account ssarAccount2 = newMockAccount(1L, 1111L, 1000L, ssar2);  //실행 됨
        Transaction transaction = newMockDepositTransaction(1L, ssarAccount2); //실행 됨
        when(transactionRepository.save(any())).thenReturn(transaction);    //실제 서비스 수행 이전까지는 실행되지 않은 코드


        //when
        AccountDepositRespDto accountDepositRespDto = accountService.계좌입금(accountDepositReqDto);


        //then
        //값을 확인하는게 아니라 stub1의 ssarAccount1 입금이 잘됐는지 확인
        assertThat(ssarAccount1.getBalance()).isEqualTo(1100L);

        //트랜잭션의 로그를 확인하기 위해서 DTO가 잘 만들어졌는지 확인
        //: 컨트롤러에서도 확인할 수 있기 때문에 굳이 DTO 작성할 필요는 없다.
//        String responseBody = om.writeValueAsString(accountDepositRespDto);
//        System.out.println("테스트 : "+responseBody);
    }

    /**
     * 간단한 서비스 테스트 방법
     * : 비즈니스 로직만 테스트 (책임의 분리)
     * DTO를 만드는 책임을 서비스가 가지고 있지만, 컨트롤러에서 테스트 가능
     * DB관련된 서비스 로직도 테스트 필요 없음
     * DB관련 로직이 비즈니스 로직을 타고 간다면, stub으로 정의해서 테스트
     * @throws Exception
     */
    @Test
    public void 계좌입금_test3() throws Exception{
        //given
        Account account = newMockAccount(1L, 1111L, 1000L, null);
        Long amount=100L;

        //when
        if(amount<=0L){
            throw new CustomApiException("0원 이하의 금액을 입금할 수 없습니다.");
        }
        account.deposit(100L);

        //then
        assertThat(account.getBalance()).isEqualTo(1100L);
    }


    /**
     * 계좌출금 서비스 테스트
     *  //(1) 테스트에 필요한 변수
     *  //(2) 유저와 계좌 객체 생성
     *  //(3) 0원 체크
     *  //(4) 출금 소유자 확인
     *  //(5) 잔액 확인
     *  //(6) 출금하기
     * @throws Exception
     */
    @Test
    public void 계좌출금_test() throws Exception{
        //given
        //(1) 테스트에 필요한 변수
        Long amount=100L;
        Long password=1234L;
        Long userId=1L;

        //(2) 유저와 계좌 객체 생성
        User ssar = newMockUser(userId, "ssar", "pepe ssar");   //실행 됨
        Account ssarAccount = newMockAccount(1L, 1111L, 1000L, ssar);  //실행 됨

        //when
        //(3) 0원 체크
        if(amount<=0L){
            throw new CustomApiException("0원 이하의 금액을 입금할 수 없습니다.");
        }

        //(4) 출금 소유자 확인
        ssarAccount.checkOwner(userId);
        //Long 타입의 값 비교는 .longValue()
        ssarAccount.checkSamePassword(password);

        //(5) 잔액 확인
//        ssarAccount.checkBalance(amount);
        //(6) 출금하기
        ssarAccount.withdraw(amount);
        //: 출금하기 메서드 실행 전, 잔액 확인 메서드가 실행되지 않을 수도 있다. -> 안전하지 않은 코드
        //: 잔액 확인 + 출금하기로 메서드 리팩토링

        //then
        assertThat(ssarAccount.getBalance()).isEqualTo(900L);
    }
}