package com.mino.bank.service;

import com.mino.bank.domain.Account;
import com.mino.bank.domain.Transaction;
import com.mino.bank.domain.TransactionEnum;
import com.mino.bank.domain.User;
import com.mino.bank.dto.account.AccountReqDto.AccountDepositReqDto;
import com.mino.bank.dto.account.AccountReqDto.AccountSaveReqDto;
import com.mino.bank.dto.account.AccountRespDto.AccountDepositRespDto;
import com.mino.bank.dto.account.AccountRespDto.AccountDto;
import com.mino.bank.dto.account.AccountRespDto.AccountSaveRespDto;
import com.mino.bank.handler.ex.CustomApiException;
import com.mino.bank.repository.AccountRepository;
import com.mino.bank.repository.TransactionRepository;
import com.mino.bank.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AccountService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    //입금 진행시 거래내역을 보존하기 위한 리포지토리 의존성 주입
    private final TransactionRepository transactionRepository;

    //DB의 변경에 요구되므로, 커밋이 진행된다.
    @Transactional
    public AccountSaveRespDto 계좌등록(AccountSaveReqDto accountSaveReqDto, Long userId) {


        //(1) User 로그인 되어있는지 컨트롤러에서 체크하고 userId만 받는다.
        //:userId를 이용해 유저 엔티티 가져오기
//        User user=userRepository.findById(userId);
        //(2) 유저 엔티티를 이용해 DB에서 조회한다.
        //Optional 객체이므로,
        User user = userRepository.findById(userId).orElseThrow(
                //예외 발생시 (DB에 해당하는 유저가 없으면)
                () -> new CustomApiException("유저를 찾을 수 없습니다.")
        );
        //(3) 해당 계좌가 DB에 있는지 중복여부를 체크한다.
        //:findByNumber를 만들어서 해당 계좌번호가 존재하는지 체크
        Optional<Account> accountOP = accountRepository.findByNumber(accountSaveReqDto.getNumber());
        if (accountOP.isPresent()) {
            //계좌 중복으로 인한 예외 던짐
            throw new CustomApiException("해당 계좌가 이미 존재합니다.");
        }
        //(4) 계좌를 등록
        //: AccountSaveReqDto의 toEntity메서드를 이용해 엔티티로 변환
        //AccountSaveReqDto: 계좌번호, 잔액, 밸런스, 유저 객체가 필요
        Account accountPS = accountRepository.save(accountSaveReqDto.toEntity(user));

        //(5) DTO를 응답한다.
        //AccountSaveRespDto :id, 계좌번호, 잔액 필요
        //: AccountSaveRespDto의 생성자로 Account를 전달해 생성
        return new AccountSaveRespDto(accountPS);
    }

    public AccountListRespDto 계좌목록보기_유저별(Long userId) {
        //(1) userId로 DB에서 유저 조회
        User userPS = userRepository.findById(userId).orElseThrow(
                () -> new CustomApiException("유저를 찾을 수 없습니다.")
        );
        //(2) 찾은 유저의 id를 이용해서 계좌 목록 조회
        List<Account> accountListPS = accountRepository.findByUser_id(userPS.getId());

        //(3) 찾은 계좌 목록을 응답하기 위한 DTO 작성
        return new AccountListRespDto(userPS, accountListPS);
    }

    @Getter
    @Setter
    public static class AccountListRespDto {
        private String fullname;
        private List<AccountDto> accounts = new ArrayList<>();

        //여러 계좌마다 가지고 있는 User 객체를 각각 조회하면, 매우 많은 리소스 낭비
        //: Account가 가지고 있는 User 객체는 LAZY 로딩하지 않는다.
        //따라서 유저 조회 1번, 계좌 목록 조회 1번으로 DB I/O를 최소화한다.
        public AccountListRespDto(User user, List<Account> accounts) {
            this.fullname = user.getFullname();
            //Account 리스트 -> 각각 new AccountDto해서 리스트로 변환

            //(1) 스트림
//            this.accounts = accounts
//                    //스트림 생성 메서드
//                    .stream()
//                    //변환 - 중간 연산
//                    .map(
//                            (account)-> new AccountDto(account)
//                    )
//                    //리스트로 모음 - 최종 연산
//                    .collect(Collectors.toList());

            //(2) 메서드 참조
            this.accounts = accounts
                    //스트림 생성 메서드
                    .stream()
                    //변환 - 중간 연산
                    .map(
                            AccountDto::new
                    )
                    //리스트로 모음 - 최종 연산
                    .collect(Collectors.toList());
        }


    }

    @Transactional
    public void 계좌삭제(Long number, Long userId){
        //(1) 계좌 확인
        Account accountPS = accountRepository.findByNumber(number).orElseThrow(
                () -> new CustomApiException("계좌를 찾을 수 없습니다.")
        );
        //(2) 계좌 소유자 확인
        //: 동일한 유저인지 확인해야하는데, Account 객체에서 확인-checkOwner
        accountPS.checkOwner(userId);

        //(3) 계좌 삭제
        accountRepository.deleteById(accountPS.getId());
    }

    @Transactional
    //무통장 입금이므로 인증이 필요없는 로직
    //ATM에서 무통장 입금에 필요한 정보를 위한 요청 DTO 필요: AccountDepositReqDto
    public AccountDepositRespDto 계좌입금(AccountDepositReqDto accountDepositReqDto){

        //로직 순서가 매우 중요한 메서드
        //(1) 0원 체크
        if(accountDepositReqDto.getAmount()<=0L){
            throw new CustomApiException("0원 이하의 금액을 입금할 수 없습니다.");
        }

        //(2) 입금계좌 확인
        Account depositAccountPS = accountRepository.findByNumber(accountDepositReqDto.getNumber()).orElseThrow(
                () -> new CustomApiException("계좌를 찾을 수 없습니다.")
        );

        //(3) 입금 (해당 계좌 balance 조정 - update문 - 더티체킹)
        //: Account 클래스에 입금 메서드 작성
        depositAccountPS.deposit(accountDepositReqDto.getAmount());
//        System.out.println("테스트 : "+depositAccountPS.getBalance());
        //서비스 테스트를 위한 임시 값 확인

        //(4) 거래내역 남기기
        Transaction transaction = Transaction.builder()
                .depositAccount(depositAccountPS)
                .depositAccountBalance(depositAccountPS.getBalance())   //입금된 금액을 추가
                .withdrawAccount(null)  //무통장입금이므로 이체일때만 존재
                .withdrawAccountBalance(null)   //무통장입금이므로 이체일때만 존재
                .amount(accountDepositReqDto.getAmount())   //입금할 금액을 추가
                .gubun(TransactionEnum.DEPOSIT) //열거형을 이용해 구분값 설정
                .sender("ATM")  //sender 값을 보고 나중에 ATM 문자열을 보고 출금 정보가 없는 것을 알 수도 있다.
                .receiver(accountDepositReqDto.getNumber() + "")  //받는 사람 (계좌번호)
                .tel(accountDepositReqDto.getTel())
                .build();

        Transaction transactionPS = transactionRepository.save(transaction);

        //(5) 입금 성공했다는 응답을 하기위한 DTO 작성 : 필요한 정보는 id, number, TransactionDto
        //: TransactionDto의 경우 Transaction 객체를 받아 생성
        return new AccountDepositRespDto(depositAccountPS, transactionPS);
    }


}