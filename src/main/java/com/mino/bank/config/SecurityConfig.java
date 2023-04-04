package com.mino.bank.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mino.bank.domain.user.UserEnum;
import com.mino.bank.dto.ResponseDto;
import com.mino.bank.util.CustomResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


//@Slf4j
//=private final Logger log = LoggerFactory.getLogger(getClass()); 대신해주는데, Junit 테스트 오류 발생
@Configuration
public class SecurityConfig {
    //로깅
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Bean   //IoC 컨테이너에 BCryptPasswordEncoder 객체 등록
    public BCryptPasswordEncoder passwordEncoder() {
        log.debug("디버그 : BCryptPasswordEncoder 빈 등록됨");
        return new BCryptPasswordEncoder();
    }

    @Bean
    //1. JWT 서버를 만들기 위한 설정
    //2. JWT 필터 등록
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.debug("디버그 : filterChain 빈 등록됨");
        //iframe 사용안함 (인라인 프레임을 생성하는 태그로 타 사이트의 컨텐츠 혹은 타 페이지를 삽입)
        http.headers().frameOptions().disable();
        //csrf 사용안함
        //:Cross-Site Request Forgery
        // 타 사이트에서 인증된 사용자의 권한을 이용해서 공격하는 방식으로,
        // 페이지 로드시 CSRF 토큰을 생성해 해당 토큰을 가진 요청만 처리함으로써 방지한다.
        http.csrf().disable();
        //다른 서버의 자바스크립트 요청 거부 허용으로 (거부할 사항을 Null)
        //: Cross Origin Resource Sharing
        // 프론트엔드와 백엔드의 도메인을 다르게 설정해서 백엔드에서 프론트엔드의 요청만 응답하도록 설정한다.
        http.cors().configurationSource(configurationSource());

        //stateless 전략으로 사용하기 위해 jSessionId를 서버쪽에서 관리안하도록 설정
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        //react 같은 프론트엔드 프레임워크 이용하므로 제공되는 폼 사용안함
        http.formLogin().disable();
        //httpbasic 방식 사용안함 (팝업창 이용해서 사용자 인증하는 방식)
        http.httpBasic().disable();

        //응답의 일관성을 만들기 위해 Exception 가로채기
        http.exceptionHandling().authenticationEntryPoint(

                (request, response, authenticationException) ->{
                    String uri = request.getRequestURI();
                    log.debug("디버그 : "+ uri);
                    if(uri.contains("admin")){
                        CustomResponseUtil.unAuthorization(response, "관리자만 접근이 가능합니다.");
                    }else{
                        CustomResponseUtil.unAuthentication(response, "로그인이 필요합니다.");
                    }

                }
        );
        //인증이 되지 않은 사용자에 대한 예외처리하는 메서드로 파라미터는
        // ExceptionTranslationFilter로 필터링 되는 AuthenticationEntryPoint 객체
        //: AuthenticationEntryPoint의 commence 메서드는 파라미터로 request, response, AuthenticationException


        //접근 권한 설정
        http.authorizeRequests()
                .antMatchers("/api/s/**").authenticated()
                .antMatchers("/api/admin/**").hasRole("" + UserEnum.ADMIN) //default prefix가 'ROLE_'
                .anyRequest().permitAll();

        return http.build();
    }
    public CorsConfigurationSource configurationSource(){
        log.debug("디버그 : CorsConfigurationSource cors 설정돼 SecurityFilterChain에 등록됨");
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");    //HTTP 메서드와 자바스크립트 요청 허용
        configuration.addAllowedOriginPattern("*");     //모든 IP 주소 허용(추후 프론트 엔드 쪽 IP 허용하도록 변경)
        configuration.setAllowCredentials(true);    //클라이언트에서 쿠키 요청 허용

        UrlBasedCorsConfigurationSource source= new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); //작성한 설정을 모든 주소에 적용

        return source;
    }
}
