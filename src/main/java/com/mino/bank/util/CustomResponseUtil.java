package com.mino.bank.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mino.bank.dto.ResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

public class CustomResponseUtil {
    private static final Logger log = LoggerFactory.getLogger(CustomResponseUtil.class);
    public static void unAuthentication(HttpServletResponse response, String msg){
        //파싱 오류가 날 경우 예외 처리
        try{
            //응답을 JSON으로 만들기
            ObjectMapper objectMapper=new ObjectMapper();
//            ResponseDto<?> responseDto=new ResponseDto<>(-1, "인증되지 않은 사용자", null);
            ResponseDto<?> responseDto=new ResponseDto<>(-1, msg, null);
            String responseBody = objectMapper.writeValueAsString(responseDto);

            response.setContentType("application/json; charset=utf-8");
            response.setStatus(401);
            //response.getWriter().println("error");
            response.getWriter().println(responseBody);
            //공통적인 응답 DTO 작성 필요
        }catch (Exception e){
            log.error("서버 파싱 에러");

        }


    }
    public static void unAuthorization(HttpServletResponse response, String msg){
        //파싱 오류가 날 경우 예외 처리
        try{
            //응답을 JSON으로 만들기
            ObjectMapper objectMapper=new ObjectMapper();
//            ResponseDto<?> responseDto=new ResponseDto<>(-1, "권한이 없는 사용자", null);
            ResponseDto<?> responseDto=new ResponseDto<>(-1, msg, null);
            String responseBody = objectMapper.writeValueAsString(responseDto);

            response.setContentType("application/json; charset=utf-8");
            response.setStatus(403);
            //response.getWriter().println("error");
            response.getWriter().println(responseBody);
            //공통적인 응답 DTO 작성 필요
        }catch (Exception e){
            log.error("서버 파싱 에러");

        }


    }
}
