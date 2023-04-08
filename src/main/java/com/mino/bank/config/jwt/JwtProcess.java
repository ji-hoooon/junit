package com.mino.bank.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mino.bank.config.auth.LoginUser;
import com.mino.bank.domain.User;
import com.mino.bank.domain.UserEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class JwtProcess {
    private final Logger log = LoggerFactory.getLogger(getClass());

    //토큰 생성 메서드
    public static String create(LoginUser loginUser){
        String jwtToken = JWT.create()
                .withSubject("bank")
                .withExpiresAt(new Date(System.currentTimeMillis()+JwtVO.EXPIRATION_TIME))
                .withClaim("id", loginUser.getUser().getId())
                .withClaim("role", loginUser.getUser().getRole().name())  //getRole() -> UserEnum 타입이므로
//                .withClaim("id", loginUser.getUser().getRole()+"")  //getRole() -> UserEnum 타입이므로
                .sign(Algorithm.HMAC512(JwtVO.SECRET));
        return JwtVO.TOKEN_PREFIX+jwtToken;
    }
    //토큰 검증 메서드
    //(1) 토큰을 받아서 클레임으로 유저 객체 생성
    //(2) User 객체를 이용해서 생성한 LoginUser 객체 생성
    //(3) 생성한 LoginUser 객체를 강제로 시큐리티 세션에 주입 (강제 로그인)
    public static LoginUser verify(String token){
        //(1) 토큰을 받아서 클레임으로 유저 객체 생성
        DecodedJWT decodedJWT=JWT.require(Algorithm.HMAC512(JwtVO.SECRET)).build().verify(token);
        Long id = decodedJWT.getClaim("id").asLong();
        String role = decodedJWT.getClaim("role").asString();
        User user = User.builder().id(id).role(UserEnum.valueOf(role)).build();

        //(2) User 객체를 이용해서 생성한 LoginUser 객체 생성
        LoginUser loginUser = new LoginUser(user);

        //(3) 생성한 LoginUser 객체를 강제로 시큐리티 세션에 주입 (강제 로그인)
        return loginUser;
    }

}
