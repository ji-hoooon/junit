package com.mino.bank.dto.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mino.bank.domain.Account;
import com.mino.bank.domain.Transaction;
import com.mino.bank.util.CustomDateUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            private String tel;
            private String createdAt;

            @JsonIgnore
            //: JSON 데이터로 변환할때에는 무시되는 변수
            private Long depositAccountBalance;
            //: 테스트를 위한 변수로 클라이언트에게 전달시에는 제외해야한다.

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

    //DTO가 똑같다고 해도 재사용하지 않아야 한다.
    //: 변경에 유연하도록 독립적으로 작성한다.
    @Getter
    @Setter
    public static class AccountTransferRespDto {
        private Long id;
        private Long number;

        private Long balance;
        private AccountTransferRespDto.TransactionDto transaction;

        public AccountTransferRespDto(Account account, Transaction transaction) {
            this.id = account.getId();
            this.number = account.getNumber();
            this.balance=account.getBalance();
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
            private String createdAt;

            public TransactionDto(Transaction transaction) {
                this.id = transaction.getId();
                this.gubun = transaction.getGubun().getValue();
                this.sender = transaction.getSender();
                this.receiver = transaction.getReceiver();
                this.amount = transaction.getAmount();
                this.createdAt = CustomDateUtil.toStringFormat(transaction.getCreatedAt());
            }
        }
    }

    @Getter
    @Setter
    public static class AccountWithdrawRespDto{
        private Long id;
        private Long number;

        private Long balance;   //출금계좌잔액
        private TransactionDto transaction;

        public AccountWithdrawRespDto(Account account, Transaction transaction) {
            this.id = account.getId();
            this.number = account.getNumber();
            this.balance=account.getBalance();
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
            private String createdAt;

            @JsonIgnore
            //: JSON 데이터로 변환할때에는 무시되는 변수
            private Long depositAccountBalance; //입금계좌잔액
            //: 테스트를 위한 변수로 클라이언트에게 전달시에는 제외해야한다.
            public TransactionDto(Transaction transaction) {
                this.id = transaction.getId();
                this.gubun = transaction.getGubun().getValue();
                this.sender = transaction.getSender();
                this.receiver = transaction.getReceiver();
                this.amount = transaction.getAmount();
                this.createdAt = CustomDateUtil.toStringFormat(transaction.getCreatedAt());
                this.depositAccountBalance = transaction.getDepositAccountBalance();
            }
        }
    }

    @Getter
    @Setter
    public static class AccountDetailRespDto {
        private Long id; // 계좌 ID
        private Long number; // 계좌번호
        private Long balance; // 그 계좌의 최종 잔액
        private List<TransactionDto> transactions = new ArrayList<>();

        public AccountDetailRespDto(Account account, List<Transaction> transactions) {
            this.id = account.getId();
            this.number = account.getNumber();
            this.balance = account.getBalance();
            this.transactions = transactions.stream()
                    .map((transaction) -> new TransactionDto(transaction, account.getNumber()))
                    .collect(Collectors.toList());
        }

        @Getter
        @Setter
        public class TransactionDto {

            private Long id;
            private String gubun;
            private Long amount;

            private String sender;
            private String reciver;

            private String tel;
            private String createdAt;
            private Long balance;

            public TransactionDto(Transaction transaction, Long accountNumber) {
                this.id = transaction.getId();
                this.gubun = transaction.getGubun().getValue();
                this.amount = transaction.getAmount();
                this.sender = transaction.getSender();
                this.reciver = transaction.getReceiver();
                this.createdAt = CustomDateUtil.toStringFormat(transaction.getCreatedAt());
                this.tel = transaction.getTel() == null ? "없음" : transaction.getTel();

                if (transaction.getDepositAccount() == null) {
                    this.balance = transaction.getWithdrawAccountBalance();
                } else if (transaction.getWithdrawAccount() == null) {
                    this.balance = transaction.getDepositAccountBalance();
                } else {
                    if (accountNumber.longValue() == transaction.getDepositAccount().getNumber().longValue()) {
                        this.balance = transaction.getDepositAccountBalance();
                    } else {
                        this.balance = transaction.getWithdrawAccountBalance();
                    }
                }

            }
        }
    }
}
