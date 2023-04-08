package com.mino.bank.service;

import com.mino.bank.domain.User;
import com.mino.bank.repository.UserRepository;
import com.mino.bank.dto.user.UserReqDto.JoinReqDto;
import com.mino.bank.dto.user.UserRespDto.JoinRespDto;
import com.mino.bank.handler.ex.CustomApiException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
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
    public JoinRespDto 회원가입(@Valid JoinReqDto joinReqDto){
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
}
