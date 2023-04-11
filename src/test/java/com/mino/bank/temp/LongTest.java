package com.mino.bank.temp;

import org.junit.jupiter.api.Test;

public class LongTest {

    @Test
    public void long_test() throws Exception{
        //given
        Long number1 = 1111L;
        Long number2 = 1111L;


        //when
//        if(number1==number2){
        if(number1.longValue()==number2.longValue()){
            System.out.println("테스트 : 동일합니다.");
        }else
            System.out.println("테스트 : 동일하지 않습니다..");

        Long amount1= 100L;
        Long amount2= 1000L;

        if(amount1<amount2){
            System.out.println("테스트 : amount1이 작습니다.");
        }else System.out.println("테스트 : amount2이 작습니다.");

        //then

    }

    @Test
    public void long_test2 () throws Exception{
        //given
        Long v1= 1000L;
        Long v2= 1000L;

        //Long 값이 충분히 작으면 ==비교로 가능 2의8승까지 -126~127까지만 가능

        //when
        if(v1==v2){
            System.out.println("테스트1 : 같습니다.");
        }
        if(v1.longValue()==v2.longValue()){
            System.out.println("테스트2 : 같습니다.");
        }

    }
}
