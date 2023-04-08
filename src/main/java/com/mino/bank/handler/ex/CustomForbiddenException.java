 package com.mino.bank.handler.ex;


//커스텀 예외클래스 작성
public class CustomForbiddenException extends RuntimeException{
    public CustomForbiddenException(String message) {
        super(message);
    }
}
