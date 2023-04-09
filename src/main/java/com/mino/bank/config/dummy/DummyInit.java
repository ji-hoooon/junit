package com.mino.bank.config.dummy;

import com.mino.bank.domain.Account;
import com.mino.bank.domain.User;
import com.mino.bank.repository.AccountRepository;
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
    //: DI 두 번째 방법 메서드 주입
    CommandLineRunner init(UserRepository userRepository, AccountRepository accountRepository){
        return (args) -> {
            User ssar = userRepository.save(newUser("ssar", "pepe ssar"));
            User cos = userRepository.save(newUser("cos", "pepe coco"));

            //계좌삭제 테스트를 위해 계좌 데이터 생성
            Account ssarAccount1 = accountRepository.save(newAccount(1111L, ssar));
            Account cosAccount1 = accountRepository.save(newAccount(2222L, cos));
        };
    }
}
