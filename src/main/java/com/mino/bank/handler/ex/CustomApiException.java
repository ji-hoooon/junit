package com.mino.bank.handler.ex;


//커스텀 예외클래스 작성
public class CustomApiException extends RuntimeException{
    public CustomApiException(String message) {
        super(message);
    }
}
