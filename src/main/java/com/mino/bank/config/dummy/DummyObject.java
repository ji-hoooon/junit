package com.mino.bank.config.dummy;

import com.mino.bank.domain.Account;
import com.mino.bank.domain.Transaction;
import com.mino.bank.domain.TransactionEnum;
import com.mino.bank.domain.User;
import com.mino.bank.domain.UserEnum;
import com.mino.bank.repository.AccountRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

public class DummyObject {
    //모두 스태틱 메서드
    protected User newUser(String username, String fullname){
        BCryptPasswordEncoder passwordEncoder= new BCryptPasswordEncoder();
        String encPassword = passwordEncoder.encode("1234");
        return User.builder()
                .username(username)
//                .password("1234")
                .password(encPassword)
                .email(username+"@nate.com")
                .fullname(fullname)
                .role(UserEnum.CUSTOMER)
                .build();
    }
    protected static User newMockUser(Long id,String username, String fullname){
        BCryptPasswordEncoder passwordEncoder= new BCryptPasswordEncoder();
        String encPassword = passwordEncoder.encode("1234");
        return User.builder()
                .id(id)
                .username(username)
//                .password("1234")
                .password(encPassword)
                .email(username+"@nate.com")
                .fullname(fullname)
                .role(UserEnum.CUSTOMER)
                .build();
    }

    protected static Account newAccount(Long number, User user){
        return Account.builder()
                .number(number)
                .user(user)
                .password(1234L)
                .balance(1000L)
                .build();
    }

    protected static Account newMockAccount(Long id,Long number, Long balance,User user) {
        return Account.builder()
                .id(id)
                .number(number)
                .user(user)
                .password(1234L)
                .balance(balance)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    //계좌 1111L 1000원
    //입금 트랜잭션 -> 계좌를 받아와서 1100원으로 변경 -> 입금 트랜잭션 히스토리 생성
    protected static Transaction newDepositTransaction(Account account){
        //(1) 테스트를 위해서 계좌를 받아와서 입금 수행
        account.deposit(100L);
        //(2) 트랜잭션 히스토리 생성
        Transaction transaction = Transaction.builder()
                .depositAccount(account)
                .depositAccountBalance(account.getBalance())   //입금된 금액을 추가

                .withdrawAccount(null)  //무통장입금이므로 이체일때만 존재
                .withdrawAccountBalance(null)   //무통장입금이므로 이체일때만 존재

                .amount(100L)   //입금할 금액을 추가
                .gubun(TransactionEnum.DEPOSIT) //열거형을 이용해 구분값 설정
                .sender("ATM")  //sender 값을 보고 나중에 ATM 문자열을 보고 출금 정보가 없는 것을 알 수도 있다.

                .receiver(account.getNumber() + "")  //받는 사람 (계좌번호)
                .tel("01099997777")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return transaction;
    }

    //계좌 1111L 1000원
    //입금 트랜잭션 -> 계좌를 받아와서 1100원으로 변경 -> 입금 트랜잭션 히스토리 생성
    protected static Transaction newMockDepositTransaction(Long id, Account account){
        //(1) 테스트를 위해서 계좌를 받아와서 입금 수행
        account.deposit(100L);
        //(2) 트랜잭션 히스토리 생성
        Transaction transaction = Transaction.builder()
                .id(id)
                .depositAccount(account)
                .depositAccountBalance(account.getBalance())   //입금된 금액을 추가

                .withdrawAccount(null)  //무통장입금이므로 이체일때만 존재
                .withdrawAccountBalance(null)   //무통장입금이므로 이체일때만 존재

                .amount(100L)   //입금할 금액을 추가
                .gubun(TransactionEnum.DEPOSIT) //열거형을 이용해 구분값 설정
                .sender("ATM")  //sender 값을 보고 나중에 ATM 문자열을 보고 출금 정보가 없는 것을 알 수도 있다.

                .receiver(account.getNumber() + "")  //받는 사람 (계좌번호)
                .tel("01099997777")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return transaction;
    }
    protected Transaction newDepositTransaction(Account account, AccountRepository accountRepository){
     account.deposit(100L); //1000원이었을 경우 900원이 된다.
     //서비스 레이어를 통해 수행한 메서드가 아니므로, 더티체킹이 안된다.
        if(accountRepository!=null){
            accountRepository.save(account);
            //DB에 900원으로 변경
        }
        Transaction transaction = Transaction.builder()
                .withdrawAccount(null)
                .withdrawAccountBalance(null)
                .depositAccount(account)
                .depositAccountBalance(account.getBalance())
                .amount(100L)
                .gubun(TransactionEnum.DEPOSIT)
                .sender("ATM")
                .receiver(account.getNumber()+"")
                .tel("01022227777")
                .build();
        return transaction;
    }
    protected Transaction newWithdrawTransaction(Account account, AccountRepository accountRepository){
        account.withdraw(100L); //1000원이었을 경우 900원이 된다.
        //서비스 레이어를 통해 수행한 메서드가 아니므로, 더티체킹이 안된다.

        //Repository 테스트에서는 더티체킹이 되고, Controller 테스트에서는 더티체킹이 안된다.
        //: save 필수 -> 테스트간 일관성을 위해서
        if(accountRepository!=null){
            accountRepository.save(account);
            //DB에 900원으로 변경
        }
        Transaction transaction = Transaction.builder()
                .withdrawAccount(account)
                .withdrawAccountBalance(account.getBalance())
                .depositAccount(null)
                .depositAccountBalance(null)
                .amount(100L)
                .gubun(TransactionEnum.WITHDRAW)
                .receiver("ATM")
                .sender(account.getNumber()+"")
                .build();
        return transaction;
    }
    protected Transaction newTransferTransaction(Account withdrawAccount,Account depositAccount, AccountRepository accountRepository){
        withdrawAccount.withdraw(100L); //1000원이었을 경우 900원이 된다.
        depositAccount.deposit(100L); //1000원이었을 경우 900원이 된다.

        //서비스 레이어를 통해 수행한 메서드가 아니므로, 더티체킹이 안된다.
        //Repository 테스트에서는 더티체킹이 되고, Controller 테스트에서는 더티체킹이 안된다.
        //: save 필수
        if(accountRepository!=null){
            accountRepository.save(withdrawAccount);
            //DB에 900원으로 변경
        }
        Transaction transaction = Transaction.builder()
                .withdrawAccount(withdrawAccount)
                .withdrawAccountBalance(withdrawAccount.getBalance())
                .depositAccount(depositAccount)
                .depositAccountBalance(depositAccount.getBalance())
                .amount(100L)
                .gubun(TransactionEnum.TRANSFER)
                //: 오류체크 ㅜㅜ
                .receiver(depositAccount.getNumber()+"")
                .sender("ATM")
                .build();
        return transaction;
    }

 }
