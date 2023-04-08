package com.mino.bank.config.dummy;

import com.mino.bank.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
public class DummyInit extends DummyObject{

    //빈 메서드에서 프로퍼티 설정
    @Profile("dev")
    @Bean
    //설정 클래스에 있는 빈메서드는 서버 실행시 무조건 실행
    CommandLineRunner init(UserRepository userRepository){
        return (args) -> {
            userRepository.save(newUser("ssar", "pepe ssar"));
        };
    }
}
