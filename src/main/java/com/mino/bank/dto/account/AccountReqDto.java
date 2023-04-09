package com.mino.bank.dto.account;

import com.mino.bank.domain.Account;
import com.mino.bank.domain.User;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class AccountReqDto {
    //요청을 위한 임시 DTO 작성
    //: 계좌등록을 위한 요청 DTO
    @Getter
    @Setter
    public static class AccountSaveReqDto{
        @NotNull
//        @Size : 문자열의 길이 체크
        @Digits(integer = 4, fraction = 4)   //: 숫자의 길이 체크 최소4자 최대4자
        private Long number;
        @Digits(integer = 4, fraction = 4)   //: 숫자의 길이 체크 최소4자 최대4자
        private Long password;
//        private User user;
        //: 세션에 있는걸로 검증할 것이므로

        //User를 받아서 엔티티로 변환하는 메서드
        public Account toEntity(User user){
            return Account.builder()
                    .number(number)
                    .password(password)
                    .balance(1000L)
                    .user(user)
                    .build();
        }

    }
    @Getter
    @Setter
    public static class AccountDepositReqDto{
        @NotNull
        @Digits(integer =4, fraction = 4)
        private Long number;
        @NotNull
        //0원 체크도 가능하긴 하지만 -> 서비스에서 체크
        private Long amount;
        @NotEmpty
        @Pattern(regexp = "^(DEPOSIT)$")
        private String gubun;   //DEPOSIT
        @NotEmpty
//        @Pattern(regexp = "^[0-9]{3}-[0-9]{4}[0-9]{4}")
        @Pattern(regexp = "^[0-9]{11}")
        private String tel; //입금이 잘못 되었을 때를 대비해 필요한 입금자 연락처

        //: 정규표현식 테스트 필요
    }
}
