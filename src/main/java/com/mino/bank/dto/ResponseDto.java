package com.mino.bank.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ResponseDto<T> {
    private final Integer code; //1 성공, -1 실패
    private final String msg;
    private final T data;
    //응답의 Dto는 한 번 만들어지면 수정할 일이 없다.


}
