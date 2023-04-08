package com.mino.bank.config.jwt;

import com.mino.bank.config.auth.LoginUser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 모든 주소에서 동작하는 토큰 검증 필터
 */
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(isHeaderVerify(request, response)){
            //JWT 존재할 때
            //(1) 프로토콜로 인해 필요했던 Bearer 접두사 제거
            String token = request.getHeader(JwtVO.HEADER).replace(JwtVO.TOKEN_PREFIX, "");
            //(2) 토큰을 이용해 토큰 검증 수행해 로그인 유저 정보 얻는다.
            LoginUser loginUser = JwtProcess.verify(token);

            //(3) 강제로 임시 세션에 로그인한 유저로 유저의 토큰을 생성해 넣는다. (여기서 확인할 정보는 해당 유저의 권한정보)
            //UsernamePasswordAuthenticationToken의 파라미터는 로그인유저 객체 or username (null), 비밀번호 (null), 로그인한 유저의 권한
            //UsernamePasswordAuthenticationToken의 부모는 AbstractAuthenticationToken의 부모는 Authentication
            Authentication authentication = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            //(4) 강제 로그인 수행
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        // 다음 필터 수행
        chain.doFilter(request,response);

    }

    //헤더에 JWT 있는지 체크하는 검증 메서드
    private boolean isHeaderVerify(HttpServletRequest request, HttpServletResponse response){
        String header = request.getHeader(JwtVO.HEADER);
        if(header==null || !header.startsWith(JwtVO.TOKEN_PREFIX)){
            return false;
        }
        return true;
    }
}
