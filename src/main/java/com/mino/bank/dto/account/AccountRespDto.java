package com.mino.bank.dto.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mino.bank.domain.Account;
import com.mino.bank.domain.Transaction;
import com.mino.bank.util.CustomDateUtil;
import lombok.Getter;
import lombok.Setter;

public class AccountRespDto {
    //응답을 위한 임시 DTO 작성
    //: 계좌등록을 위한 응답 DTO

    @Getter
    @Setter
    public static class AccountSaveRespDto{
        private Long id;
        private Long number;
        private Long balance;
        //컨트롤러에 항상 엔티티가 아닌 DTO를 전달하기 위한 생성자

        public AccountSaveRespDto(Account account) {
            this.id = account.getId();
            this.number = account.getNumber();
            this.balance = account.getBalance();
        }
    }

    //서비스에서 반드시 컨트롤러한테 엔티티가 아닌 DTO로 변환해서 응답해야 한다.
    //컨트롤러로 응답할 내용 : id, number, balance
    @Getter
    @Setter
    public static class AccountDto{
        private Long id;
        private Long number;
        private Long balance;

        //생성자로 Account를 받아서 DTO로 변환
        public AccountDto(Account account) {
            this.id = account.getId();
            this.number = account.getNumber();
            this.balance = account.getBalance();
        }
    }
    @Getter
    @Setter
    public static class AccountDepositRespDto{
        private Long id;
        private Long number;
        private TransactionDto transaction;

        public AccountDepositRespDto(Account account, Transaction transaction) {
            this.id = account.getId();
            this.number = account.getNumber();
            this.transaction = new TransactionDto(transaction);
        }

        @Getter
        @Setter
        public class TransactionDto{
            //트랜잭션 히스토리

            private Long id;
            private String gubun;
            private String sender;
            private String receiver;
            private Long amount;
            @JsonIgnore
            //: JSON 데이터로 변환할때에는 무시되는 변수
            private Long depositAccountBalance;
            //: 테스트를 위한 변수로 클라이언트에게 전달시에는 제외해야한다.
            private String tel;
            private String createdAt;

            public TransactionDto(Transaction transaction) {
                this.id = transaction.getId();
                this.gubun = transaction.getGubun().getValue();
                this.sender = transaction.getSender();
                this.receiver = transaction.getReceiver();
                this.amount = transaction.getAmount();
                this.depositAccountBalance = transaction.getDepositAccountBalance();
                this.tel = transaction.getTel();
                this.createdAt = CustomDateUtil.toStringFormat(transaction.getCreatedAt());
            }
        }
    }
}
