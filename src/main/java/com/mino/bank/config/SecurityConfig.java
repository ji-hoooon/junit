package com.mino.bank.config;

import com.mino.bank.domain.user.UserEnum;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


//@Slf4j
//private final Logger log = LoggerFactory.getLogger(getClass()); 대신해주는데, Junit 테스트 오류 발생
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
        //iframe 사용안함
        http.headers().frameOptions().disable();
        //csrf 사용안함
        http.csrf().disable();
        //다른 서버의 자바스크립트 요청 거부 허용으로 (거부할 사항을 Null)
        //: cross origin resource sharing
        http.cors().configurationSource(configurationSource());

        //stateless 전략으로 사용하기 위해 jSessionId를 서버쪽에서 관리안하도록 설정
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        //react 같은 프론트엔드 프레임워크 이용하므로 제공되는 폼 사용안함
        http.formLogin().disable();
        //httpbasic 방식 사용안함 (팝업창 이용해서 사용자 인증하는 방식)
        http.httpBasic().disable();

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
