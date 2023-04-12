package com.mino.bank.dto.transaction;

import com.mino.bank.domain.Account;
import com.mino.bank.domain.Transaction;
import com.mino.bank.util.CustomDateUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionRespDto {
    @Getter
    @Setter
    public static class TransactionListRespDto{
        private List<TransactionDto> transactions=new ArrayList<>();

        //계좌 번호를 받아야 하므로, Account와 Transaction 엔티티 리스트로 Dto 만들기 위한 생성자
        public TransactionListRespDto(Account account, List<Transaction> transactions) {
            this.transactions =  transactions.stream()
                    .map(
                            (transaction) -> new TransactionDto( transaction,account.getNumber())
                    ).collect(Collectors.toList());
        }

        @Getter
        @Setter
        public class TransactionDto{
            private Long id;
            private String gubun;
            private Long amount;
            private String sender;
            private String receiver;
            private String tel;
            private String createdAt;
            private Long balance;


            //계좌이체 -> 내 계좌 번호와 같은 계좌의 잔액을 출력하기 위해 accountNumber 필요
            public TransactionDto(Transaction transaction, Long accountNumber) {
                this.id = transaction.getId();
                this.gubun = transaction.getGubun()+"";
                this.amount = transaction.getAmount();
                this.sender = transaction.getSender();
                this.receiver = transaction.getReceiver();
                //입출금 모두 존재하므로, 입금시에만 TEL이 존재하므로, 조건에 따라 null을 직접 처리한다.
                this.tel = transaction.getTel()==null ? "없음" : transaction.getTel();

                this.createdAt = CustomDateUtil.toStringFormat(transaction.getCreatedAt());
                //입출금 모두 존재하므로, 입금시에는 입금계좌의 잔액이 존재하고, 출금시에는 출금계좌의 잔액이 존재한다.
                if(transaction.getDepositAccountBalance()==null){
                    this.balance=transaction.getWithdrawAccountBalance();
                }else if((transaction.getWithdrawAccountBalance()==null)){
                    this.balance=transaction.getDepositAccountBalance();
                }else{
                    //계좌이체 -> 내 계좌 번호와 같은 계좌의 잔액을 출력
                    //:주의 롱타입이므로, longValue()로 동등비교 필요
                    if(accountNumber.longValue()==transaction.getDepositAccount().getNumber()){
                        this.balance=transaction.getDepositAccountBalance();
                    }else if(accountNumber.longValue()==transaction.getWithdrawAccount().getNumber()){
                        this.balance=transaction.getWithdrawAccountBalance();
                    }
                }
            }   //: 동적 쿼리를 이용해 동적 DTO를 만들기 위함
        }

    }
}
