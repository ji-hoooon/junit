package com.mino.bank.temp;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

public class RegexTest {

    //길이는 +,*,{start, end}
    //: +는 빈문자열 불가능, *은 빈문자열도 가능
    @Test
    public void 한글만된다_test() throws Exception {
        //given
        String value = "가나";
//        boolean result = Pattern.matches("^[ㄱ-ㅎ가-힣]$", value);
        //한글로 시작(^)하거나 한글로 끝나($)는 것
//        boolean result = Pattern.matches("^[ㄱ-ㅎ가-힣]*$", value);
        //한글로 시작(^)하거나 한글로 끝나($)는 것+ 무한으로 반복 가능(*), 하지만 빈 문자열도 true
        boolean result = Pattern.matches("^[ㄱ-ㅎ가-힣]+$", value);
        //한글로 시작(^)하거나 한글로 끝나($)는 것+ 무한으로 반복 가능(*), 한 글자 이상 꼭 필요

        System.out.println("테스트 : " + result);
        //빈 regex에 공백이면 참

    }

    @Test
    public void 한글안됨_test() throws Exception {
        //given
        String value = " ";
//        boolean result = Pattern.matches("^[^ㄱ-ㅎ가-힣]+$", value);
        // !(한글로 시작(^)하거나 한글로 끝나($)는 것+ 무한으로 반복 가능(*)), 한글이 아닌 문자 한 글자 이상 꼭 필요
        boolean result = Pattern.matches("^[^ㄱ-ㅎ가-힣]*$", value);
        // !(한글로 시작(^)하거나 한글로 끝나($)는 것+ 무한으로 반복 가능(*)), 빈 문자열도 가능

        System.out.println("테스트 : " + result);


    }

    @Test
    public void 영어만된다_test() throws Exception {
        //given
        String value = "가나";
//        boolean result = Pattern.matches("^[A-Za-z]$", value);
        //영어로 시작(^)하거나 영어로 끝나($)는 것
//        boolean result = Pattern.matches("^[A-Za-z]*$", value);
        //영어로 시작(^)하거나 영어로 끝나($)는 것+ 무한으로 반복 가능(*), 하지만 빈 문자열도 true
        boolean result = Pattern.matches("^[A-Za-z]+$", value);
        //영어로 시작(^)하거나 영어로 끝나($)는 것+ 무한으로 반복 가능(*), 한 글자 이상 꼭 필요

        System.out.println("테스트 : " + result);
        //빈 regex에 공백이면 참

    }

    @Test
    public void 영어는안된다_test() throws Exception {
        //given
        String value = "가11";
//        boolean result = Pattern.matches("^[^A-Za-z]+$", value);
        // !(영어로 시작(^)하거나 영어로 끝나($)는 것+ 무한으로 반복 가능(*)), 영어이 아닌 문자 한 글자 이상 꼭 필요
        boolean result = Pattern.matches("^[^A-Za-z]*$", value);
        // !(영어로 시작(^)하거나 영어로 끝나($)는 것+ 무한으로 반복 가능(*)), 빈 문자열도 가능

        System.out.println("테스트 : " + result);


    }

    @Test
    public void 영어와숫자만된다_test() throws Exception {
        //given
        String value = "가11";
        boolean result = Pattern.matches("^[0-9A-Za-z]+$", value);

        System.out.println("테스트 : " + result);

    }

    @Test
    public void 영어만되고_길이는최소2최대4이다_test() throws Exception {
        //given
        String value = "가나";

        boolean result = Pattern.matches("^[A-Za-z]{2,4}$", value);
        //영어로 시작(^)하거나 영어로 끝나($)는 것+ 무한으로 반복 가능(*), 길이는 2,4만 가능

        System.out.println("테스트 : " + result);
        //빈 regex에 공백이면 참


    }

    @Test
    public void 영어숫자특수문자만되고_공백불가능_길이는최소4최대12이다_test() throws Exception {
        //given
        String value = "a#(sasd(12";

        boolean result = Pattern.matches("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};:\\\\|,.<>\\/?]{4,12}$", value);
        //영어와 숫자,특수문자로 시작(^)하거나 영어와 숫자,특수문자로 끝나($)는 것+ 무한으로 반복 가능(*), 길이는 4,12만 가능

        System.out.println("테스트 : " + result);
        //빈 regex에 공백이면 참


    }

    //username, email, fullname 테스트 진행
    @Test
    //영문,숫자, 2~20자이내
    public void user_username_test() throws Exception {
        //given
        String username = "ssar123";
        boolean result = Pattern.matches("^[A-Za-z0-9]{2,20}$", username);

        System.out.println("테스트 : " + result);
    }

    @Test
    //영문,숫자, 2~20자이내
    public void user_email_test() throws Exception {
        //given
        String email = "ssar@nate.com";
        //com만 가능한 테스트
        //메타문자의 경우 \\를 추가해 메타문자가 아닌걸 표현해야한다.
        boolean result = Pattern.matches("^[A-Za-z0-9]{2,10}@[A-Za-z0-9]{2,6}\\.[a-zA-Z]{2,3}$", email);

        System.out.println("테스트 : " + result);
    }

    @Test
    //영문,한글, 1~20자이내
    public void user_fullname_test() throws Exception {
        //given
        String fullname = "ssar";
        boolean result = Pattern.matches("^[A-Za-z가-힣]{1,20}$", fullname);

        System.out.println("테스트 : " + result);
    }


    @Test
    public void account_gubun_test1() throws Exception{
        //given
        String gubun = "DEPOSIT";
        boolean result = Pattern.matches("^(DEPOSIT)$", gubun);
        //boolean result = Pattern.matches("DEPOSIT", gubun);   //하나인 경우엔 문자열자체로도 가능
//        boolean result = Pattern.matches("^(DEPOSIT|TRANSFER)$", gubun);    //두 개인 경우에도 가능 (띄우면 안됨)

        //배열은 범위, 정확한 문자열은 괄호
        System.out.println("테스트 : "+result);
    }
    @Test
    public void account_gubun_test2() throws Exception{
        //given
        String gubun = "DEPOSIT";
//        boolean result = Pattern.matches("^(DEPOSIT)$", gubun);
        //boolean result = Pattern.matches("DEPOSIT", gubun);   //하나인 경우엔 문자열자체로도 가능
        boolean result = Pattern.matches("^(DEPOSIT|TRANSFER)$", gubun);    //두 개인 경우에도 가능 (띄우면 안됨)

        //배열은 범위, 정확한 문자열은 괄호
        System.out.println("테스트 : "+result);
    }

    @Test
    public void account_tel_test1() throws Exception{
        //given
        String tel = "010-3333-7777";
        boolean result = Pattern.matches("^[0-9]{3}-[0-9]{4}-[0-9]{4}", tel);
        System.out.println("테스트 : "+result);
    }
    @Test
    public void account_tel_test2() throws Exception{
        //given
        String tel = "01033337777";
        boolean result = Pattern.matches("^[0-9]{11}", tel);
        System.out.println("테스트 : "+result);
    }
}