package com.mino.bank.dto.user;

import com.mino.bank.domain.user.User;
import com.mino.bank.domain.user.UserEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserReqDto {
    //임시로 서비스 안에 회원가입 요청을 위한 DTO 작성
    //: 유효성 검사 필요
    @Getter
    @Setter
    public static class JoinReqDto {
        private String username;
        private String password;
        private String email;
        private String fullname;

        //DTO를 엔티티로 변환하는 메서드 작성
        //: 패스워드 인코더를 파라미터로 받아서 패스워드 인코딩 수행
        public User toEntity(BCryptPasswordEncoder bCryptPasswordEncoder){
            return User.builder()
                    .username(username)
                    //패스워드는 인코딩 필요
//                    .password(password)
                    .password(bCryptPasswordEncoder.encode(password))
                    .email(email)
                    .fullname(fullname)
                    .role(UserEnum.CUSTOMER)
                    .build();
        }
    }
}
