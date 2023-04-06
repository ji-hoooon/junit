package com.mino.bank.service;

import com.mino.bank.domain.user.User;
import com.mino.bank.domain.user.UserEnum;
import com.mino.bank.domain.user.UserRepository;
import com.mino.bank.handler.ex.CustomApiException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    //서비스는 DTO를 요청받고 DTO로 응답한다.
    @Transactional
    //메서드 시작할 때 트랜잭션 시작
    //메서드 종료시 트랜잭션 함께 종료
    /**
     * 회원가입 로직 -사용자 이름, 패스워드, 이메일, 이름 필요
     * 1. 사용자 이름 중복 체크
     * 2. 패스워드 인코딩
     * 3. dto 응답
     */
    public JoinRespDto 회원가입(JoinReqDto joinReqDto){
//     1. 사용자 이름 중복 체크
        Optional<User> userOP = userRepository.findByUsername(joinReqDto.getUsername());
        if(userOP.isPresent()){
            //중복된 아이디가 존재하는 경우 예외발생
            throw new CustomApiException("동일한 username이 존재합니다.");
        }
//     2. 패스워드 인코딩 + 회원가입
        User userPS = userRepository.save(joinReqDto.toEntity(bCryptPasswordEncoder));
//     3. dto 응답
        return new JoinRespDto(userPS);

    }
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
