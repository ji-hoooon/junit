package com.mino.bank.dto.user;

import com.mino.bank.domain.user.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class UserRespDto {
    @ToString
    @Getter
    @Setter
    //임시로 서비스 안에 회원가입 응답을 위한 DTO 작성
    public static class JoinRespDto{
        private Long id;
        private String username;
        private String fullname;

        public JoinRespDto(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.fullname = user.getFullname();
        }
    }
}
