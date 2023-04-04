package com.mino.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

//Audit 기능 사용하기 위한 어노테이션 3
@EnableJpaAuditing
@SpringBootApplication
public class JunitstudyApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(JunitstudyApplication.class, args);
        //등록된 빈 목록 확인
//        String[] iocNames = context.getBeanDefinitionNames();
//        for(String name:iocNames){
//            System.out.println(name);
//        }
    }

}
