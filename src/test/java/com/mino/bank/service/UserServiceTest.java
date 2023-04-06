package com.mino.bank.service;

import com.mino.bank.config.dummy.DummyObject;
import com.mino.bank.domain.user.User;
import com.mino.bank.domain.user.UserRepository;
import com.mino.bank.dto.user.UserReqDto.JoinReqDto;
import com.mino.bank.dto.user.UserRespDto.JoinRespDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

//서비스 테스트를 위한 모키토 환경은 스프링 관련 빈들이 존재하지 않는다.
@ExtendWith(MockitoExtension.class)
public class UserServiceTest extends DummyObject {

    //가짜 환경으로 주입하기 위한 어노테이션
    //: Mock으로 만든 가짜 객체와, Spy로 가져온 진짜 객체를 Inject한다.
    @InjectMocks
    private UserService userService;

    //모키토 환경에서는
    //빈의 직접 등록이 필요하므로 @Mock을 이용해 가짜로 메모리에 띄운다.
    @Mock
    private UserRepository userRepository;

    //진짜 객체를 가짜 객체로 집어넣는 방법
    @Spy
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Test
    public void 회원가입_test() throws Exception{
        //given
        //요청 데이터 전달하기 위한 JoinReqDto 필요
        JoinReqDto joinReqDto = new JoinReqDto();
        joinReqDto.setUsername("ssar");
        joinReqDto.setPassword("1234");
        joinReqDto.setEmail("ssar@nate.com");
        joinReqDto.setFullname("pepe ssar");

        //stub1
        //가짜로 띄워줘도 해당 클래스의 메서드가 존재하지 않기 때문에, stub 처리 필요
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        //: 리포지토리의 중복 체크 메서드를 실행시, 빈 옵셔널 객체를 리턴한다.
//        when(userRepository.findByUsername(any())).thenReturn(Optional.of(new User()));
        //: 동일한 유저네임을 전달 리턴할 경우


        //stub2
        //user 객체를 리턴하는 스텁
        //: 패스워드 인코딩도 필요
//        User user= User.builder()
//                .id(1L)
//                .username("ssar")
//                .password("1234")
//                .email("ssar@nate.com")
//                .fullname("pepe ssar")
//                .role(UserEnum.CUSTOMER)
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .build();
        // 더미 오브젝트로 리팩토링
        User user = newMockUser(1L, "ssar", "pepe ssar");

        when(userRepository.save(any())).thenReturn(user);

        //when
        JoinRespDto joinRespDto = userService.회원가입(joinReqDto);
        System.out.println("테스트 : "+joinRespDto);
        //then


        assertThat(joinRespDto.getId()).isEqualTo(1L);
        assertThat(joinRespDto.getUsername()).isEqualTo("ssar");
    }
}
