package com.mino.bank.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mino.bank.config.auth.LoginUser;
import com.mino.bank.dto.user.UserReqDto.LoginReqDto;
import com.mino.bank.dto.user.UserRespDto.LoginRespDto;
import com.mino.bank.util.CustomResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
        setFilterProcessesUrl("/api/login");
        this.authenticationManager=authenticationManager;
    }

    //Post :/login시 동작하는 메서드
    //-> Post :/api/login시 동작하는 메서드
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.debug("디버그 : attemptAuthentication 호출됨");
        //cmd+option+T
        try {
            //(1) request 객체의 json 데이터 꺼내기
            ObjectMapper om =new ObjectMapper();
            //(2) 로그인을 위한 DTO 작성
            //: UserReqDto의 내부클래스로 LoginReqDto (필터에서는 컨트롤러 전이므로 유효성 검사 불가능)
            LoginReqDto loginReqDto=om.readValue(request.getInputStream(), LoginReqDto.class);

            //(3) 강제 로그인
            //: 토큰 방식의 인증을 사용하더라도 시큐리티의 권한체크, 인증체크 기능을 사용하기 위해 세션 생성
            //-> 임시 세션으로, request와 response 완료시 끝
            UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(loginReqDto.getUsername(), loginReqDto.getPassword());

            Authentication authentication = authenticationManager.authenticate(authenticationToken);    //UserDetailsService의 LoadUserByUsername 호출
            return authentication;

        } catch (Exception e) {
            e.printStackTrace();
            //필터를 모두 통과한 후에 컨트롤러 단으로 들어가고, 그때 CustomExceptionHandler로 처리 가능하므로
            //시큐리티가 가지고 있는 제어권을 가져오기 위해 예외를 던지고 예외처리 과정에서 unsuccessfulAuthentication 호출
            throw new InternalAuthenticationServiceException(e.getMessage());

            //직접 unsuccessfulAuthentication이 실행되기가 어렵기 때문에 InternalAuthenticationServiceException 예외를 던진다.
//            try {
//                unsuccessfulAuthentication(request, response, new InternalAuthenticationServiceException(e.getMessage()));
//            } catch (IOException ex) {
//                throw new RuntimeException(ex);
//            } catch (ServletException ex) {
//                throw new RuntimeException(ex);
//            }
        }
    }

    //return authentication 잘 작동하면 successfulAuthentication 메서드 호출
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        log.debug("디버그 : successfulAuthentication 호출됨");

        //(1) 파라미터의 authResult에서 로그인 유저 객체 얻기
        LoginUser loginUser = (LoginUser) authResult.getPrincipal();
        //(2) 얻은 로그인 유저로 토큰 생성
        String jwtToken = JwtProcess.create(loginUser);
        //(3) 생성한 토큰을 헤더에 추가
        response.addHeader(JwtVO.HEADER, jwtToken);
        //로그인을 위한 응답 DTO 작성

        //(4) loginUser를 이용해 loginRespDto 변환
        LoginRespDto loginRespDto = new LoginRespDto(loginUser.getUser());

        //CustomResponseUtil에 JSON 응답 DTO 생성하는 메서드 작성

        //(5) JSON 응답 DTO 반환
        CustomResponseUtil.success(response, loginRespDto);
    }

    //로그인 실패 로직
    //시큐리티 과정 중 예외이므로, authenticationEntryPoint에 걸린다.
    // : Spring Security에서 인증에 실패한 경우 처리를 담당하는 인터페이스
    //시큐리티가 가지고 있는 제어권을 가져오기 위해 예외를 던짐
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        CustomResponseUtil.fail(response, "로그인 실패", HttpStatus.UNAUTHORIZED);
    }
}
