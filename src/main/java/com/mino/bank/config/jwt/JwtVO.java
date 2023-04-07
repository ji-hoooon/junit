package com.mino.bank.config.jwt;

/**
 * SECRET은 노출안되도록 클라우드 AWS, 환경변수, 파일에 저장
 * 액세스 토큰 만료시 리플래시 토큰 생성해 UX 향상
 */
public class JwtVO {
    public static final String SECRET="BANK_APP";   //HS256 - 대칭키
    public static final int EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7;  //만료시간을 일주일로 설정
    public static final String TOKEN_PREFIX= "Bearer ";    //프로토콜 강제사항
    public static final String HEADER="Authorization";



}
