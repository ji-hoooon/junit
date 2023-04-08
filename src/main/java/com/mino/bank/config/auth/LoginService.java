package com.mino.bank.config.auth;

import com.mino.bank.domain.User;
import com.mino.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

//    @Autowired
//    private UserRepository userRepository;

    private final UserRepository userRepository;

    @Override
    //로그인할 때 세션 생성 메서드
    //: 시큐리티로 로그인 시, 시큐리티가 loadUserByUsername() 실행해 username 체크
    // 없으면 예외 발생
    // 있으면 정상적으로 시큐리티 컨텍스트 내부 세션에 로그인된 세션이 만들어진다.
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User userPS=userRepository.findByUsername(username).orElseThrow(
                //인증 과정에 오류가 발생하면, 시큐리티가 제어권을 가지고 있기 때문에 Exception 발동
                ()->new InternalAuthenticationServiceException("인증 실패")
                //테스트할 때 확인 필요
        );
        //있으면 정상적으로 시큐리티 컨텍스트 내부 세션에 로그인된 세션 추가
        return new LoginUser(userPS);
    }
}
