package com.mino.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

//Audit 기능 사용하기 위한 어노테이션 3
@EnableJpaAuditing
@SpringBootApplication
public class JunitstudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(JunitstudyApplication.class, args);
    }

}
