package com.mino.bank.dto.account;

import com.mino.bank.domain.Account;
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
}
